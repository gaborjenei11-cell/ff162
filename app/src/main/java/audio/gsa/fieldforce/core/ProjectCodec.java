package audio.gsa.fieldforce.core;

import java.io.*;
import java.util.zip.*;

/** Portable project format: embedded PCM samples + step data; no external file references. */
public final class ProjectCodec {
    public static final String MAGIC="GSA_FIELDFORCE_PROJECT_V2";
    private ProjectCodec(){}
    public static final class Data {
        public PcmSample[] pads=new PcmSample[16];
        public Pattern pattern=new Pattern();
        public int preset=0,skin=0;public boolean globalFx=true;public boolean[] padFx=new boolean[16],padMute=new boolean[16];
    }
    public static void save(OutputStream output,Data state)throws IOException {
        DataOutputStream o=new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(output)));
        o.writeUTF(MAGIC);o.writeInt(state.pattern.bpm);o.writeInt(state.pattern.swing);o.writeBoolean(state.pattern.metronome);
        o.writeUTF(state.pattern.encode());o.writeInt(state.preset);o.writeInt(state.skin);
        for(int i=0;i<16;i++){
            PcmSample sample=state.pads[i];o.writeBoolean(sample!=null);
            if(sample!=null){o.writeUTF(sample.name.substring(0,Math.min(100,sample.name.length())));
                if(sample.data.length>60*PcmSample.RATE)throw new IOException("Sample too long");
                o.writeInt(sample.data.length);
                for(float f:sample.data)o.writeShort(Math.round(Math.max(-1,Math.min(1,f))*32767));
            }
        }
        o.writeBoolean(state.globalFx);for(int i=0;i<16;i++){o.writeBoolean(state.padFx[i]);o.writeBoolean(state.padMute[i]);}
        o.close();
    }
    public static Data load(InputStream stream)throws IOException {
        DataInputStream in=new DataInputStream(new BufferedInputStream(new GZIPInputStream(stream)));
        String magic=in.readUTF();boolean v2=MAGIC.equals(magic);if(!v2&&!"GSA_FIELDFORCE_PROJECT_V1".equals(magic))throw new IOException("Invalid FieldForce project");
        Data d=new Data();d.pattern.bpm=Math.max(40,Math.min(240,in.readInt()));
        d.pattern.swing=Math.max(0,Math.min(75,in.readInt()));d.pattern.metronome=in.readBoolean();
        String pattern=in.readUTF();if(pattern.length()!=256)throw new IOException("Invalid pattern");d.pattern.decode(pattern);
        d.preset=Math.max(0,Math.min(AudioFx.PRESETS.length-1,in.readInt()));d.skin=Math.max(0,Math.min(9,in.readInt()));
        for(int i=0;i<16;i++)if(in.readBoolean()){
            String name=in.readUTF();int n=in.readInt();if(n<0||n>60*PcmSample.RATE)throw new IOException("Invalid sample size");
            float[] audio=new float[n];for(int j=0;j<n;j++)audio[j]=in.readShort()/32768f;d.pads[i]=new PcmSample(name,audio);
        }
        if(v2){d.globalFx=in.readBoolean();for(int i=0;i<16;i++){d.padFx[i]=in.readBoolean();d.padMute[i]=in.readBoolean();}}
        return d;
    }
}
