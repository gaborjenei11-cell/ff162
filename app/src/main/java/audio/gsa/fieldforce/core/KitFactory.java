package audio.gsa.fieldforce.core;

import java.util.Random;

/** Copyright-free, fully synthesised starter kits: app works with no preloaded sound files. */
public final class KitFactory {
    public static final String[] KITS={"FIELD DRUMS","NEON MACHINES","DUST & TAPE","DEEP SUB","GLASS MOTION"};
    private KitFactory() {}
    public static PcmSample[] build(int which) {
        PcmSample[] kit=new PcmSample[16];
        Random rnd=new Random(0xF13DL+which*911);
        for(int i=0;i<16;i++) {
            int family=i%8, n=(int)(PcmSample.RATE*(family==0?.72:family==1?.42:family==2?.20:.32));
            float[] wave=new float[n];
            double phase=0;
            double base=45+which*17+i*11;
            for(int j=0;j<n;j++) {
                float t=j/(float)PcmSample.RATE;
                double e=Math.exp(-t*(family==0?8:family==2?40:14));
                double noise=rnd.nextDouble()*2-1;
                double pitch=(family==0?72:base)*Math.exp(-t*(family==0?10:1.2))+(family==3?base*4:base);
                phase+=Math.PI*2*pitch/PcmSample.RATE;
                double signal;
                switch(family) {
                    case 0: signal=Math.sin(phase)*1.1;break;
                    case 1: signal=Math.sin(phase)*.35+noise*.70;break;
                    case 2: signal=noise*.68;break;
                    case 3: signal=Math.sin(phase)+.4*Math.sin(phase*2);break;
                    case 4: signal=Math.sin(phase*2)*.55+noise*.23;break;
                    case 5: signal=Math.sin(phase)*(Math.sin(t*30)+1)*.5;break;
                    case 6: signal=noise*.4+Math.sin(phase*3)*.3;break;
                    default: signal=Math.sin(phase)*.7+Math.sin(phase*1.5)*.2;
                }
                wave[j]=(float)Math.max(-1,Math.min(1,signal*e*.75));
            }
            kit[i]=new PcmSample(String.format(java.util.Locale.US,"%s %02d",KITS[Math.max(0,Math.min(4,which))],i+1),wave);
        }
        return kit;
    }
}
