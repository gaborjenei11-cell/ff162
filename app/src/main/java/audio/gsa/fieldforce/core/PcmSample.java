package audio.gsa.fieldforce.core;

import java.util.Arrays;

/** Immutable mono 44.1 kHz sample; convenient for field recordings and pad-triggered playback. */
public final class PcmSample {
    public static final int RATE = 44100;
    public final String name;
    public final float[] data;

    public PcmSample(String name, float[] mono) {
        this.name = name == null ? "Untitled" : name;
        this.data = mono == null ? new float[0] : mono.clone();
    }
    public double seconds() { return data.length / (double) RATE; }
    public PcmSample renamed(String title) { return new PcmSample(title, data); }
    public PcmSample reverse() {
        float[] copy = data.clone();
        for (int i=0, j=copy.length-1;i<j;i++,j--) { float v=copy[i];copy[i]=copy[j];copy[j]=v; }
        return new PcmSample(name+" REV",copy);
    }
    public PcmSample normalize() {
        float peak = 0f;
        for(float v:data) peak=Math.max(peak,Math.abs(v));
        float gain=peak>0.00001f? .94f/peak:1f;
        float[] out=new float[data.length];
        for(int i=0;i<out.length;i++)out[i]=data[i]*gain;
        return new PcmSample(name+" NORM",out);
    }
    public PcmSample fade() {
        float[] out = data.clone();
        int fade = Math.min(out.length/3, RATE/40);
        for(int i=0;i<fade;i++) {
            float a = i/(float)Math.max(1,fade);
            out[i]*=a;out[out.length-1-i]*=a;
        }
        return new PcmSample(name+" FADE",out);
    }
    public PcmSample trim(float start, float end) {
        int a=(int)(Math.max(0,Math.min(1,start))*data.length);
        int b=(int)(Math.max(0,Math.min(1,end))*data.length);
        if(b<=a)return this;
        return new PcmSample(name+" CUT", Arrays.copyOfRange(data,a,b));
    }
    public static PcmSample resample(String name, float[] in, int rate) {
        if(rate<=0 || rate>384000 || in.length==0) return new PcmSample(name,new float[0]);
        int count = (int)Math.min(60L * RATE, Math.round(in.length*(double)RATE/rate));
        float[] out=new float[count];
        for(int i=0;i<count;i++) {
            double at = i*(double)rate/RATE;
            int idx=Math.min(in.length-1,(int)at);
            int next=Math.min(in.length-1,idx+1);
            double t=at-idx;
            out[i]=(float)(in[idx]*(1-t)+in[next]*t);
        }
        return new PcmSample(name,out);
    }
}
