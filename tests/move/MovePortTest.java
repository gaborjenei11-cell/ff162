import audio.gsa.fieldforce.*;
public class MovePortTest {
 public static void main(String[] args){
  if(MovePresets.NAMES.length!=85||MovePresets.RECIPE.length!=85||MovePresets.VISUAL.length!=85)throw new AssertionError("85 preset mapping");
  for(int i=0;i<85;i++){if(MovePresets.NAMES[i].isEmpty())throw new AssertionError("name");for(float f:MovePresets.RECIPE[i])if(!Float.isFinite(f))throw new AssertionError("recipe");}
  MoveFx fx=new MoveFx();float[] o=new float[2];fx.process(.3f,-.2f,o);if(o[0]!=.3f||o[1]!=-.2f)throw new AssertionError("bypass");
  fx.setEnabled(true);fx.setWet(1);for(int i=0;i<85;i++){fx.setPreset(i);fx.setXY((i%10)/10f,((i*7)%10)/10f);fx.setBpm(40+i*2);for(int j=0;j<50;j++){fx.process(.3f,-.2f,o);if(!Float.isFinite(o[0])||!Float.isFinite(o[1]))throw new AssertionError("nonfinite "+i);}}
  fx.setFreeze(true);float x=fx.x();fx.setXY(0,0);if(fx.x()!=x)throw new AssertionError("freeze");
  MoveMotion m=new MoveMotion();m.record();m.capture(.3f,.4f);m.capture(.5f,.6f);m.stop();if(m.count()!=2)throw new AssertionError("rec");m.play();m.tick(fx);System.out.println("PASS: 85 MOVE profiles, Android DSP, freeze, XY recording");
 }
}
