#include <jni.h>
#include <mutex>
#include <algorithm>
#include "move/MoveDSP.h"

// Original GSA MOVE 1.0.3d DSP is compiled directly into Android.
struct MobileMove {
    gsamove::MoveDSP dsp;
    gsamove::Controls controls;
    double bpm=120.0;
    MobileMove(double sr){dsp.prepare(sr);}
};
extern "C" {
JNIEXPORT jlong JNICALL Java_audio_gsa_fieldforce_MoveNative_create(JNIEnv*,jclass,jdouble sr){
 try{return reinterpret_cast<jlong>(new MobileMove(sr));}catch(...){return 0;}
}
JNIEXPORT void JNICALL Java_audio_gsa_fieldforce_MoveNative_destroy(JNIEnv*,jclass,jlong h){delete reinterpret_cast<MobileMove*>(h);}
JNIEXPORT void JNICALL Java_audio_gsa_fieldforce_MoveNative_setControls(JNIEnv*,jclass,jlong h,jfloat x,jfloat y,jfloat wet,jint preset,jdouble bpm,jboolean enabled){
 auto* p=reinterpret_cast<MobileMove*>(h);if(!p)return;
 p->controls.x=std::clamp(x,0.f,1.f);p->controls.y=std::clamp(y,0.f,1.f);
 p->controls.dryWet=std::clamp(wet,0.f,1.f);p->controls.presetIndex=std::clamp(static_cast<int>(preset),0,84);
 p->controls.enabled=enabled==JNI_TRUE;p->bpm=std::clamp(bpm,40.,240.);
 gsamove::routeXY(p->controls);
}
JNIEXPORT void JNICALL Java_audio_gsa_fieldforce_MoveNative_process(JNIEnv* env,jclass,jlong h,jfloatArray stereo){
 auto* p=reinterpret_cast<MobileMove*>(h);if(!p||!stereo||env->GetArrayLength(stereo)<2)return;
 jfloat s[2];env->GetFloatArrayRegion(stereo,0,2,s);
 if(p->controls.enabled) p->dsp.process(s[0],s[1],p->controls,gsamove::presets[static_cast<size_t>(p->controls.presetIndex)],p->bpm);
 env->SetFloatArrayRegion(stereo,0,2,s);
}
}
