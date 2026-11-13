package audio.gsa.fieldforce.core;

public final class Pattern {
    public static final int PADS=16, STEPS=16;
    private final boolean[][] hits = new boolean[PADS][STEPS];
    public volatile int bpm=120, swing=0;
    public volatile boolean metronome=false;
    public synchronized void toggle(int row,int step) { if(valid(row,step))hits[row][step]=!hits[row][step]; }
    public synchronized void set(int row,int step,boolean on) {if(valid(row,step))hits[row][step]=on;}
    public synchronized boolean get(int row,int step) {return valid(row,step)&&hits[row][step];}
    public synchronized void clear() {for(int p=0;p<PADS;p++)for(int s=0;s<STEPS;s++)hits[p][s]=false;}
    private static boolean valid(int row,int step){return row>=0&&row<PADS&&step>=0&&step<STEPS;}
    public synchronized String encode() {
        StringBuilder s=new StringBuilder(PADS*STEPS);
        for(int p=0;p<PADS;p++)for(int t=0;t<STEPS;t++)s.append(hits[p][t]?'1':'0');
        return s.toString();
    }
    public synchronized void decode(String s) {
        clear();if(s==null||s.length()!=PADS*STEPS)return;
        for(int p=0;p<PADS;p++)for(int t=0;t<STEPS;t++)hits[p][t]=s.charAt(p*STEPS+t)=='1';
    }
    public synchronized int count() {int n=0;for(int p=0;p<PADS;p++)for(int t=0;t<STEPS;t++)if(hits[p][t])n++;return n;}
}
