package audio.gsa.fieldforce.core;

/** Sample-accurate 16-step clock with BPM and swing; returns a step on each onset. */
public final class SequencerClock {
    private double phase=0;
    private int step=0;
    private boolean first=true;
    public void reset(){phase=0;step=0;first=true;}
    public int step(){return step;}
    public int tick(int bpm,int swing){
        int current=step;
        if(first){first=false;return current;}
        double beat=60.0*PcmSample.RATE/Math.max(40,Math.min(240,bpm));
        double length=beat/4.0*(step%2==0?1+swing/100.0:1-swing/100.0);
        phase++;
        if(phase>=length){phase-=length;step=(step+1)&15;return step;}
        return -1;
    }
}
