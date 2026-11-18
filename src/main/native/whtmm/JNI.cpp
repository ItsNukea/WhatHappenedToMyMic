#include <jni.h>
#include "Engine.h"

static Engine engine;

extern "C"
JNIEXPORT void JNICALL
Java_me_nukea_whtmm_audio_AudioEngine_process(
    JNIEnv* env,
    jobject obj
) {
    engine.process();
}