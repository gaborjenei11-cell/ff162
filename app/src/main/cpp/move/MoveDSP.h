#pragma once
// GSA MOVE XY-only multi FX. Parallel wet return preserves the dry programme.
// No tone-cutting high-pass/low-pass sound-shaping; no user macro controls.
// Time-based effects (delay, reverb, pitch, grain, stereo movement, distortion)
// remain 100% driven by XY, with no audio-thread allocations.
#include "PresetLibrary.h"
#include <array>
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <vector>

namespace gsamove {
constexpr float pi=3.14159265358979323846f;
inline float clamp01(float a) noexcept { return std::clamp(a,0.f,1.f); }
inline float saturate(float s) noexcept { return std::tanh(s); }
inline float dbGain(float d) noexcept { return std::pow(10.f, d/20.f); }
struct Controls {
    float x=0, y=0;
    float dryWet=0.f;  // 0 = unity dry + effect; 1 = effect return only
    int presetIndex=0;
    bool enabled=false, bypass=false;
    // Five internal engines: delay, reverb, distortion, stereo motion, grains.
    std::array<float,6> fxLevel{{0.f,0.f,0.f,0.f,0.f,0.f}};
    std::array<bool,6> fxOn{{false,false,false,false,false,false}};
    std::array<int,6> fxMode{{3,1,1,0,1,0}};
};
// Internal preset roles are modulated by the horizontal, vertical, and diagonal
// pad movement, respectively. This replaces the retired 3 macro parameters.
inline float roleAmount(const Controls& c,EffectRole role) noexcept {
    const auto& mapping=profileFor(c.presetIndex).roles;
    const float axis[3]={clamp01(c.x),clamp01(c.y),
                         std::sqrt(clamp01(c.x)*clamp01(c.y))};
    float result=0.f;
    for(int i=0;i<3;++i) if(mapping[static_cast<size_t>(i)]==role)
        result=std::max(result,axis[i]);
    return result;
}
inline void routeXY(Controls& c) noexcept {
    c.fxOn.fill(false);
    c.fxLevel.fill(0.f);
    const auto& profile=profileFor(c.presetIndex);
    const float axis[3]={clamp01(c.x),clamp01(c.y),
                         std::sqrt(clamp01(c.x)*clamp01(c.y))};
    for(int i=0;i<3;++i) {
        const float amount=axis[i];
        if(amount<=.00001f) continue;
        const auto role=static_cast<unsigned>(profile.roles[static_cast<size_t>(i)]);
        const unsigned mask=effectEngineMasks[role];
        for(int j=1;j<6;++j) if(mask&(1u<<j)) {
            c.fxOn[static_cast<size_t>(j)]=true;
            c.fxLevel[static_cast<size_t>(j)]=std::max(c.fxLevel[static_cast<size_t>(j)],amount);
        }
    }
    const auto cat=categoryForPreset(c.presetIndex);
    c.fxMode[1]=c.presetIndex%4;
    c.fxMode[2]=(c.presetIndex/3)%4;
    c.fxMode[3]=(c.presetIndex+1)%4;
    c.fxMode[4]=(c.presetIndex/5)%4;
    c.fxMode[5]=(c.presetIndex/7)%4;
    // Every transition is a pitch-grain riser/drop, regardless of recipe.
    if(cat==Category::Transition && std::max(c.x,c.y)>.00001f) {
        c.fxOn[2]=true; c.fxOn[5]=true;
        c.fxLevel[2]=std::max(c.fxLevel[2],std::sqrt(clamp01(c.x)*clamp01(c.y)));
        c.fxLevel[5]=std::max(c.fxLevel[5],std::max(c.x,c.y));
    }
    // Stutters must capture repeats when the pad is active.
    if(cat==Category::Stutter && std::max(c.x,c.y)>.00001f) {
        c.fxOn[5]=true;
        c.fxLevel[5]=std::max(c.fxLevel[5],std::max(c.x,c.y));
    }
}
class MoveDSP {
public:
    void prepare(double sampleRate) {
        sr = std::clamp(sampleRate, 8000.0, 384000.0);
        for(int ch=0;ch<2;++ch) {
            auto& s=channels[ch];
            s.delay.assign(static_cast<size_t>(sr*4.1)+8,0.f);
            s.capture.assign(static_cast<size_t>(sr*1.1)+8,0.f);
            s.delayIndex=0; s.captureIndex=0; s.dc=0;s.prevDC=0;
            s.crushCounter=0;s.crushValue=0;
            for(int k=0;k<4;++k) {
                // mutually prime-ish lengths for dense, decorrelated FDN tails
                const double d[4]={.0297,.0371,.0411,.0533};
                s.fdn[k].data.assign(std::max<size_t>(3,static_cast<size_t>(sr*d[k]*(ch ? 1.071 : 1.0))),0.f);
                s.fdn[k].index=0;s.fdn[k].damp=0; s.fdn[k].gain=.8f;
            }
            s.diff1.data.assign(std::max<size_t>(3,static_cast<size_t>(sr*(ch?.0067:.0049))),0.f);
            s.diff2.data.assign(std::max<size_t>(3,static_cast<size_t>(sr*(ch?.0107:.0083))),0.f);
            s.diff1.index=s.diff2.index=0;
        }
        controlClock=7; phase=0; stutterClock=0; stutterStart[0]=stutterStart[1]=0;
        historyGate=0; mixSmoothed=0.f; pitchGrainPhase=0.f; target={}; derived={};
        noiseState=0x12345ad1u; stutterLoop=0;
    }
    void reset() noexcept {
        for(auto& s:channels) {
            std::fill(s.delay.begin(),s.delay.end(),0.f);std::fill(s.capture.begin(),s.capture.end(),0.f);
            s.delayIndex=s.captureIndex=0;s.noiseLow=s.dc=s.prevDC=0;
            for(auto& line:s.fdn) {std::fill(line.data.begin(),line.data.end(),0.f);line.index=0;line.damp=0;}
            for(auto* a:{&s.diff1,&s.diff2}) {std::fill(a->data.begin(),a->data.end(),0.f);a->index=0;}
        }
        phase=0;stutterClock=0;historyGate=0;mixSmoothed=0.f;pitchGrainPhase=0.f;
    }
    // Called for each stereo frame. Grouped control calculations occur once every 32 samples.
    void process(float& left,float& right,const Controls& c,const Preset& preset,double bpm) noexcept {
        if(channels[0].delay.empty()) return;
        if(controlClock++ >= 7) {controlClock=0;makeTargets(c,preset,bpm);}
        const float smooth=static_cast<float>(1.0-std::exp(-8.0/(.003*sr)));
        auto sm=[smooth](float& v,float dst) noexcept { v+=smooth*(dst-v); };
        if(controlClock==0) {
            sm(derived.delayFrames,target.delayFrames);sm(derived.feedback,target.feedback);
            sm(derived.reverb,target.reverb);sm(derived.decay,target.decay);
            sm(derived.drive,target.drive);sm(derived.crush,target.crush);
            sm(derived.wet,target.wet);sm(derived.width,target.width);
            sm(derived.rate,target.rate);sm(derived.gate,target.gate);
            sm(derived.smear,target.smear);
            for(auto& s:channels) for(auto& line:s.fdn) {
                const float delaySeconds=static_cast<float>(line.data.size()/sr);
                line.gain=std::pow(10.f,-3.f*delaySeconds/std::max(.2f,derived.decay));
            }
        }
        const float engage=(c.enabled && !c.bypass) ? 1.f : 0.f;
        historyGate+=static_cast<float>(1.0-std::exp(-1.0/(.002*sr)))*(engage-historyGate);
        // The wet/dry control fades the direct path; the DSP wet return stays unchanged.
        const float input[2]={left,right};
        float wet[2]{};
        phase+=static_cast<float>(std::clamp(bpm,20.0,300.0)/(60.0*sr));
        if(phase>1024.f) phase-=1024.f;
        const float wow=std::sin(phase*2*pi*(.5f + (preset.flavor%5)*.125f));
        const float crossPhase=std::sin(phase*2*pi*(.25f + (preset.flavor%3)*.25f));
        const float chorusCharacter=c.fxMode[4]==0?.65f:(c.fxMode[4]==1?1.2f:(c.fxMode[4]==2?1.65f:.9f));
        const float chorusAmount=c.fxOn[4] ? (.25f+.95f*clamp01(c.fxLevel[4]))*chorusCharacter : 0.f;
        const float delayMotion=derived.delayFrames * (.015f + .06f*derived.smear)*wow*chorusAmount;
        // Read delays BEFORE writing; stereo cross-feedback for spatial motion.
        float taps[2]{};
        for(int ch=0;ch<2;++ch) {
            auto& s=channels[ch];
            const float skew=ch ? 1.0f+0.15f*derived.width : 1.f;
            const float division=c.fxMode[1]==1 ? 1.5f : (c.fxMode[1]==3 ? .5f : 1.f);
            const float len=std::clamp(derived.delayFrames*skew*division + delayMotion*(ch? -1.f:1.f), 2.f,
                                  static_cast<float>(s.delay.size()-3));
            taps[ch]=c.fxOn[1] ? readDelay(s,len) : 0.f;
        }
        // No HP/LP band shaping: delay captures the original full-band signal.
        // Warm feedback saturation prevents uncontrolled echo buildup.
        float effectInput[2]={input[0],input[1]};
        for(int ch=0;ch<2;++ch) {
            auto& s=channels[ch];
            const int fbChannel=(c.fxMode[1]==2 || (c.fxMode[1]==0 && preset.flavor%3==0)) ? 1-ch : ch;
            const float fb=c.fxOn[1] ? taps[fbChannel]*derived.feedback : 0.f;
            writeDelay(s,std::clamp(saturate(input[ch]*.84f+fb),-1.f,1.f));
            // A little immediate character plus the full musical echo return.
            // The dry input itself will be added separately at unity gain.
            const float motion=c.fxOn[4] ? chorusAmount*(input[1-ch]-input[ch])*.15f : 0.f;
            wet[ch]=input[ch]*.11f + motion + taps[ch]*(.73f+derived.feedback*.36f);
        }
        const auto cat = preset.category;
        if(cat==Category::Hardcore && c.fxOn[3]) {
            for(int ch=0;ch<2;++ch) {
                auto& s=channels[ch];
                const float raw=effectInput[ch] * (1.5f+derived.drive*23.f);
                float distorted;
                switch(c.fxMode[3]) {
                    case 1: distorted=saturate(raw*.72f + .27f*std::sin(raw));break; // tape
                    case 2: distorted=std::sin(std::clamp(raw,-10.f,10.f)*1.7f);break; // fold
                    case 3: distorted=std::round(saturate(raw)*16.f)/16.f;break; // crush
                    default: distorted=(preset.flavor%2==0)?saturate(raw):std::sin(std::clamp(raw,-10.f,10.f)*1.7f);break;
                }
                const int bits=std::clamp(static_cast<int>(14.f-10.f*derived.crush),3,16);
                const float levels=static_cast<float>(1<<bits);
                const int hold=1+static_cast<int>(derived.crush*derived.crush*38.f);
                if(s.crushCounter++>=hold) {s.crushCounter=0;s.crushValue=std::round(distorted*levels)/levels;}
                // DC blocker after nonlinear stage
                float noDC=s.crushValue-s.prevDC+.996f*s.dc;
                s.prevDC=s.crushValue;s.dc=noDC;
                wet[ch] = .79f*noDC + .21f*wet[ch];
            }
        } else if(cat==Category::Stutter && c.fxOn[5]) {
            const float frames=std::clamp(static_cast<float>(sr*60.0/std::clamp(bpm,20.0,300.0))*derived.rate,
                                         48.f, static_cast<float>(channels[0].capture.size()-5));
            if(stutterClock++ >= frames) {
                stutterClock=0; ++stutterLoop;
                if(stutterLoop%std::max(1,1+int(derived.smear*4))==0)
                    for(int ch=0;ch<2;++ch) {
                        const auto& s=channels[ch];
                        stutterStart[ch]=(s.captureIndex + s.capture.size() - static_cast<size_t>(frames))%s.capture.size();
                    }
            }
            for(int ch=0;ch<2;++ch) {
                auto& s=channels[ch];
                const int n=std::max(2,static_cast<int>(frames));
                const int idx=std::clamp(static_cast<int>(stutterClock),0,n-1);
                const int reverse=(c.fxMode[5]==1 || (c.fxMode[5]==0 && preset.flavor%3==0)) ? n-1-idx : idx;
                const auto pos=(stutterStart[ch]+static_cast<size_t>(reverse))%s.capture.size();
                const float slice=s.capture[pos];
                const float e=std::min({1.f,idx/(.0035f*static_cast<float>(sr)+1.f),
                                         (n-1-idx)/(.0035f*static_cast<float>(sr)+1.f)});
                const float repeatPos=float(idx)/float(n);
                const float gateProgress=(preset.flavor%2==0)
                    ? repeatPos : std::fmod(repeatPos*2.f,1.f);
                const float duck=(gateProgress>derived.gate && c.fxMode[5]!=2) ? 0.f : 1.f;
                wet[ch]=slice*e*duck*(.72f+clamp01(c.fxLevel[5])*.95f)
                       + wet[ch]*derived.smear*.42f;
            }
        } else if(cat==Category::Transition) {
            // Real musical up/downlifter, not a white-noise bed.
            // Two overlapping 120 ms pitch grains read the captured INPUT signal.
            // Continuous Hann crossfades remove the discontinuity at grain resets.
            // Each transition preset selects lift/drop direction and a unique contour.
            const int localIndex=std::clamp(c.presetIndex-25,0,19);
            const bool down=(localIndex%4==1 || localIndex%4==3);
            const float sweep=clamp01(.14f+.86f*(.56f*c.x+.44f*c.y));
            const float contour=localIndex%5==0 ? sweep*sweep :
                                (localIndex%5==1 ? std::sqrt(sweep) :
                                (localIndex%5==2 ? sweep*sweep*(3.f-2.f*sweep) : sweep));
            const float roleRise=roleAmount(c,EffectRole::Riser);
            const float rolePitch=roleAmount(c,EffectRole::PitchCloud);
            const float roleImpact=roleAmount(c,EffectRole::Impact);
            const float padPower=clamp01(std::max(c.x,c.y));
            const float pitchSpan=.45f+.70f*clamp01(.5f*roleRise+.7f*rolePitch+.45f*padPower);
            // Up: about -4 to +12 semitones; down: +7 to -12 as XY rises.
            const float semitones=down ? (7.f-20.f*contour)*pitchSpan : (-4.f+17.f*contour)*pitchSpan;
            const float pitchRatio=std::clamp(std::pow(2.f,semitones/12.f),.42f,2.35f);
            const float grainFrames=float(sr)*(.085f+.055f*preset.tail);
            pitchGrainPhase+=1.f/std::max(1.f,grainFrames);
            if(pitchGrainPhase>=1.f)pitchGrainPhase-=1.f;
            const float grain0=pitchGrainPhase;
            const float grain1=std::fmod(grain0+.5f,1.f);
            const auto hann=[](float ph) noexcept {
                const float s=std::sin(pi*ph);return s*s;
            };
            const float w0=hann(grain0),w1=hann(grain1);
            const float pitchAmount=clamp01(.24f+.65f*padPower+.28f*roleImpact);
            const float pitchMotion=down ? (1.f-contour) : contour;
            const float sweepAmp=.52f+.38f*pitchMotion;
            for(int ch=0;ch<2;++ch) {
                const auto& capture=channels[ch];
                const float offset=float(sr)*.010f;
                const float src0=readCapture(capture,offset+grainFrames*pitchRatio*(1.f-grain0));
                const float src1=readCapture(capture,offset+grainFrames*pitchRatio*(1.f-grain1));
                const float pitched=(src0*w0+src1*w1)/std::max(.5f,w0+w1);
                // The original programme remains audible: pitched input and
                // tempo echoes constitute the lift, with a long FDN space tail.
                const float movement=std::sin(phase*2.f*pi*(.5f+float(localIndex%4)*.25f));
                const float swirl=(.7f+.3f*movement)*sweepAmp;
                const float transitionBody=effectInput[ch]*(.34f+.20f*roleImpact)+taps[ch]*(.20f+.24f*roleRise);
                const float signal=transitionBody+pitched*pitchAmount*swirl*(1.f+.25f*contour);
                wet[ch]=std::clamp(saturate(signal*1.55f),-1.1f,1.1f);
            }
        } else if(cat==Category::Chaos) {
            const float gain=1.f+.5f*derived.drive;
            const float a=wet[0]*gain+crossPhase*.1f*taps[1];
            const float b=wet[1]*gain-crossPhase*.1f*taps[0];
            wet[0]=c.fxOn[3]?saturate(a):std::clamp(a,-1.4f,1.4f);
            wet[1]=c.fxOn[3]?saturate(b):std::clamp(b,-1.4f,1.4f);
        } else { // GENTLE: musical warm saturation + modulation
            const float a=wet[0]*(.8f+.15f*derived.drive)+taps[1]*.08f*wow;
            const float b=wet[1]*(.8f+.15f*derived.drive)-taps[0]*.08f*wow;
            wet[0]=c.fxOn[3]?saturate(a):a;
            wet[1]=c.fxOn[3]?saturate(b):b;
        }
        // Preset-specific saturation character; XY routes these hidden engines.
        if(c.fxOn[3] && cat!=Category::Hardcore && cat!=Category::Transition) {
            for(auto& signal:wet) {
                const float drive=1.f+derived.drive*(1.f+clamp01(c.fxLevel[3])*5.f);
                const float x=signal*drive;
                switch(c.fxMode[3]) {
                    case 1: signal=saturate(x*.72f+.28f*std::sin(x));break;
                    case 2: signal=std::sin(std::clamp(x,-9.f,9.f)*1.7f);break;
                    case 3: signal=std::round(saturate(x)*24.f)/24.f;break;
                    default: signal=saturate(x);break;
                }
            }
        }
        // Read an actual micro-grain from the circular capture buffer.
        // Windows-safe, bounded, no allocation; mode controls grain trajectory.
        if(c.fxOn[5] && cat!=Category::Stutter) {
            const float amount=clamp01(c.fxLevel[5]);
            const float grainLength=float(sr)*(.009f+.071f*amount);
            for(int ch=0;ch<2;++ch) {
                auto& s=channels[ch];
                const float variation = (c.fxMode[5]==2 ? std::sin(phase*31.f+ch*1.5f)
                                        : (c.fxMode[5]==1 ? -std::sin(phase*14.f+ch*1.5f)
                                                          : std::sin(phase*8.f+ch*1.5f)));
                const float variationDepth=c.fxMode[5]==3 ? .16f : .45f;
                const float offset=grainLength*(.55f+variationDepth*variation);
                const auto delay=size_t(std::clamp(offset,1.f,float(s.capture.size()-2)));
                const size_t index=(s.captureIndex+s.capture.size()-delay)%s.capture.size();
                const float grain=s.capture[index];
                wet[ch]+=grain*(.06f+.24f*amount)*(.5f+.5f*std::sin(phase*5.f));
            }
        }
        for(int ch=0;ch<2;++ch) {
            auto& s=channels[ch];
            s.capture[s.captureIndex]=input[ch];
            s.captureIndex=(s.captureIndex+1)%s.capture.size();
        }
        // All five categories have a true stereo diffusion + feedback delay network.
        const float reverbScale=c.fxOn[2] ? (c.fxMode[2]==1?1.12f:(c.fxMode[2]==3?.62f:1.f)) : 0.f;
        const float rvIn[2]={wet[0]*derived.reverb*reverbScale, wet[1]*derived.reverb*reverbScale};
        float rv[2]{};
        for(int ch=0;ch<2;++ch) {
            auto& s=channels[ch];
            float in=allpass(s.diff1,rvIn[ch],.68f);
            in=allpass(s.diff2,in,.57f);
            float d[4]{};
            for(int i=0;i<4;++i) d[i]=s.fdn[i].data[s.fdn[i].index];
            // Orthogonal Hadamard feedback matrix / 2 -> bounded energy.
            const float h[4]={(d[0]+d[1]+d[2]+d[3])*.5f,
                              (d[0]-d[1]+d[2]-d[3])*.5f,
                              (d[0]+d[1]-d[2]-d[3])*.5f,
                              (d[0]-d[1]-d[2]+d[3])*.5f};
            for(int i=0;i<4;++i) {
                auto& line=s.fdn[i];
                line.damp+=.4f*(h[i]-line.damp);
                line.data[line.index]=std::clamp(in*.21f + line.damp*line.gain,-2.f,2.f);
                line.index=(line.index+1)%line.data.size();
            }
            rv[ch]=(d[0]+d[1]-d[2]+d[3])*.34f;
        }
        const float reverbMix=c.fxOn[2]?derived.reverb:0.f;
        float result[2]={wet[0]*(1.f-reverbMix*.7f)+rv[0]*reverbMix,
                         wet[1]*(1.f-reverbMix*.7f)+rv[1]*reverbMix};
        const float mid=(result[0]+result[1])*.5f;
        const float side=(result[0]-result[1])*.5f*derived.width;
        result[0]=mid+side;result[1]=mid-side;
        // Custom parallel-to-wet mix, preserving the v1.5.0 sound at 0%.
        // 0%: original (unity) + full XY-effect return.
        // 100%: XY-effect return only (without an injected dry signal).
        // This is NOT the usual dry/wet crossfade: only the direct feed is
        // reduced, ensuring the effect remains strong at intermediate positions.
        // Smooth the host-automatable parameter per sample to prevent zipper noise.
        const float targetMix=clamp01(c.dryWet);
        const float mixAlpha=static_cast<float>(1.0-std::exp(-1.0/(.006*sr)));
        mixSmoothed += mixAlpha*(targetMix-mixSmoothed);
        // Snap at end stops: exact unity dry at 0%, absolutely no dry at 100%.
        if(std::abs(targetMix-mixSmoothed)<.00005f)mixSmoothed=targetMix;
        const float send=clamp01(derived.wet*historyGate);
        const float strength=(preset.category==Category::Gentle ? .52f : .82f);
        if(c.bypass) {left=input[0]; right=input[1];}
        else {
            const float dryGain=1.f-mixSmoothed;
            left=input[0]*dryGain+saturate(result[0]*1.15f)*send*strength;
            right=input[1]*dryGain+saturate(result[1]*1.15f)*send*strength;
        }
    }
private:
    struct Line {std::vector<float> data;size_t index=0;float damp=0, gain=.8f;};
    struct Channel {
        std::vector<float> delay,capture;
        size_t delayIndex=0,captureIndex=0;
        float noiseLow=0;
        float dc=0,prevDC=0;
        int crushCounter=0;float crushValue=0;
        std::array<Line,4> fdn{};Line diff1,diff2;
    };
    std::array<Channel,2> channels{};
    struct Derived {
        float delayFrames=1200, feedback=.4f, reverb=.4f,decay=3.f,drive=.3f,crush=.4f,wet=0,width=1,
              rate=.25f,gate=.65f,smear=.25f;
    } target,derived;
    double sr=44100;
    int controlClock=31;
    float phase=0,stutterClock=0,historyGate=0,mixSmoothed=0.f,pitchGrainPhase=0;
    uint32_t noiseState=0x12345ad1u;
    size_t stutterStart[2]{};
    int stutterLoop=0;
    static float readDelay(const Channel& s,float frames) noexcept {
        // Double precision is important near the wrap boundary: float(size - epsilon)
        // can round UP to exactly size, producing an out-of-bounds read on some hosts.
        const auto count=s.delay.size();
        if(count<4) return 0.f;
        const double n=static_cast<double>(count);
        const double span=std::isfinite(frames)
            ? std::clamp(static_cast<double>(frames),2.0,n-2.0) : 2.0;
        double at=static_cast<double>(s.delayIndex)-span;
        if(at<0.0) at+=n;
        if(at>=n) at-=n;
        const auto i=std::min(static_cast<size_t>(at),count-1);
        const float f=static_cast<float>(std::clamp(at-static_cast<double>(i),0.0,1.0));
        return s.delay[i]*(1.f-f)+s.delay[(i+1==count?0:i+1)]*f;
    }
    static float readCapture(const Channel& c,float age) noexcept {
        const auto count=c.capture.size();
        if(count<4)return 0.f;
        const double n=static_cast<double>(count);
        const double delay=std::clamp(static_cast<double>(age),2.0,n-2.0);
        double pos=static_cast<double>(c.captureIndex)-delay;
        if(pos<0.0)pos+=n;
        if(pos>=n)pos-=n;
        const size_t i=std::min(static_cast<size_t>(pos),count-1);
        const float fraction=static_cast<float>(pos-static_cast<double>(i));
        return c.capture[i]*(1.f-fraction)+c.capture[(i+1)%count]*fraction;
    }
    static void writeDelay(Channel& s,float sample) noexcept {
        s.delay[s.delayIndex]=sample;s.delayIndex=(s.delayIndex+1)%s.delay.size();
    }
    static float allpass(Line& a,float x,float gain) noexcept {
        const float buffer=a.data[a.index];
        const float y=buffer-gain*x;
        a.data[a.index]=x+gain*y;
        a.index=(a.index+1)%a.data.size();
        return y;
    }
    void makeTargets(const Controls& c,const Preset& p,double bpm) noexcept {
        const auto& recipe=profileFor(c.presetIndex);
        const float x=clamp01(c.x),y=clamp01(c.y);
        const auto v=[&](EffectRole role) noexcept {return roleAmount(c,role);};
        const float t=v(EffectRole::DelayTime), f=v(EffectRole::Feedback),r=v(EffectRole::Reverb),
                    pm=v(EffectRole::PitchMotion),d=v(EffectRole::Drive),cr=v(EffectRole::Crush),
                    w=v(EffectRole::Width),gr=v(EffectRole::Granular),ch=v(EffectRole::Chorus),
                    st=v(EffectRole::StutterRate),gt=v(EffectRole::Gate),de=v(EffectRole::Decay),
                    riser=v(EffectRole::Riser),im=v(EffectRole::Impact),
                    timbre=v(EffectRole::Timbre),ft=v(EffectRole::Flutter),
                    pc=v(EffectRole::PitchCloud),rg=v(EffectRole::ReverseGrain);
        const float beatSec=float(60./std::clamp(bpm,20.,300.));
        const float ratio=std::clamp(p.beats*(.18f+2.7f*t+1.2f*riser+.75f*rg+.28f*pm),.03125f,3.8f);
        target.delayFrames=std::clamp(float(sr)*beatSec*ratio,2.f,float(channels[0].delay.size()-3));
        target.feedback=std::clamp(p.feedback*(.36f+.52f*x)+.26f*f+.16f*rg+.18f*ft,0.f,.87f);
        target.reverb=std::clamp(p.reverb*(.25f+.51f*y)+.33f*r+.2f*de+.2f*pc,0.f,.94f);
        target.decay=std::clamp(.8f+8.f*(p.tail*.27f+de*.46f+pc*.36f+r*.18f),.4f,10.f);
        target.drive=clamp01(p.drive*.21f + .70f*d+.37f*im+.66f*cr+.21f*timbre);
        target.crush=clamp01(p.crush*.22f +cr*.73f +im*.14f);
        target.width=std::clamp(.80f+w*1.12f+ch*.43f+.34f*pc+.20f*y,.52f,1.96f);
        target.smear=clamp01(p.motion*.18f+gr*.62f+pc*.49f+rg*.46f+ft*.33f+pm*.34f);
        target.rate=std::clamp(p.beats*std::pow(2.f,3.f*(.6f-st)),.03125f,1.5f);
        target.gate=std::clamp(.22f+.68f*gt+.16f*rg,.17f,.94f);
        // The pad, and ONLY the pad, now controls the effect blend and engines.
        const float power=clamp01(.70f*std::max(x,y)+.30f*std::sqrt(x*y));
        target.wet=clamp01(std::pow(power,.73f)*(1.10f+.11f*y)*recipe.intensity);
        if(p.category==Category::Gentle) {
            target.feedback*=.85f; target.drive*=.45f;
        } else if(p.category==Category::Hardcore) {
            target.drive=clamp01(target.drive*1.3f+.14f*power);
            target.crush=clamp01(target.crush*1.2f);
        } else if(p.category==Category::Stutter) {
            target.reverb*=.50f;
        } else if(p.category==Category::Transition) {
            target.reverb=std::clamp(target.reverb+.18f*power,0.f,.97f);
            target.smear=clamp01(target.smear+.24f*power);
            target.delayFrames=std::clamp(target.delayFrames*(1.f+.42f*riser),2.f,
                                          float(channels[0].delay.size()-3));
        }
        if(!c.fxOn[1]) target.feedback=0.f;
        if(!c.fxOn[2]) target.reverb=0.f;
        if(!c.fxOn[3]) {target.drive=0.f;target.crush=0.f;}
        if(!c.fxOn[4]) target.width=1.f;
        if(!c.fxOn[5]) target.smear=0.f;
    }
};
} // namespace gsamove
