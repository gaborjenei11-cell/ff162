package audio.gsa.fieldforce.core;

/** Streaming stereo send FX. All DSP state is preallocated; no allocations per audio frame. */
public final class AudioFx {
    public static final String[] PRESETS={"CLEAN ROOM","TAPE STREET","DUB CHAMBER","METAL CRUSH",
        "GLASS ECHO","UNDERWATER","SATELLITE","DARK PLATE","STUTTER DUST","BIG SPACE", "NEON DRIVE", "BROKEN RADIO"};
    // MIX, DRIVE, ECHO, REVERB, FILTER, CRUSH, CHORUS, STEREO
    private static final int[][] VALUES={
        {10,0,0,12,100,0,0,50},{47,24,30,20,70,2,14,50},
        {70,13,72,63,82,4,20,62},{76,91,18,17,100,90,0,55},
        {63,5,68,46,100,0,38,75},{70,9,28,57,18,0,30,54},
        {72,33,54,64,100,22,65,80},{59,12,30,83,76,4,28,70},
        {66,51,49,36,72,65,10,62},{79,18,69,91,94,0,50,80},
        {66,82,26,42,100,8,44,66},{70,65,20,25,39,78,2,50}
    };
    private final float[] delayL=new float[44100*2],delayR=new float[44100*2];
    private final float[] verbL=new float[19001],verbR=new float[22703];
    private final float[] chorusL=new float[4096], chorusR=new float[4096];
    private int dIndex=0, vLIndex=0, vRIndex=0, cIndex=0, crushPos=0;
    private double phase=0;
    private float lpL=0,lpR=0,bitL=0,bitR=0;
    private volatile int mix=0,drive=0,echo=0,reverb=0,filter=100,crush=0,chorus=0,stereo=50;
    private volatile boolean enabled=true;

    public void setPreset(int preset) {
        if(preset<0||preset>=VALUES.length)return;
        int[] v=VALUES[preset];
        set(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7]);
    }
    public synchronized void set(int mix,int drive,int echo,int reverb,int filter,int crush,int chorus,int stereo) {
        this.mix=cap(mix);this.drive=cap(drive);this.echo=cap(echo);this.reverb=cap(reverb);
        this.filter=cap(filter);this.crush=cap(crush);this.chorus=cap(chorus);this.stereo=cap(stereo);
    }
    private static int cap(int n){return Math.max(0,Math.min(100,n));}
    public synchronized int[] settings(){return new int[]{mix,drive,echo,reverb,filter,crush,chorus,stereo};}
    public void setEnabled(boolean enabled){this.enabled=enabled;}
    public boolean isEnabled(){return enabled;}
    public void process(float left,float right,float[] out) {
        if(!enabled) {out[0]=left;out[1]=right;return;}
        final float wet=mix*.01f;
        final float gain=1f+drive*.095f;
        float l=(float)Math.tanh(left*gain),r=(float)Math.tanh(right*gain);
        // Filter is a tonal FX only, not an input attenuation stage.
        float alpha=(float)(.006+filter*.00027);
        lpL+=(l-lpL)*alpha;lpR+=(r-lpR)*alpha;
        float color=filter*.01f;
        l=l*(color*.65f+.2f)+lpL*(.8f-color*.65f);
        r=r*(color*.65f+.2f)+lpR*(.8f-color*.65f);
        int hold=1+crush/8;
        if(crushPos++%hold==0) {
            int steps=Math.max(8,256-crush*2);
            bitL=Math.round(l*steps)/(float)steps;bitR=Math.round(r*steps)/(float)steps;
        }
        l=l*(1-crush*.01f)+bitL*(crush*.01f);
        r=r*(1-crush*.01f)+bitR*(crush*.01f);
        phase+=2*Math.PI*(.1+chorus*.016)/PcmSample.RATE;
        if(phase>Math.PI*2)phase-=Math.PI*2;
        int cOffset=180+(int)(130*Math.sin(phase));
        int cAt=(cIndex-240-cOffset+chorusL.length)%chorusL.length;
        l+=chorusL[cAt]*chorus*.003f;
        r+=chorusR[cAt]*chorus*.003f;
        chorusL[cIndex]=l;chorusR[cIndex]=r;
        cIndex=(cIndex+1)%chorusL.length;
        int delayAt=(dIndex-11025+delayL.length)%delayL.length;
        float dl=delayL[delayAt],dr=delayR[delayAt];
        float fb=.12f+echo*.0066f;
        delayL[dIndex]=Math.max(-2,Math.min(2,l*.5f+dr*fb));
        delayR[dIndex]=Math.max(-2,Math.min(2,r*.5f+dl*fb));
        dIndex=(dIndex+1)%delayL.length;
        l+=dl*echo*.010f; r+=dr*echo*.010f;
        float vl=verbL[vLIndex],vr=verbR[vRIndex];
        verbL[vLIndex]=Math.max(-2,Math.min(2,l*.31f+vl*(.44f+reverb*.004f)+vr*.14f));
        verbR[vRIndex]=Math.max(-2,Math.min(2,r*.31f+vr*(.44f+reverb*.004f)+vl*.14f));
        vLIndex=(vLIndex+1)%verbL.length;vRIndex=(vRIndex+1)%verbR.length;
        l+=vl*reverb*.012f;r+=vr*reverb*.012f;
        float m=(l+r)*.5f,side=(l-r)*(.5f+stereo*.01f);
        l=m+side;r=m-side;
        // Wet control is crossfade, master limiter prevents accidental overs.
        out[0]=(float)Math.tanh(left*(1-wet)+l*wet);
        out[1]=(float)Math.tanh(right*(1-wet)+r*wet);
    }
}
