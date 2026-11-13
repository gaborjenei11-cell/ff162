package audio.gsa.fieldforce;
/** Original MOVE 1.0.3d C++ engine, via JNI. Requires libgsa_move.so. */
public final class MoveNative {
    private static final boolean AVAILABLE;
    static { boolean ok; try {System.loadLibrary("gsa_move");ok=true;}catch(UnsatisfiedLinkError e){ok=false;} AVAILABLE=ok; }
    public static boolean available(){return AVAILABLE;}
    public static native long create(double sampleRate);
    public static native void destroy(long handle);
    public static native void setControls(long handle,float x,float y,float wet,int preset,double bpm,boolean enabled);
    public static native void process(long handle,float[] stereo);
    private MoveNative(){}
}
