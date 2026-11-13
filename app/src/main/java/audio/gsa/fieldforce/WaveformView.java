package audio.gsa.fieldforce;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import audio.gsa.fieldforce.core.PcmSample;

/** Waveform with colored neon trace, grids and studio-style display frame. */
public final class WaveformView extends View {
    private final Paint p=new Paint(3);
    private volatile PcmSample sample;
    private int light=Color.CYAN;
    private final float[] levels=new float[256];
    public WaveformView(Context context){super(context);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    public void setAccent(int c){light=c;invalidate();}
    public void setSample(PcmSample s){sample=s;java.util.Arrays.fill(levels,0);
        if(s!=null){for(int i=0;i<256;i++){int a=(int)((long)i*s.data.length/256),b=(int)((long)(i+1)*s.data.length/256);
            float peak=0;for(int j=a;j<b;j++)peak=Math.max(peak,Math.abs(s.data[j]));levels[i]=peak;}}
        invalidate();
    }
    @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();
        p.setShader(new LinearGradient(0,0,w,h,0xff151c2c,0xff060910,Shader.TileMode.CLAMP));p.setStyle(Paint.Style.FILL);c.drawRoundRect(3,3,w-3,h-3,18,18,p);p.setShader(null);
        p.setColor(0xff344050);p.setStrokeWidth(1);for(int i=1;i<8;i++){float x=i*w/8;c.drawLine(x,13,x,h-13,p);}
        for(int i=1;i<4;i++){float y=i*h/4;c.drawLine(13,y,w-13,y,p);}
        p.setColor(light);p.setStrokeWidth(2);p.setShadowLayer(7,0,0,light);
        for(int i=0;i<256;i++){float x=w*i/256,extent=Math.max(.6f,levels[i]*(h*.39f));c.drawLine(x,h/2-extent,x,h/2+extent,p);}
        p.clearShadowLayer();p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xff849bb8);c.drawRoundRect(2,2,w-2,h-2,18,18,p);p.setStyle(Paint.Style.FILL);
        if(sample==null){p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(25);p.setColor(0xff7d8c9c);c.drawText("GSA FIELD INPUT",w*.23f,h*.57f,p);}
    }
}
