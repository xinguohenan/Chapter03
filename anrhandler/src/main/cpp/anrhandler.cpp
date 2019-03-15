#include <jni.h>
#include <stdio.h>
#include <stdio.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <setjmp.h>

static JavaVM* g_JVM; // JavaVM is valid for all threads, so just save it globally
static jobject anrMonitorObj;
sigjmp_buf mark;

static void anr_sig_handler(int sig, siginfo_t *siginfo, void *context) {
    JNIEnv *env = 0;
    g_JVM->AttachCurrentThread(&env, 0);

    jclass clazz = env->FindClass("com/xishui/anrhandler/ANRMonitor");
    jmethodID methodId = env->GetMethodID(clazz, "reportANR", "()V");
    env->CallVoidMethod(anrMonitorObj, methodId);
    //siglongjmp(mark, 1);
}

static void register_anr_handler() {
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sigemptyset(&sa.sa_mask);
    /* Use the sa_sigaction field because the handles has two additional parameters */
    sa.sa_sigaction = &anr_sig_handler;
    /* The SA_SIGINFO flag tells sigaction() to use the sa_sigaction field, not sa_handler. */
    sa.sa_flags = SA_SIGINFO | SA_ONSTACK;
    sigaddset(&sa.sa_mask, SIGSEGV);
    sigaddset(&sa.sa_mask, SIGQUIT);
    int result = sigaction(SIGQUIT, &sa, NULL);
//    int result2 = sigaction(SIGSEGV, &sa, NULL);
}

extern "C" {
// https://developer.android.com/training/articles/perf-jni.html
// Until Android 2.0 (Eclair), the '$' character was not properly converted to "_00024" during searches for method names.
// Working around this requires using explicit registration or moving the native methods out of inner classes.
JNIEXPORT void Java_com_xishui_anrhandler_ANRMonitor_setUpANRHandler(
        JNIEnv* env, jobject obj) {
    env->GetJavaVM(&g_JVM);
    anrMonitorObj = env->NewGlobalRef(obj);
    register_anr_handler();
}

JNIEXPORT void Java_com_xishui_anrhandler_ANRMonitor_testCrash(
        JNIEnv* env, jobject obj) {
    // SIGSEGV will cause infinite loop crash, see explanation here:
    // https://stackoverflow.com/questions/6981325/why-signal-handler-goes-to-infinite-loop-sigsegv
    // We use siglongjmp to avoid infinite loop.
    *(int*)0 = 0;
}

}
