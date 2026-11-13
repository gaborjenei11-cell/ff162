package audio.gsa.fieldforce;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.*;

/** Compact analog controls; no scrollbars or Android SeekBars. */
public final class AnalogControls {
 public interface Change {void onChange(int value);}
 /** Multi-layer translucent backlit polycarbonate face over a metal rim. */
 public static final class LedButtonDrawable extends Drawable {
  private final Paint p=new Paint(3);private final int metal,light;
  public LedButtonDrawable(int metal,int light){this.metal=metal;this.light=light;}
  @Override public void draw(Canvas c){android.graphics.Rect b=getBounds();float x=b.left+1,y=b.top+1,w=b.width()-2,h=b.height()-2,r=Math.min(13,h*.23f);
   p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(x,y,x,y+h,new int[]{0xffa6b0bb,0xff3b444f,0xff10161e},null,Shader.TileMode.CLAMP));c.drawRoundRect(x,y,x+w,y+h,r,r,p);p.setShader(null);
   p.setShader(new LinearGradient(x,y+3,x,y+h-3,new int[]{0x9999b6c7,metal,0xff0b1420},null,Shader.TileMode.CLAMP));c.drawRoundRect(x+2,y+2,x+w-2,y+h-2,r-2,r-2,p);p.setShader(null);
   p.setColor(light);p.setAlpha(175);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);c.drawRoundRect(x+3,y+3,x+w-3,y+h-3,r-3,r-3,p);p.setStyle(Paint.Style.FILL);
   p.setColor(0x55ffffff);c.drawRoundRect(x+5,y+4,x+w-5,y+Math.max(6,h*.27f),Math.max(2,r-5),Math.max(2,r-5),p);
  }
  @Override public void setAlpha(int a){}@Override public void setColorFilter(android.graphics.ColorFilter f){}@Override public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
 }

 public static final class Knob extends View {
  private final Paint p=new Paint(3);private int accent;private android.graphics.Bitmap knobBitmap;private String label;private int value;private Change listener;private float touchY;
  public Knob(Context ctx,String label,int accent,int value,Change change){super(ctx);this.label=label;this.accent=accent;this.value=value;this.listener=change;int index=ctx.getSharedPreferences("fieldforce",Context.MODE_PRIVATE).getInt("skin",0);int[] knobIds={R.drawable.move_knob_dark_graphite,R.drawable.move_knob_blue_alloy,R.drawable.move_knob_cream_metal,R.drawable.move_knob_walnut_glass,R.drawable.move_knob_violet_titanium,R.drawable.move_knob_solar_gold,R.drawable.move_knob_gunmetal_amber,R.drawable.move_knob_bronze_cyan,R.drawable.move_knob_pearl_magenta};try{knobBitmap=android.graphics.BitmapFactory.decodeResource(ctx.getResources(),knobIds[Math.max(0,Math.min(8,index))]);}catch(Exception ignored){}setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  public void setValue(int n){value=Math.max(0,Math.min(100,n));invalidate();}
  @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),radius=Math.min(w*.34f,h*.32f),cx=w/2,cy=h*.46f;
   p.setShader(null);p.setShadowLayer(7,0,5,Color.BLACK);p.setColor(0xff0a1019);c.drawCircle(cx,cy,radius+7,p);p.clearShadowLayer();
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(accent);p.setAlpha(170);c.drawArc(cx-radius-8,cy-radius-8,cx+radius+8,cy+radius+8,135,270*value/100f,false,p);p.setStyle(Paint.Style.FILL);p.setAlpha(255);
   if(knobBitmap!=null){p.setAlpha(255);c.drawBitmap(knobBitmap,null,new RectF(cx-radius,cy-radius,cx+radius,cy+radius),p);}else{p.setShader(new LinearGradient(cx-radius,cy-radius,cx+radius,cy+radius,new int[]{0xffe1e7ec,0xff748391,0xff202a35,0xffa9b4c0},null,Shader.TileMode.CLAMP));c.drawCircle(cx,cy,radius,p);p.setShader(null);}
   p.setColor(0xffe8f0ff);p.setStrokeWidth(3);float a=(float)Math.toRadians(135+270*value/100f);c.drawLine(cx,cy,cx+(float)Math.cos(a)*radius*.72f,cy+(float)Math.sin(a)*radius*.72f,p);
   p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));p.setTextSize(Math.max(11,Math.min(16,h*.15f)));p.setColor(0xfff1f4ff);c.drawText(label,cx,h-7,p);
  }
  @Override public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){touchY=e.getY();return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){int n=Math.round(value+(touchY-e.getY())*.5f);if(n!=value){value=Math.max(0,Math.min(100,n));touchY=e.getY();listener.onChange(value);invalidate();}return true;}return e.getAction()==MotionEvent.ACTION_UP;}
 }
 public static final class Steps extends View {
  public interface Toggle {void onToggle(int track,int step);}
  private final Paint p=new Paint(3);private final audio.gsa.fieldforce.core.Pattern pattern;private final int accent;private Toggle toggle;private final AudioEngine engine;private int playhead=-1;
  public Steps(Context c,audio.gsa.fieldforce.core.Pattern pattern,int accent,AudioEngine engine,Toggle toggle){super(c);this.pattern=pattern;this.accent=accent;this.engine=engine;this.toggle=toggle;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  public void setPlayhead(int n){playhead=n;invalidate();}
  @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();float left=Math.max(68,w*.145f),top=18,cw=(w-left)/16f,ch=(h-top)/16f;
   p.setTextSize(10);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);
   for(int j=0;j<16;j++){p.setColor(0xffb8c7d8);c.drawText(String.valueOf(j+1),left+(j+.5f)*cw,12,p);}
   for(int i=0;i<16;i++){
    float cy=top+(i+.5f)*ch,br=Math.min(5,ch*.18f);
    p.setColor(engine.padFx(i)?0xff39ef80:0xff263545);p.setShadowLayer(engine.padFx(i)?5:0,0,0,0xff39ef80);
    c.drawRoundRect(3,top+i*ch+2,left*.48f,top+(i+1)*ch-2,br,br,p);p.clearShadowLayer();
    p.setColor(engine.padMute(i)?0xffffd44e:0xff263545);p.setShadowLayer(engine.padMute(i)?5:0,0,0,0xffffd44e);
    c.drawRoundRect(left*.52f,top+i*ch+2,left-4,top+(i+1)*ch-2,br,br,p);p.clearShadowLayer();
    p.setTextSize(Math.min(10,ch*.47f));p.setColor(0xffe5edf7);
    c.drawText("FX",left*.25f,cy+ch*.16f,p);c.drawText("M",left*.75f,cy+ch*.16f,p);
    p.setTextSize(10);
    for(int j=0;j<16;j++){float x=left+j*cw+2,y=top+i*ch+2;boolean active=pattern.get(i,j);int color=active?accent:0xff283646;
     p.setColor(0xff0b121c);p.setShadowLayer(3,0,2,0xff000000);c.drawRoundRect(x,y,x+cw-4,y+ch-4,Math.min(6,ch*.25f),Math.min(6,ch*.25f),p);p.clearShadowLayer();
     if(active){p.setColor(accent);p.setAlpha(95);p.setShadowLayer(Math.min(9,ch*.45f),0,0,accent);c.drawRoundRect(x+2,y+2,x+cw-6,y+ch-6,Math.min(5,ch*.22f),Math.min(5,ch*.22f),p);p.clearShadowLayer();p.setAlpha(255);}
     p.setShader(new LinearGradient(x,y,x,y+ch,new int[]{active?0xfff2ffff:0xff8897a8,color,active?color:0xff15202e},null,Shader.TileMode.CLAMP));
     c.drawRoundRect(x+1,y+1,x+cw-5,y+ch-5,Math.min(5,ch*.22f),Math.min(5,ch*.22f),p);p.setShader(null);
     if(j==playhead){p.setColor(0xaaffffff);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);c.drawRoundRect(x,y,x+cw-4,y+ch-4,4,4,p);p.setStyle(Paint.Style.FILL);}
    }
   }
  }
  @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){float left=Math.max(68,getWidth()*.145f),cw=(getWidth()-left)/16f,ch=(getHeight()-18)/16f;int col=(int)((e.getX()-left)/cw),row=(int)((e.getY()-18)/ch);
    if(e.getY()>=18&&row>=0&&row<16){if(e.getX()<left){if(e.getX()<left*.5f)engine.setPadFx(row,!engine.padFx(row));else engine.setPadMute(row,!engine.padMute(row));invalidate();}
    else if(col>=0&&col<16){toggle.onToggle(row,col);invalidate();}}return true;}return true;}
 }
}
