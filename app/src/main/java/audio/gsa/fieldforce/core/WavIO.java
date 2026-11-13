package audio.gsa.fieldforce.core;

import java.io.*;

/** Standard PCM WAV reader/writer without Android dependencies. */
public final class WavIO {
    private WavIO() {}
    private static void put16(OutputStream o,int n)throws IOException {o.write(n&255);o.write((n>>>8)&255);}
    private static void put32(OutputStream o,long n)throws IOException {put16(o,(int)n);put16(o,(int)(n>>>16));}
    private static int u16(byte[] b,int i){return (b[i]&255)|((b[i+1]&255)<<8);}
    private static int i32(byte[] b,int i){return (b[i]&255)|((b[i+1]&255)<<8)|((b[i+2]&255)<<16)|((b[i+3]&255)<<24);}
    private static boolean has(byte[] d,int off,String tag){if(off<0||off+tag.length()>d.length)return false;for(int i=0;i<tag.length();i++)if(d[off+i]!=(byte)tag.charAt(i))return false;return true;}
    public static void write(OutputStream o,float[] interleaved,int channels,int rate)throws IOException {
        if(channels<1||channels>2||rate<=0||interleaved.length%channels!=0)throw new IOException("Invalid WAV data");
        int bytes=interleaved.length*2;
        o.write(new byte[]{'R','I','F','F'});put32(o,36L+bytes);o.write(new byte[]{'W','A','V','E','f','m','t',' '});
        put32(o,16);put16(o,1);put16(o,channels);put32(o,rate);put32(o,rate*channels*2);put16(o,channels*2);put16(o,16);
        o.write(new byte[]{'d','a','t','a'});put32(o,bytes);
        for(float v:interleaved){int s=Math.round(Math.max(-1f,Math.min(1f,v))*32767);put16(o,s);}
    }
    public static PcmSample read(InputStream in,String name)throws IOException {
        ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] t=new byte[8192];int k;
        while((k=in.read(t))!=-1){if(b.size()+k>65_000_000)throw new IOException("WAV exceeds 65 MB limit");b.write(t,0,k);}
        byte[] d=b.toByteArray();if(d.length<44||!has(d,0,"RIFF")||!has(d,8,"WAVE"))throw new IOException("Not a WAV file");
        int channels=0,rate=0,bits=0,format=0,off=-1,size=0;
        for(int p=12;p+8<=d.length;){int n=i32(d,p+4);if(n<0||p+8L+n>d.length)break;
            if(has(d,p,"fmt ")&&n>=16){format=u16(d,p+8);channels=u16(d,p+10);rate=i32(d,p+12);bits=u16(d,p+22);}
            else if(has(d,p,"data")){off=p+8;size=n;}
            p+=8+n+(n&1);
        }
        if(format!=1||bits!=16||channels<1||channels>2||rate<8000||rate>192000||off<0)throw new IOException("Only PCM16 mono/stereo WAV supported by fallback reader");
        int frames=Math.min(size/(channels*2),60*rate);float[] samples=new float[frames];
        for(int i=0;i<frames;i++){float sum=0;for(int ch=0;ch<channels;ch++){int v=u16(d,off+(i*channels+ch)*2);sum+=(short)v/32768f;}samples[i]=sum/channels;}
        return PcmSample.resample(name,samples,rate);
    }
}
