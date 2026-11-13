package audio.gsa.fieldforce;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import java.io.*;
import java.nio.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import audio.gsa.fieldforce.core.*;

/** Android media bridge: microphone capture and WAV/MP3 sample decoding. */
public final class MediaSupport {
    private MediaSupport() {}
    public interface RecordingCallback { void completed(PcmSample recording, String error); }
    public static final class Recorder {
        private final AtomicBoolean active=new AtomicBoolean(false);
        private Thread worker;
        public boolean isRecording(){return active.get();}
        public boolean start(RecordingCallback cb){
            if(!active.compareAndSet(false,true))return false;
            worker=new Thread(()->capture(cb),"FieldForce Microphone");worker.start();return true;
        }
        public void stop(){active.set(false);}
        private void capture(RecordingCallback cb){
            AudioRecord recorder=null;
            String error=null;PcmSample sample=null;
            try{
                int min=AudioRecord.getMinBufferSize(PcmSample.RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
                if(min<0)throw new IOException("Device does not support 44.1 kHz microphone recording");
                recorder=new AudioRecord(MediaRecorder.AudioSource.MIC,PcmSample.RATE,AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,Math.max(min,8192));
                if(recorder.getState()!=AudioRecord.STATE_INITIALIZED)throw new IOException("Cannot initialize microphone");
                recorder.startRecording();
                // Hard 60 second guard for memory and project size.
                short[] buffer=new short[2048];float[] audio=new float[60*PcmSample.RATE];int pos=0;
                while(active.get()&&pos<audio.length){
                    int n=recorder.read(buffer,0,Math.min(buffer.length,audio.length-pos));
                    if(n<0)throw new IOException("Microphone read failed: "+n);
                    for(int i=0;i<n;i++)audio[pos++]=buffer[i]/32768f;
                }
                sample=new PcmSample("FIELD TAKE",Arrays.copyOf(audio,pos));
            }catch(Exception e){error=e.getMessage();}
            finally{active.set(false);if(recorder!=null){try{recorder.stop();}catch(Exception ignored){}recorder.release();}}
            cb.completed(sample,error);
        }
    }
    public static PcmSample decode(Context context,Uri uri,String name)throws IOException {
        // Fast header sniff: never read an entire MP3 into memory as a WAV candidate.
        try(InputStream stream=context.getContentResolver().openInputStream(uri)){
            if(stream!=null){
                java.io.BufferedInputStream head=new java.io.BufferedInputStream(stream);
                head.mark(16);
                byte[] riff=new byte[4];int n=head.read(riff);head.reset();
                if(n==4&&riff[0]=='R'&&riff[1]=='I'&&riff[2]=='F'&&riff[3]=='F'){
                    try{return WavIO.read(head,name);}catch(IOException unsupportedWav){/* platform decoder fallback */}
                }
            }
        }
        MediaExtractor extractor=new MediaExtractor();MediaCodec codec=null;
        try{
            extractor.setDataSource(context,uri,null);
            int track=-1;
            for(int i=0;i<extractor.getTrackCount();i++){
                MediaFormat f=extractor.getTrackFormat(i);
                if(f.getString(MediaFormat.KEY_MIME).startsWith("audio/")){track=i;break;}
            }
            if(track<0)throw new IOException("No audio track in file");
            extractor.selectTrack(track);
            MediaFormat format=extractor.getTrackFormat(track);
            String mime=format.getString(MediaFormat.KEY_MIME);
            codec=MediaCodec.createDecoderByType(mime);codec.configure(format,null,null,0);codec.start();
            MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
            int channels=format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)?format.getInteger(MediaFormat.KEY_CHANNEL_COUNT):2;
            int rate=format.containsKey(MediaFormat.KEY_SAMPLE_RATE)?format.getInteger(MediaFormat.KEY_SAMPLE_RATE):PcmSample.RATE;
            if(channels<1||channels>8||rate<8000||rate>192000)throw new IOException("Unsupported media format");
            int maxSamples=Math.min(60*rate,60*192000);
            float[] audio=new float[maxSamples];int pos=0;
            boolean inputDone=false,outputDone=false;
            while(!outputDone && pos<maxSamples){
                if(!inputDone){int slot=codec.dequeueInputBuffer(10000);
                    if(slot>=0){ByteBuffer input=codec.getInputBuffer(slot);if(input==null)throw new IOException("Decoder input buffer missing");input.clear();
                        int size=extractor.readSampleData(input,0);
                        if(size<0){codec.queueInputBuffer(slot,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM);inputDone=true;}
                        else {codec.queueInputBuffer(slot,0,size,extractor.getSampleTime(),0);extractor.advance();}
                    }
                }
                int slot=codec.dequeueOutputBuffer(info,10000);
                if(slot==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                    MediaFormat changed=codec.getOutputFormat();
                    channels=changed.containsKey(MediaFormat.KEY_CHANNEL_COUNT)?changed.getInteger(MediaFormat.KEY_CHANNEL_COUNT):channels;
                    rate=changed.containsKey(MediaFormat.KEY_SAMPLE_RATE)?changed.getInteger(MediaFormat.KEY_SAMPLE_RATE):rate;
                    if(channels<1||channels>8||rate<8000||rate>192000)throw new IOException("Unsupported decoder output");
                }else if(slot>=0){
                    ByteBuffer output=codec.getOutputBuffer(slot);
                    if(output!=null&&info.size>0){
                        output.order(ByteOrder.LITTLE_ENDIAN);output.position(info.offset);output.limit(info.offset+info.size);
                        MediaFormat outputFormat=codec.getOutputFormat(slot);
                        int pcm=outputFormat.containsKey(MediaFormat.KEY_PCM_ENCODING)?outputFormat.getInteger(MediaFormat.KEY_PCM_ENCODING):AudioFormat.ENCODING_PCM_16BIT;
                        int bytesPer=pcm==AudioFormat.ENCODING_PCM_FLOAT?4:2;
                        int frames=info.size/Math.max(1,channels*bytesPer);
                        for(int i=0;i<frames&&pos<audio.length;i++){
                            float sum=0;
                            for(int c=0;c<channels;c++)sum+= pcm==AudioFormat.ENCODING_PCM_FLOAT?output.getFloat():output.getShort()/32768f;
                            audio[pos++]=sum/channels;
                        }
                    }
                    codec.releaseOutputBuffer(slot,false);
                    if((info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0)outputDone=true;
                }
            }
            if(pos==0)throw new IOException("The selected file decoded to silence");
            return PcmSample.resample(name,Arrays.copyOf(audio,pos),rate);
        }catch(Exception e){throw new IOException("Cannot decode audio: "+e.getMessage(),e);}
        finally{if(codec!=null){try{codec.stop();}catch(Exception ignored){}codec.release();}extractor.release();}
    }
}
