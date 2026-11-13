package audio.gsa.fieldforce;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import audio.gsa.fieldforce.core.*;

/** Full-screen standalone GSA FieldForce: Recorder / Sampler / Sequencer / FX. */
public final class MainActivity extends Activity {
    private static final int IMPORT_SAMPLE=100,LOAD_PROJECT=101,SAVE_PROJECT=102,SAVE_WAV=103, MIC_PERMISSION=200;
    private static final String[] PAGES={"RECORDER","SAMPLER","SEQUENCER","FX RACK"};
    private static final String[] SKINS={"Dark Graphite","Blue Alloy","Cream Metal","Walnut Glass","Violet Titanium",
        "Solar Gold","Gunmetal Amber","Bronze Cyan","Pearl Magenta"};
    private static final int[] BASE={0xff111621,0xff263b56,0xffd7c6a9,0xff52331c,0xff342843,0xffaa8834,0xff333a42,0xff5d493b,0xffc6bac7};
    private static final int[] PANEL={0xff222b3a,0xff34506f,0xffb9a78a,0xff714a29,0xff4e3965,0xffbca34f,0xff48545e,0xff806149,0xffe4d6de};
    private static final int[] ACCENT={0xffff454c,0xff53bfff,0xffff4451,0xff63ef8f,0xffaf62ff,0xff88ff44,0xffffae45,0xff4df5ee,0xffff44b9};
    private static final int[] SKIN_BACKGROUNDS={
        R.drawable.move_background_dark_graphite,R.drawable.move_background_blue_alloy,R.drawable.move_background_cream_metal,
        R.drawable.move_background_walnut_glass,R.drawable.move_background_violet_titanium,R.drawable.move_background_solar_gold,
        R.drawable.move_background_gunmetal_amber,R.drawable.move_background_bronze_cyan,R.drawable.move_background_pearl_magenta
    };
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private final MediaSupport.Recorder recorder=new MediaSupport.Recorder();
    private AudioEngine engine;
    private LinearLayout content,chrome;
    private WaveformView waveform;
    private int skin=0,page=0,selectedPad=0,fxPreset=0,wavAction=0,exportBars=4, pendingPad=-1;
    private boolean movePage=false;private int movePreset=0;private AnalogControls.Steps stepGrid;
    private final MoveMotion moveMotion=new MoveMotion();
    private int moveUiGeneration=0;
    private boolean ready=false;
    private byte[] pendingWav;
    private final Button[] gridButtons=new Button[16];
    private final Button[][] seqButtons=new Button[16][16];
    private TextView status;
    private volatile int stepShown=-1;

