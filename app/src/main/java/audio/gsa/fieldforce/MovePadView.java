package audio.gsa.fieldforce;
import android.content.Context;
import android.graphics.*;
import android.view.*;

/** Android Canvas implementation of MOVE's 85 per-preset motion profiles.
 * VST OpenGL renderer itself is not executable on Android without an NDK port. */
public final class MovePadView extends View {
 private final Paint p=new Paint(3);private final MoveFx fx;private int accent;private long epoch=System.nanoTime();private boolean touched=false;
 public MovePadView(Context c,MoveFx fx,int accent){super(c);this.fx=fx;this.accent=accent;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
 @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float w=getWidth(),h=getHeight(),cx=w*.5f,cy=h*.5f;
  p.setShader(new LinearGradient(0,0,w,h,0xff273341,0xff030913,Shader.TileMode.CLAMP));canvas.drawRoundRect(0,0,w,h,18,18,p);p.setShader(null);
  p.setColor(accent);p.setAlpha(35);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);canvas.drawRoundRect(3,3,w-3,h-3,16,16,p);
  float[] v=MovePresets.VISUAL[fx.preset()];float bpm=fx.bpm();double beats=(System.nanoTime()-epoch)/1e9*bpm/60.;double angle=beats*Math.PI/4*v[2];
  float zoom=1+fx.y()*.7f+(touched?.3f:0),amp=.22f+fx.x()*.3f;
  canvas.save();canvas.clipRect(8,8,w-8,h-8);canvas.translate(cx,cy);
  int lines=55+(int)(fx.y()*85);int segments=48;
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);
  for(int i=0;i<lines;i++){
   Path path=new Path();double layer=i/(double)lines;double phase=angle+layer*6.283*v[3];
   for(int j=0;j<=segments;j++){double t=j/(double)segments,theta=phase+t*Math.PI*2*(1+v[4]*.2);double radius=(.10+.62*t+.08*Math.sin(t*12+phase))*Math.min(w,h)*.5*zoom;
    float x=(float)(Math.cos(theta)*radius*(1+amp*Math.sin(t*9+layer*11)));float y=(float)(Math.sin(theta)*radius*.54+Math.sin(t*11+phase)*h*.07*v[5]);
    if(j==0)path.moveTo(x,y);else path.lineTo(x,y);
   }
   p.setColor(accent);p.setAlpha(25+(i%9)*12);canvas.drawPath(path,p);
  }
  p.setStyle(Paint.Style.FILL);p.setAlpha(255);canvas.restore();postInvalidateDelayed(33);
 }
 @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN||e.getAction()==MotionEvent.ACTION_MOVE){touched=true;fx.setXY(e.getX()/getWidth(),1-e.getY()/getHeight());invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_UP){touched=false;return true;}return true;}
}
