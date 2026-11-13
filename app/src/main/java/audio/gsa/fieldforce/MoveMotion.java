package audio.gsa.fieldforce;
import java.util.ArrayList;
/** In-memory XY gesture capture/playback for the Android MOVE view. */
public final class MoveMotion {
 private final ArrayList<float[]> points=new ArrayList<>();private boolean recording=false,playing=false;
 private long start=0;
 public void record(){points.clear();recording=true;playing=false;start=System.currentTimeMillis();}
 public void stop(){recording=false;playing=false;}
 public void capture(float x,float y){if(recording&&points.size()<36000)points.add(new float[]{System.currentTimeMillis()-start,x,y});}
 public boolean isRecording(){return recording;}public boolean isPlaying(){return playing;}
 public void play(){if(points.size()>0){recording=false;playing=true;start=System.currentTimeMillis();}}
 public void tick(MoveFx fx){if(!playing||points.size()==0)return;long t=System.currentTimeMillis()-start;
  float[] last=points.get(points.size()-1);if(t>=last[0]){fx.setXY(last[1],last[2]);playing=false;return;}
  int lo=0,hi=points.size()-1;while(lo<hi){int m=(lo+hi+1)/2;if(points.get(m)[0]<=t)lo=m;else hi=m-1;}
  float[] a=points.get(lo),b=points.get(Math.min(lo+1,points.size()-1));float f=b[0]<=a[0]?0:(t-a[0])/(b[0]-a[0]);fx.setXY(a[1]+(b[1]-a[1])*f,a[2]+(b[2]-a[2])*f);
 }
 public String serialize(){StringBuilder b=new StringBuilder();for(float[] p:points){if(b.length()>0)b.append(';');b.append((int)p[0]).append(',').append(p[1]).append(',').append(p[2]);}return b.toString();}
 public void deserialize(String value){stop();points.clear();if(value==null||value.isEmpty())return;for(String row:value.split(";")){try{String[] v=row.split(",");if(v.length==3&&points.size()<36000)points.add(new float[]{Float.parseFloat(v[0]),Float.parseFloat(v[1]),Float.parseFloat(v[2])});}catch(NumberFormatException ignored){}}}
 public int count(){return points.size();}
}