    @Override public void onCreate(Bundle state){super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(android.os.Build.VERSION.SDK_INT>=28){android.view.WindowManager.LayoutParams attrs=getWindow().getAttributes();attrs.layoutInDisplayCutoutMode=android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;getWindow().setAttributes(attrs);}
        android.content.SharedPreferences prefs=getSharedPreferences("fieldforce",MODE_PRIVATE);
        skin=prefs.getInt("skin",0);skin=Math.max(0,Math.min(8,skin));
        engine=new AudioEngine();engine.setPositionListener(step->{stepShown=step;main.post(this::updateStepLights);});
        ready=true;display();
    }
    private int fg(){return skin==2||skin==5||skin==8?0xff151923:Color.WHITE;}
    private int secondary(){return skin==2||skin==5||skin==8?0xff41444a:0xffc2cedb;}
    private int dp(int value){return (int)(getResources().getDisplayMetrics().density*value+.5f);}
    private GradientDrawable panel(int color,float radius,int stroke){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{tint(color,1.18f),color,tint(color,.71f)});g.setCornerRadius(dp((int)radius));g.setStroke(dp(1),stroke);return g;}
    private int tint(int c,float gain){return Color.rgb(Math.max(0,Math.min(255,(int)(Color.red(c)*gain))),Math.max(0,Math.min(255,(int)(Color.green(c)*gain))),Math.max(0,Math.min(255,(int)(Color.blue(c)*gain))));}
    private TextView text(String str,int size,boolean heavy){TextView t=new TextView(this);t.setText(str);t.setTextColor(fg());t.setTextSize(size);t.setTypeface(Typeface.create("sans-serif",heavy?Typeface.BOLD:Typeface.NORMAL));t.setPadding(dp(6),dp(5),dp(6),dp(5));return t;}
    private Button button(String label,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(12);b.setTextColor(fg());b.setPadding(dp(4),0,dp(4),0);b.setBackground(new AnalogControls.LedButtonDrawable(PANEL[skin],ACCENT[skin]));b.setElevation(dp(5));b.setOnClickListener(v->action.run());return b;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private void gap(LinearLayout l,int px){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(px)));}
    private void header(LinearLayout target,String title,String description){TextView label=text(title,21,true);label.setTextColor(ACCENT[skin]);target.addView(label);target.addView(text(description,12,false));gap(target,6);}
    private void fillRow(LinearLayout row,View... children){for(View c:children){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(49),1);lp.setMargins(dp(2),dp(2),dp(2),dp(2));row.addView(c,lp);}}
    private void display(){if(!ready||isFinishing())return;
        if(movePage){showMove();return;}
        LinearLayout root=column();root.setPadding(dp(10),dp(8),dp(10),dp(6));root.setBackground(getDrawable(SKIN_BACKGROUNDS[skin]));setContentView(root);
        LinearLayout header=row();header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.move_original_gsa_logo);logo.setAdjustViewBounds(true);
        header.addView(logo,new LinearLayout.LayoutParams(dp(92),dp(54)));
        Button menu=button("MENU  ☰",this::showMenu);header.addView(menu,new LinearLayout.LayoutParams(dp(88),dp(42)));root.addView(header);
        TextView fieldTitle=text("F I E L D F O R C E",15,true);fieldTitle.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);fieldTitle.setTextColor(ACCENT[skin]);header.addView(fieldTitle,new LinearLayout.LayoutParams(0,dp(48),1));
        LinearLayout nav=row();for(int i=0;i<4;i++){final int n=i;Button b=button(PAGES[i],()->{page=n;display();});
            if(i==page)b.setBackground(new AnalogControls.LedButtonDrawable(tint(ACCENT[skin],.47f),ACCENT[skin]));
            nav.addView(b,new LinearLayout.LayoutParams(0,dp(47),1));}root.addView(nav);
        waveform=new WaveformView(this);waveform.setAccent(ACCENT[skin]);waveform.setSample(engine.pads.get(selectedPad));
        if(page==0){LinearLayout.LayoutParams waveSize=new LinearLayout.LayoutParams(-1,dp(78));waveSize.setMargins(0,dp(4),0,dp(4));root.addView(waveform,waveSize);}
        status=text(String.format(Locale.US,"PAD %02d   /   %s   |   %d BPM",selectedPad+1,
            engine.pads.get(selectedPad)==null?"EMPTY":engine.pads.get(selectedPad).name,engine.pattern.bpm),11,false);
        status.setTextColor(secondary());root.addView(status);
        content=column();content.setPadding(dp(6),dp(4),dp(6),dp(4));content.setBackground(panel(PANEL[skin],18,BASE[skin]));root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        switch(page){case 0:buildRecorder();break;case 1:buildSampler();break;case 2:buildSequencer();break;default:buildEffects();}

    }
    private void notifyUser(String message){main.post(()->Toast.makeText(this,message,Toast.LENGTH_SHORT).show());}
    private void selectPad(int i){selectedPad=i;if(waveform!=null)waveform.setSample(engine.pads.get(i));updateStatus();}
    private void updateStatus(){if(status!=null&&engine!=null){PcmSample s=engine.pads.get(selectedPad);status.setText(String.format(Locale.US,"PAD %02d  /  %s  |  %d BPM",selectedPad+1,s==null?"EMPTY":s.name,engine.pattern.bpm));}}
    private void padPicker(LinearLayout target){
        LinearLayout chooser=row();for(int i=0;i<16;i++){final int id=i;Button b=button(i==selectedPad?"●":"·",()->{selectPad(id);display();});
            b.setContentDescription("Select pad "+(i+1));b.setTextSize(11);
            if(i==selectedPad)b.setBackground(new AnalogControls.LedButtonDrawable(PANEL[skin],ACCENT[skin]));
            chooser.addView(b,new LinearLayout.LayoutParams(0,dp(30),1));}
        target.addView(chooser);
    }
    private void buildRecorder(){header(content,"FIELD RECORDER","Capture environmental sounds, edit the take and send it to a pad.");padPicker(content);
        LinearLayout controls=row();Button rec=button(recorder.isRecording()?"STOP REC":"● REC",this::recordToggle);
        fillRow(controls,rec,button("▶ PLAY",()->engine.trigger(selectedPad)),button("STOP",()->engine.stop()),button("WAV SAVE",()->exportPad()));content.addView(controls);
        content.addView(text("INPUT MONITOR / TAKE EDITOR",13,true));
        LinearLayout edits=row();fillRow(edits,button("NORMALIZE",()->editSample(0)),button("REVERSE",()->editSample(1)),button("FADE",()->editSample(2)));content.addView(edits);
        LinearLayout edits2=row();fillRow(edits2,button("TRIM 10%",()->editSample(3)),button("PROCESS FX",this::processSelectedFx),button("IMPORT",this::importSample));content.addView(edits2);

    }
    private void recordToggle(){if(recorder.isRecording()){recorder.stop();notifyUser("Finishing take...");return;}
        if(android.os.Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC_PERMISSION);return;}
        startRecording();
    }
    private void startRecording(){
        final int recordPad=selectedPad;
        boolean ok=recorder.start((sample,error)->main.post(()->{
            if(error!=null)notifyUser("Recording failed: "+error);
            else if(sample!=null&&sample.data.length>0){engine.pads.set(recordPad,sample);notifyUser("Take assigned to PAD "+(recordPad+1));}
            if(page==0)display();
        }));if(ok){notifyUser("Microphone recording started (max 60 s)");display();}
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){super.onRequestPermissionsResult(request,permissions,results);
        if(request==MIC_PERMISSION&&results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)startRecording();else notifyUser("Microphone permission required for field recording");
    }
    private void editSample(int action){PcmSample s=engine.pads.get(selectedPad);if(s==null){notifyUser("Select a loaded pad");return;}
        PcmSample updated= action==0?s.normalize():action==1?s.reverse():action==2?s.fade():s.trim(.1f,.9f);
        engine.pads.set(selectedPad,updated);waveform.setSample(updated);updateStatus();
    }
    private void processSelectedFx(){PcmSample s=engine.pads.get(selectedPad);if(s==null)return;
        io.execute(()->{AudioFx processor=new AudioFx();int[] params=engine.fx.settings();processor.set(params[0],params[1],params[2],params[3],params[4],params[5],params[6],params[7]);
            float[] dst=new float[s.data.length],out=new float[2];for(int i=0;i<dst.length;i++){processor.process(s.data[i],s.data[i],out);dst[i]=(out[0]+out[1])*.5f;}
            PcmSample result=new PcmSample(s.name+" FX",dst);engine.pads.set(selectedPad,result);main.post(()->{waveform.setSample(result);updateStatus();notifyUser("FX printed into sample");});
        });
    }
    private Button lightButton(String name,int color,boolean active,Runnable action){
        Button b=button(name,action);b.setTextSize(10);b.setTextColor(active?Color.WHITE:secondary());
        b.setBackground(new AnalogControls.LedButtonDrawable(PANEL[skin],active?color:0xff263545));return b;
    }
    private void padImport(int pad){selectPad(pad);importSample();}
    private void padExport(int pad){selectPad(pad);exportPad();}
    private void buildSampler(){
        LinearLayout top=row();Button global=lightButton("GLOBAL FX",engine.anyPadFx()&&engine.globalFx?0xff3aff7a:0xffff3945,true,()->{engine.globalFx=!engine.globalFx;display();});
        top.addView(global,new LinearLayout.LayoutParams(0,dp(43),1));
        top.addView(button("KIT",()->new AlertDialog.Builder(this).setTitle("LOAD KIT").setItems(KitFactory.KITS,(d,i)->{engine.loadKit(i);display();}).show()),new LinearLayout.LayoutParams(0,dp(43),1));
        content.addView(top);
        for(int r=0;r<4;r++){
            LinearLayout line=row();for(int c=0;c<4;c++){
                final int id=r*4+c;LinearLayout cell=column();
                Button pad=lightButton("",ACCENT[skin],id==selectedPad,()->{selectPad(id);engine.trigger(id);display();});
                pad.setContentDescription("Pad "+(id+1)+(engine.pads.get(id)==null?" empty":" loaded"));
                pad.setOnLongClickListener(v->{selectPad(id);display();return true;});
                cell.addView(pad,new LinearLayout.LayoutParams(-1,0,1));
                LinearLayout actions=row();
                Button imp=lightButton("IN",0xff53bfff,false,()->padImport(id));
                Button exp=lightButton("OUT",0xff53bfff,false,()->padExport(id));
                Button fx=lightButton("FX",0xff3aff7a,engine.padFx(id),()->{engine.setPadFx(id,!engine.padFx(id));display();});
                for(Button b:new Button[]{imp,exp,fx}){LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(31),1);bp.setMargins(dp(1),0,dp(1),0);actions.addView(b,bp);}
                cell.addView(actions);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1);cp.setMargins(dp(2),dp(2),dp(2),dp(2));line.addView(cell,cp);
            }content.addView(line,new LinearLayout.LayoutParams(-1,0,1));
        }
    }
    private void buildSequencer(){
        LinearLayout transport=row();fillRow(transport,button("▶",()->engine.play()),button("■",()->engine.stop()),
            button(engine.recordNotes?"● REC":"○ REC",()->{engine.recordNotes=!engine.recordNotes;display();}),button("WAV",this::exportSequence));content.addView(transport);
        LinearLayout settings=row();
        Button minus=button("−",()->{engine.pattern.bpm=Math.max(40,engine.pattern.bpm-1);display();});
        Button plus=button("+",()->{engine.pattern.bpm=Math.min(240,engine.pattern.bpm+1);display();});
        Button tempo=button("TEMPO "+engine.pattern.bpm,()->{engine.pattern.bpm=engine.pattern.bpm>=240?40:engine.pattern.bpm+5;display();});
        Button metro=button(engine.pattern.metronome?"METRONOME ON":"METRONOME OFF",()->{engine.pattern.metronome=!engine.pattern.metronome;display();});
        Button swing=button("SWING "+engine.pattern.swing,()->{engine.pattern.swing=(engine.pattern.swing+5)%70;display();});
        fillRow(settings,minus,tempo,plus,metro,swing);content.addView(settings);
        stepGrid=new AnalogControls.Steps(this,engine.pattern,ACCENT[skin],engine,(track,step)->engine.pattern.toggle(track,step));
        content.addView(stepGrid,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout foot=row();fillRow(foot,button("CLEAR",()->new AlertDialog.Builder(this).setTitle("Clear pattern?").setPositiveButton("CLEAR",(d,w)->{engine.pattern.clear();stepGrid.invalidate();}).setNegativeButton("CANCEL",null).show()),button("SAMPLER",()->{page=1;display();}));content.addView(foot);
        updateStepLights();
    }
    private interface OnValue{void changed(int v);}
    private SeekBar.OnSeekBarChangeListener seek(OnValue callback){return new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}public void onProgressChanged(SeekBar b,int v,boolean from){if(from)callback.changed(v);}};}
    private void tempoSlider(LinearLayout parent){TextView label=text("TEMPO  "+engine.pattern.bpm+" BPM",13,true);parent.addView(label);
        SeekBar bar=new SeekBar(this);bar.setMax(200);bar.setProgress(engine.pattern.bpm-40);bar.setProgressTintList(android.content.res.ColorStateList.valueOf(ACCENT[skin]));
        bar.setOnSeekBarChangeListener(seek((v)->{engine.pattern.bpm=40+v;label.setText("TEMPO  "+engine.pattern.bpm+" BPM");updateStatus();}));parent.addView(bar);
    }
    private void updateStepLights(){if(stepGrid!=null&&page==2)stepGrid.setPlayhead(engine.playing?stepShown:-1);}
    private void buildEffects(){
        LinearLayout top=row();fillRow(top,button("FX PRESET: "+AudioFx.PRESETS[fxPreset],()->{
            new AlertDialog.Builder(this).setTitle("FX PRESETS").setItems(AudioFx.PRESETS,(d,n)->{fxPreset=n;engine.fx.setPreset(n);display();}).show();
        }),button("GSA MOVE 1.0.3d",this::openMove));content.addView(top);
        content.addView(lightButton(engine.fx.isEnabled()?"FX ENGINE ON":"FX ENGINE OFF",0xff3aff7a,engine.fx.isEnabled(),()->{engine.fx.setEnabled(!engine.fx.isEnabled());display();}),new LinearLayout.LayoutParams(-1,dp(38)));
        final String[] labels={"DRY/WET","DRIVE","ECHO","REVERB","FILTER","CRUSH","CHORUS","STEREO"};
        int[] values=engine.fx.settings();
        for(int row=0;row<2;row++){
            LinearLayout knobs=row();for(int col=0;col<4;col++){
                final int idx=row*4+col;
                AnalogControls.Knob k=new AnalogControls.Knob(this,labels[idx],ACCENT[skin],values[idx],v->{int[] a=engine.fx.settings();a[idx]=v;engine.fx.set(a[0],a[1],a[2],a[3],a[4],a[5],a[6],a[7]);});
                knobs.addView(k,new LinearLayout.LayoutParams(0,-1,1));
            }
            content.addView(knobs,new LinearLayout.LayoutParams(-1,0,1));
        }
        content.addView(button("PRINT EFFECT TO SAMPLE",this::processSelectedFx),new LinearLayout.LayoutParams(-1,dp(45)));
    }
    private void openMove(){movePage=true;engine.move.setEnabled(true);engine.move.setPreset(movePreset);engine.move.setBpm(engine.pattern.bpm);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);display();}
    private void closeMove(){moveMotion.stop();movePage=false;
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);page=3;display();}
    private void showMove(){
        LinearLayout root=column();root.setPadding(dp(8),dp(4),dp(8),dp(6));root.setBackgroundResource(SKIN_BACKGROUNDS[skin]);
        android.util.DisplayMetrics metrics=getResources().getDisplayMetrics();int sw=metrics.widthPixels,sh=metrics.heightPixels;
        int vw=Math.min(sw,(int)(sh*16f/9f)),vh=Math.min(sh,(int)(sw*9f/16f));
        FrameLayout viewport=new FrameLayout(this);viewport.setBackgroundColor(0xff04070c);
        FrameLayout.LayoutParams frame=new FrameLayout.LayoutParams(vw,vh,Gravity.CENTER);viewport.addView(root,frame);setContentView(viewport);
        LinearLayout top=row();ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.move_original_gsa_logo);
        top.addView(logo,new LinearLayout.LayoutParams(dp(76),dp(44)));
        TextView name=text("MOVE  •  1.0.3d",19,true);name.setTextColor(ACCENT[skin]);top.addView(name,new LinearLayout.LayoutParams(0,dp(44),1));
        top.addView(button("SKIN",this::chooseSharedSkin),new LinearLayout.LayoutParams(dp(72),dp(44)));
        top.addView(button("◀ FX RACK",this::closeMove),new LinearLayout.LayoutParams(dp(110),dp(44)));
        root.addView(top);
        LinearLayout presets=row();fillRow(presets,button("◀",()->{movePreset=(movePreset+84)%85;engine.move.setPreset(movePreset);display();}),
            button(MovePresets.NAMES[movePreset],()->new AlertDialog.Builder(this).setTitle("MOVE 85 PRESETS").setItems(MovePresets.NAMES,(d,n)->{movePreset=n;engine.move.setPreset(n);display();}).show()),
            button("▶",()->{movePreset=(movePreset+1)%85;engine.move.setPreset(movePreset);display();}),
            button(engine.move.frozen()?"FREEZE ON":"FREEZE",()->{engine.move.setFreeze(!engine.move.frozen());display();}));root.addView(presets);
        LinearLayout actions=row();fillRow(actions,button(moveMotion.isRecording()?"STOP REC":"● REC",()->{if(moveMotion.isRecording())moveMotion.stop();else moveMotion.record();display();}),
          button("▶ PLAY",()->{moveMotion.play();display();}),button("■ STOP",()->{moveMotion.stop();display();}),
          button("RANDOM",()->{movePreset=(int)(Math.random()*85);engine.move.setPreset(movePreset);display();}));root.addView(actions);
        LinearLayout files=row();fillRow(files,button("SAVE MOTION",()->{getSharedPreferences("fieldforce",MODE_PRIVATE).edit().putString("move_motion",moveMotion.serialize()).apply();notifyUser("MOVE motion saved");}),
          button("LOAD MOTION",()->{moveMotion.deserialize(getSharedPreferences("fieldforce",MODE_PRIVATE).getString("move_motion",""));notifyUser("MOVE motion loaded: "+moveMotion.count()+" points");}));root.addView(files);
        LinearLayout body=row();root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout knobs=column();body.addView(knobs,new LinearLayout.LayoutParams(dp(100),-1));
        knobs.addView(new AnalogControls.Knob(this,"X AXIS",ACCENT[skin],(int)(engine.move.x()*100),v->engine.move.setXY(v/100f,engine.move.y())),new LinearLayout.LayoutParams(-1,0,1));
        knobs.addView(new AnalogControls.Knob(this,"Y AXIS",ACCENT[skin],(int)(engine.move.y()*100),v->engine.move.setXY(engine.move.x(),v/100f)),new LinearLayout.LayoutParams(-1,0,1));
        knobs.addView(new AnalogControls.Knob(this,"DRY/WET",ACCENT[skin],(int)(engine.move.wet()*100),v->engine.move.setWet(v/100f)),new LinearLayout.LayoutParams(-1,0,1));
        MoveGLView pad=new MoveGLView(this,engine.move,ACCENT[skin]);body.addView(pad,new LinearLayout.LayoutParams(0,-1,1));
        final int generation=++moveUiGeneration;main.postDelayed(new Runnable(){public void run(){if(movePage&&!isFinishing()&&generation==moveUiGeneration){moveMotion.capture(engine.move.x(),engine.move.y());moveMotion.tick(engine.move);main.postDelayed(this,33);}}},33);
    }
    @Override public void onConfigurationChanged(android.content.res.Configuration config){super.onConfigurationChanged(config);display();}
    private void chooseSharedSkin(){new AlertDialog.Builder(this).setTitle("GSA  /  UNIFIED MOVE SKINS").setSingleChoiceItems(SKINS,skin,(dialog,n)->{skin=n;getSharedPreferences("fieldforce",MODE_PRIVATE).edit().putInt("skin",skin).apply();dialog.dismiss();display();}).setNegativeButton("CLOSE",null).show();}
    private void showMenu(){String[] items={"NEW PROJECT","SAVE PROJECT","LOAD PROJECT","SKINS (9)","EXPORT SEQUENCE WAV","EXIT"};
        new AlertDialog.Builder(this).setTitle("GSA FIELDFORCE  /  MENU").setItems(items,(d,index)->{
            switch(index){case 0:new AlertDialog.Builder(this).setTitle("New project?").setMessage("Replace all pads and clear the sequence?").setPositiveButton("NEW",(a,b)->newProject()).setNegativeButton("CANCEL",null).show();break;
                case 1:document("application/octet-stream","GSA FieldForce.fieldforce",SAVE_PROJECT);break;
                case 2:openDoc("*/*",LOAD_PROJECT);break;
                case 3:chooseSharedSkin();break;
                case 4:exportSequence();break;
                case 5:new AlertDialog.Builder(this).setTitle("Exit FieldForce?").setPositiveButton("EXIT",(a,b)->finish()).setNegativeButton("CANCEL",null).show();break;
            }
        }).show();
    }
    private void newProject(){engine.stop();engine.resetPadRouting();engine.pattern.clear();engine.pattern.bpm=120;engine.pattern.swing=0;engine.pattern.metronome=false;
        engine.loadKit(0);engine.fx.setPreset(0);fxPreset=0;selectedPad=0;display();}
    private void importSample(){openDoc("audio/*",IMPORT_SAMPLE);}
    private void exportPad(){PcmSample sample=engine.pads.get(selectedPad);if(sample==null){notifyUser("No sample");return;}
        wavAction=0;document("audio/wav",String.format(Locale.US,"GSA_Pad_%02d.wav",selectedPad+1),SAVE_WAV);
    }
    private void exportSequence(){final String[] options={"1 BAR","2 BARS","4 BARS","8 BARS","16 BARS"};final int[] values={1,2,4,8,16};
        new AlertDialog.Builder(this).setTitle("RENDER SEQUENCER WAV").setItems(options,(dialog,i)->{exportBars=values[i];wavAction=1;document("audio/wav","GSA_FieldForce_Sequence.wav",SAVE_WAV);}).show();
    }
    private void document(String mime,String suggested,int request){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType(mime);i.putExtra(Intent.EXTRA_TITLE,suggested);startActivityForResult(i,request);}
    private void openDoc(String mime,int request){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType(mime);startActivityForResult(i,request);}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        if(request==IMPORT_SAMPLE){final int pad=selectedPad;io.execute(()->{
            try{String label="IMPORTED SAMPLE";try(android.database.Cursor c=getContentResolver().query(uri,null,null,null,null)){
                if(c!=null&&c.moveToFirst()){int n=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(n>=0)label=c.getString(n);}}
                PcmSample s=MediaSupport.decode(this,uri,label);engine.pads.set(pad,s);main.post(()->{selectPad(pad);display();notifyUser("Audio imported to PAD "+(pad+1));});
            }catch(Exception e){notifyUser("Import failed: "+e.getMessage());}
        });}
        else if(request==SAVE_PROJECT){io.execute(()->{try(OutputStream out=getContentResolver().openOutputStream(uri)){
            ProjectCodec.Data project=new ProjectCodec.Data();for(int j=0;j<16;j++)project.pads[j]=engine.pads.get(j);
            project.pattern.bpm=engine.pattern.bpm;project.pattern.swing=engine.pattern.swing;project.pattern.metronome=engine.pattern.metronome;project.pattern.decode(engine.pattern.encode());project.preset=fxPreset;project.skin=skin;project.globalFx=engine.globalFx;for(int j=0;j<16;j++){project.padFx[j]=engine.padFx(j);project.padMute[j]=engine.padMute(j);}
            if(out==null)throw new IOException("No output stream");ProjectCodec.save(out,project);notifyUser("Project saved");
        }catch(Exception e){notifyUser("Save failed: "+e.getMessage());}});}
        else if(request==LOAD_PROJECT){io.execute(()->{try(InputStream input=getContentResolver().openInputStream(uri)){
            if(input==null)throw new IOException("No input stream");ProjectCodec.Data project=ProjectCodec.load(input);
            engine.stop();for(int j=0;j<16;j++)engine.pads.set(j,project.pads[j]);engine.pattern.decode(project.pattern.encode());
            engine.pattern.bpm=project.pattern.bpm;engine.pattern.swing=project.pattern.swing;engine.pattern.metronome=project.pattern.metronome;
            engine.fx.setPreset(project.preset);fxPreset=project.preset;skin=project.skin;engine.globalFx=project.globalFx;for(int j=0;j<16;j++){engine.setPadFx(j,project.padFx[j]);engine.setPadMute(j,project.padMute[j]);}
            getSharedPreferences("fieldforce",MODE_PRIVATE).edit().putInt("skin",skin).apply();main.post(()->{selectedPad=0;display();notifyUser("Project loaded");});
        }catch(Exception e){notifyUser("Load failed: "+e.getMessage());}});}
        else if(request==SAVE_WAV){final int action=wavAction,bars=exportBars,pad=selectedPad;io.execute(()->{try(OutputStream out=getContentResolver().openOutputStream(uri)){
            if(out==null)throw new IOException("No output stream");
            float[] samples;int channels;
            if(action==0){PcmSample sound=engine.pads.get(pad);if(sound==null)throw new IOException("Pad empty");samples=sound.data;channels=1;}
            else {samples=engine.renderBars(bars);channels=2;}
            WavIO.write(out,samples,channels,PcmSample.RATE);notifyUser("WAV exported successfully");
        }catch(Exception e){notifyUser("WAV export failed: "+e.getMessage());}});}
    }
    @Override public void onBackPressed(){if(movePage)closeMove();else super.onBackPressed();}
    @Override protected void onDestroy(){recorder.stop();if(engine!=null)engine.close();io.shutdown();super.onDestroy();}
}
