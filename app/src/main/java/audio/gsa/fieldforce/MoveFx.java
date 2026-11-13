package audio.gsa.fieldforce;
/** MOVE XY processor: original C++ DSP via JNI when available; Java fallback for JVM tests. */
public final class MoveFx {
 private final float[] echo=new float[44100*2]; private final float[] reverb=new float[16001];
 private final long nativeHandle=MoveNative.available()?MoveNative.create(44100.0):0;
 private final float[] nativeFrame=new float[2];
 private int pos=0,rv=0; private volatile float x=.5f,y=.5f,wet=0f;
 private volatile int preset=0,bpm=120;private volatile boolean enabled=false,freeze=false;
 public void setPreset(int n){preset=Math.max(0,Math.min(84,n));}
 public int preset(){return preset;}
 public void setXY(float xx,float yy){if(!freeze){x=clamp(xx);y=clamp(yy);}}
 public float x(){return x;}public float y(){return y;}
 public void setWet(float v){wet=clamp(v);}public float wet(){return wet;}
 public void setBpm(int n){bpm=Math.max(40,Math.min(240,n));}public int bpm(){return bpm;}
 public void setEnabled(boolean b){enabled=b;}public boolean enabled(){return enabled;}
 public void setFreeze(boolean b){freeze=b;}public boolean frozen(){return freeze;}
 private static float clamp(float v){return Math.max(0,Math.min(1,v));}
 public void process(float left,float right,float[] out){
  if(!enabled){out[0]=left;out[1]=right;return;}
  if(nativeHandle!=0){
   nativeFrame[0]=left;nativeFrame[1]=right;
   MoveNative.setControls(nativeHandle,x,y,wet,preset,bpm,true);
   MoveNative.process(nativeHandle,nativeFrame);
   out[0]=nativeFrame[0];out[1]=nativeFrame[1];return;
  }
  float[] r=MovePresets.RECIPE[preset];float xx=x,yy=y;
  float drive=r[5]*(.3f+xx*1.4f),fb=Math.min(.89f,r[2]*(.2f+yy*.9f));
  float input=(left+right)*.5f;float shaped=(float)Math.tanh(input*(1+drive*5));
  float crush=r[6]*xx; if(crush>.01f){float steps=8+256*(1-crush);shaped=Math.round(shaped*steps)/steps;}
  int delay=(int)(44100*60f/bpm*Math.max(.0625f,Math.min(3,r[1])));
  delay=Math.max(64,Math.min(echo.length-1,delay));
  float delayed=echo[(pos-delay+echo.length)%echo.length];
  echo[pos]=Math.max(-1.5f,Math.min(1.5f,shaped+delayed*fb));pos=(pos+1)%echo.length;
  float tail=reverb[rv];reverb[rv]=Math.max(-1.5f,Math.min(1.5f,shaped*.35f+tail*(.4f+r[3]*.45f)));rv=(rv+1)%reverb.length;
  float effect=(shaped*.55f+delayed*(.3f+xx*.55f)+tail*(.15f+yy*.65f));
  float amount=.25f+yy*.75f;
  // 0 = additive original + effect, 1 = wet-only.
  float dry=1-wet;
  out[0]=(float)Math.tanh(left*dry+effect*amount*(1+.2f*xx));
  out[1]=(float)Math.tanh(right*dry+effect*amount*(1+.2f*yy));
 }
}
