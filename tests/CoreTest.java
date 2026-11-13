import audio.gsa.fieldforce.core.*;
import java.io.*;
import java.util.*;

public final class CoreTest {
    private static void ok(boolean b,String msg){if(!b)throw new AssertionError(msg);}
    public static void main(String[] args)throws Exception {
        PcmSample[] kit=KitFactory.build(0);
        ok(kit.length==16,"kit pads");
        for(PcmSample s:kit)ok(s.data.length>5000,"kit has audio");
        PcmSample s=kit[0];ok(s.reverse().data[0]==s.data[s.data.length-1],"reverse");
        ok(s.trim(.1f,.7f).data.length<s.data.length,"trim");
        ok(s.normalize().data.length==s.data.length,"normalize");
        ok(s.fade().data.length==s.data.length,"fade");
        ByteArrayOutputStream wav=new ByteArrayOutputStream();
        WavIO.write(wav,s.data,1,PcmSample.RATE);
        ok(new String(Arrays.copyOf(wav.toByteArray(),4),"US-ASCII").equals("RIFF"),"wav header");
        PcmSample decoded=WavIO.read(new ByteArrayInputStream(wav.toByteArray()),"decoded");
        ok(decoded.data.length==s.data.length,"wav round trip samples");
        for(int i=0;i<Math.min(1000,s.data.length);i++)ok(Math.abs(s.data[i]-decoded.data[i])<.0001,"wav PCM16 accuracy");
        Pattern p=new Pattern();p.bpm=133;p.swing=25;p.metronome=true;p.set(0,0,true);p.set(9,15,true);
        ok(p.count()==2,"pattern count");String steps=p.encode();Pattern p2=new Pattern();p2.decode(steps);
        ok(p2.count()==2&&p2.get(9,15),"pattern roundtrip");
        SequencerClock clock=new SequencerClock();int onset=0;int last=-1;
        for(int i=0;i<44100*3;i++){int n=clock.tick(120,0);if(n>=0){onset++;last=n;}}
        ok(onset==24,"sequencer quantization 120bpm, 16th notes 3 seconds -> 24 onset events: "+onset);
        float[] out=new float[2];for(int i=0;i<AudioFx.PRESETS.length;i++){
            AudioFx fx=new AudioFx();fx.setPreset(i);double change=0;
            for(int j=0;j<15000;j++){float a=(float)(Math.sin(j*.038)*.35);fx.process(a,a,out);
                ok(Float.isFinite(out[0])&&Float.isFinite(out[1]),"FX finite output");change+=Math.abs(out[0]-a);
            }
            ok(change>1,"FX preset audible " + i);
        }
        ProjectCodec.Data project=new ProjectCodec.Data();project.pads[0]=kit[0];project.pads[1]=kit[1];
        project.pattern=p;project.preset=3;project.skin=8;project.padFx[2]=true;project.padMute[5]=true;project.globalFx=false;
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();ProjectCodec.save(bytes,project);
        ProjectCodec.Data restored=ProjectCodec.load(new ByteArrayInputStream(bytes.toByteArray()));
        ok(restored.pads[0].data.length==kit[0].data.length,"embedded project sample");
        ok(restored.pads[2]==null,"project empty pad");
        ok(restored.pattern.count()==2&&restored.pattern.bpm==133&&restored.pattern.swing==25,"project seq state");
        ok(restored.preset==3&&restored.skin==8,"project fx and skin");
        ok(restored.padFx[2]&&!restored.padFx[1]&&restored.padMute[5]&&!restored.globalFx,"per-pad FX, mute and global FX project persistence");
        System.out.println("PASS: 16 starter pads, WAV import/export, sample edits, 16x16 sequence, BPM clock, " + AudioFx.PRESETS.length+" FX presets, embedded project roundtrip");
    }
}
