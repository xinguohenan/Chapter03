#include <jni.h>
#include <stdio.h>
#include <stdio.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>

static JavaVM* g_JVM; // JavaVM is valid for all threads, so just save it globally
static jobject anrMonitorObj;

static void anr_sig_handler(int sig, siginfo_t *siginfo, void *context) {
    JNIEnv *env = 0;
    g_JVM->AttachCurrentThread(&env, 0);

    jclass clazz = env->FindClass("com/xishui/anrhandler/ANRMonitor");
    jmethodID methodId = env->GetMethodID(clazz, "reportANR", "()V");
    env->CallVoidMethod(anrMonitorObj, methodId);
}

static void register_anr_handler() {
    struct sigaction act;
    memset (&act, '\0', sizeof(act));
    /* Use the sa_sigaction field because the handles has two additional parameters */
    act.sa_sigaction = &anr_sig_handler;
    /* The SA_SIGINFO flag tells sigaction() to use the sa_sigaction field, not sa_handler. */
    act.sa_flags = SA_SIGINFO;
    int result = sigaction(SIGQUIT, &act, NULL);
    int result2 = result;
}

extern "C" {
// https://developer.android.com/training/articles/perf-jni.html
// Until Android 2.0 (Eclair), the '$' character was not properly converted to "_00024" during searches for method names.
// Working around this requires using explicit registration or moving the native methods out of inner classes.
JNIEXPORT void Java_com_xishui_anrhandler_ANRMonitor_setUpANRHandler(
        JNIEnv* env, jobject obj) {
    env->GetJavaVM(&g_JVM);
    register_anr_handler();
    anrMonitorObj = env->NewGlobalRef(obj);
}
}
