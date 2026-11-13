package audio.gsa.fieldforce;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;
import android.util.Log;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;
import audio.gsa.fieldforce.core.*;

/** AudioTrack-based pad performance and 16x16 sequencer, 44.1 kHz stereo. */
public final class AudioEngine {
    public interface PositionListener { void onStep(int step); }
    public final AtomicReferenceArray<PcmSample> pads=new AtomicReferenceArray<>(16);
    public final Pattern pattern=new Pattern();
    public final AudioFx fx=new AudioFx();
    public final MoveFx move=new MoveFx();
    public volatile boolean playing=false,recordNotes=false;
    public volatile int currentStep=0;
    private final boolean[] padFx=new boolean[16],padMute=new boolean[16];
    public volatile boolean globalFx=true;
    public boolean padFx(int i){return padFx[i];}
    public void setPadFx(int i,boolean v){padFx[i]=v;}
    public boolean padMute(int i){return padMute[i];}
    public void setPadMute(int i,boolean v){padMute[i]=v;}
    public boolean anyPadFx(){for(boolean b:padFx)if(b)return true;return false;}
    public void resetPadRouting(){for(int i=0;i<16;i++){padFx[i]=false;padMute[i]=false;}globalFx=true;}

    private volatile boolean alive=true;
    private final ConcurrentLinkedQueue<Integer> hits=new ConcurrentLinkedQueue<>();
    private final ArrayList<Voice> voices=new ArrayList<>();
    private final SequencerClock clock=new SequencerClock();
    private final Thread thread;
    private volatile PositionListener listener;
    private static final int FRAMES=256;
    private static final class Voice { final PcmSample s;final int pad;int i=0; Voice(PcmSample sample,int pad){s=sample;this.pad=pad;} }
    public AudioEngine(){
        PcmSample[] bank=KitFactory.build(0);for(int i=0;i<16;i++)pads.set(i,bank[i]);
        fx.setPreset(0);
        thread=new Thread(this::run,"FieldForce Audio Engine");thread.start();
    }
    public void setPositionListener(PositionListener p){listener=p;}
    public void trigger(int pad){if(pad>=0&&pad<16){hits.offer(pad);if(playing&&recordNotes)pattern.set(pad,currentStep,true);}}
    public void play(){clock.reset();currentStep=0;playing=true;}
    public void stop(){playing=false;currentStep=0;}
    public void loadKit(int id){PcmSample[] kit=KitFactory.build(id);for(int i=0;i<16;i++)pads.set(i,kit[i]);}
    public void close(){alive=false;playing=false;thread.interrupt();try{thread.join(500);}catch(InterruptedException e){Thread.currentThread().interrupt();}}
    public float[] renderBars(int bars){
        bars=Math.max(1,Math.min(16,bars));
        int bpm=pattern.bpm,rate=PcmSample.RATE;
        int frames=(int)Math.min(rate*120L,Math.ceil(bars*4d*60d/bpm*rate));
        // Export uses a fresh, independent FX state, so live playback is untouched.
        AudioFx reverb=new AudioFx();int[] v=fx.settings();reverb.set(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7]);reverb.setEnabled(fx.isEnabled());
        ArrayList<Voice> active=new ArrayList<>();SequencerClock seq=new SequencerClock();
        float[] out=new float[frames*2], processed=new float[2];
        for(int f=0;f<frames;f++){
            int step=seq.tick(bpm,pattern.swing);
            if(step!=-1)for(int row=0;row<16;row++)if(pattern.get(row,step)&&!padMute[row]){PcmSample s=pads.get(row);if(s!=null&&s.data.length>0)active.add(new Voice(s,row));}
            float dry=0,send=0;
            for(int j=active.size()-1;j>=0;j--){Voice voice=active.get(j);if(voice.i>=voice.s.data.length){active.remove(j);continue;}float sample=voice.s.data[voice.i++]*.55f;if(globalFx&&padFx[voice.pad])send+=sample;else dry+=sample;}
            if(globalFx){reverb.process(send,send,processed);if(move.enabled())move.process(processed[0],processed[1],processed);}else{processed[0]=0;processed[1]=0;}
            out[f*2]=Math.max(-1,Math.min(1,dry+processed[0]));out[f*2+1]=Math.max(-1,Math.min(1,dry+processed[1]));
        }
        return out;
    }
    private void run(){
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
        int min=AudioTrack.getMinBufferSize(PcmSample.RATE,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT);
        if(min<=0)min=FRAMES*8;
        AudioTrack track=null;
        try{
            track=new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(new AudioFormat.Builder().setSampleRate(PcmSample.RATE).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
                .setTransferMode(AudioTrack.MODE_STREAM).setBufferSizeInBytes(Math.max(min,FRAMES*8)).build();
            track.play();short[] block=new short[FRAMES*2];float[] processed=new float[2];
            double clickPhase=0;float clickEnv=0;
            while(alive){
                for(int f=0;f<FRAMES;f++){
                    Integer pad;
                    while((pad=hits.poll())!=null){PcmSample s=pads.get(pad);if(!padMute[pad]&&s!=null&&s.data.length>0){if(voices.size()>=48)voices.remove(0);voices.add(new Voice(s,pad));}}
                    if(playing){int step=clock.tick(pattern.bpm,pattern.swing);if(step!=-1){currentStep=step;
                        PositionListener l=listener;if(l!=null)l.onStep(step);
                        for(int row=0;row<16;row++)if(pattern.get(row,step)&&!padMute[row]){PcmSample s=pads.get(row);if(s!=null&&s.data.length>0){if(voices.size()>=48)voices.remove(0);voices.add(new Voice(s,row));}}
                        if(pattern.metronome&&step%4==0){clickEnv=.65f;clickPhase=0;}
                    }}
                    float dry=0,send=0;
                    for(int i=voices.size()-1;i>=0;i--){Voice v=voices.get(i);if(v.i>=v.s.data.length){voices.remove(i);continue;}float sample=v.s.data[v.i++]*.55f;if(globalFx&&padFx[v.pad])send+=sample;else dry+=sample;}
                    if(clickEnv>.0001f){dry+=(float)(.33*Math.sin(clickPhase)*clickEnv);clickPhase+=2*Math.PI*1150/PcmSample.RATE;clickEnv*=.996f;}
                    if(globalFx){fx.process(send,send,processed);if(move.enabled())move.process(processed[0],processed[1],processed);}else{processed[0]=0;processed[1]=0;}
                    block[f*2]=toShort(dry+processed[0]);block[f*2+1]=toShort(dry+processed[1]);
                }
                int offset=0;while(alive&&offset<block.length){int written=track.write(block,offset,block.length-offset,AudioTrack.WRITE_BLOCKING);if(written<=0)break;offset+=written;}
            }
        }catch(Exception e){Log.e("FieldForce","Audio output failed",e);}
        finally{if(track!=null){try{track.stop();}catch(Exception ignored){}track.release();}}
    }
    private static short toShort(float f){return (short)Math.round(Math.max(-1,Math.min(1,f))*32767);}
}
