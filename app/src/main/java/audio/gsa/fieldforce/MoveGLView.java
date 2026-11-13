package audio.gsa.fieldforce;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/** OpenGL ES 2.0 MOVE pad. The native Android renderer uses the 85 MOVE 1.0.3d
 * visual parameter profiles; it does not claim desktop JUCE renderer parity. */
public final class MoveGLView extends GLSurfaceView {
    private final MoveFx fx;
    private final MotionRenderer renderer;
    public MoveGLView(Context context, MoveFx move, int accent) {
        super(context); fx=move;
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8,8,8,8,16,0);
        renderer=new MotionRenderer(move,accent);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }
    @Override public boolean onTouchEvent(MotionEvent e) {
        switch(e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                fx.setXY(Math.max(0f,Math.min(1f,e.getX()/Math.max(1f,getWidth()))),
                    Math.max(0f,Math.min(1f,1f-e.getY()/Math.max(1f,getHeight()))));
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: return true;
            default: return true;
        }
    }
    private static final class MotionRenderer implements GLSurfaceView.Renderer {
        private static final String VERTEX="attribute vec2 aPosition; void main(){gl_Position=vec4(aPosition,0.0,1.0);}";
        private static final String FRAGMENT="precision mediump float;uniform vec4 uColor;void main(){gl_FragColor=uColor;}";
        private final MoveFx fx;
        private final float[] rgb=new float[3];
        private final FloatBuffer points=ByteBuffer.allocateDirect(512*2*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        private final long start=System.nanoTime();
        private int program,position,color,width=1,height=1;
        MotionRenderer(MoveFx fx,int accent) {this.fx=fx;rgb[0]=((accent>>16)&255)/255f;rgb[1]=((accent>>8)&255)/255f;rgb[2]=(accent&255)/255f;}
        private static int shader(int kind,String code) {
            int s=GLES20.glCreateShader(kind);GLES20.glShaderSource(s,code);GLES20.glCompileShader(s);
            int[] status=new int[1];GLES20.glGetShaderiv(s,GLES20.GL_COMPILE_STATUS,status,0);
            if(status[0]==0){String error=GLES20.glGetShaderInfoLog(s);GLES20.glDeleteShader(s);throw new IllegalStateException(error);}
            return s;
        }
        @Override public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 unused,javax.microedition.khronos.egl.EGLConfig config){
            int vs=shader(GLES20.GL_VERTEX_SHADER,VERTEX),fs=shader(GLES20.GL_FRAGMENT_SHADER,FRAGMENT);
            program=GLES20.glCreateProgram();GLES20.glAttachShader(program,vs);GLES20.glAttachShader(program,fs);
            GLES20.glLinkProgram(program);int[] status=new int[1];GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,status,0);
            if(status[0]==0)throw new IllegalStateException(GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteShader(vs);GLES20.glDeleteShader(fs);
            position=GLES20.glGetAttribLocation(program,"aPosition");color=GLES20.glGetUniformLocation(program,"uColor");
            GLES20.glEnable(GLES20.GL_BLEND);GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE);
        }
        @Override public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 unused,int w,int h){width=Math.max(1,w);height=Math.max(1,h);GLES20.glViewport(0,0,w,h);}
        @Override public void onDrawFrame(javax.microedition.khronos.opengles.GL10 unused){
            GLES20.glClearColor(.008f,.014f,.027f,1f);GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUseProgram(program);GLES20.glEnableVertexAttribArray(position);
            float[] v=MovePresets.VISUAL[fx.preset()];
            double elapsed=(System.nanoTime()-start)*1e-9;
            double rotation=elapsed*fx.bpm()/60.0*Math.PI/4.0*v[2];
            float aspect=height/(float)width;
            int lines=70+(int)(fx.y()*110f);
            final int segments=96;
            for(int i=0;i<lines;i++){
                float layer=i/(float)lines;
                double phase=rotation+layer*6.2831853*v[3];
                points.clear();
                for(int j=0;j<=segments;j++){
                    double t=j/(double)segments;
                    double theta=phase+t*6.2831853*(1+v[4]*.2);
                    double radius=(.10+.62*t+.08*Math.sin(t*12+phase))*(1+fx.y()*.7);
                    float x=(float)(Math.cos(theta)*radius*(1+(.22+fx.x()*.3)*Math.sin(t*9+layer*11)))*aspect;
                    float y=(float)(Math.sin(theta)*radius*.54+Math.sin(t*11+phase)*.14*v[5]);
                    points.put(x);points.put(y);
                }
                points.flip();
                GLES20.glVertexAttribPointer(position,2,GLES20.GL_FLOAT,false,0,points);
                GLES20.glUniform4f(color,rgb[0],rgb[1],rgb[2],.07f+(i%9)*.018f);
                GLES20.glDrawArrays(GLES20.GL_LINE_STRIP,0,segments+1);
            }
            GLES20.glDisableVertexAttribArray(position);
        }
    }
}
