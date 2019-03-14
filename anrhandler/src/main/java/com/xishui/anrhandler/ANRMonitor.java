package com.xishui.anrhandler;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.util.Log;

public class ANRMonitor {
    private static final String LOG_TAG = ANRMonitor.class.getSimpleName();

    public native String dumpForSigQuit();
    public native void setUpANRHandler();

    static {
        System.loadLibrary("anrhandler");
    }

    public interface ANRHandler {
        void onApplicationNotResponding();
    }

    private static CpuSampler sCpuSampler = new CpuSampler(1000);

    private ANRHandler mDefaultANRHandler;
//    public static ANRMonitor INSTANCE = new ANRMonitor(sDefaultANRHandler);

    private ANRHandler mANRHandler;
    private boolean mIsRunning = false;

    public void reportANR() {
        if (mANRHandler != null) {
            mANRHandler.onApplicationNotResponding();
        } else {
            mDefaultANRHandler.onApplicationNotResponding();
        }
    }

    public void init() {
        // ToDO: Should we start CPU Sampler in production build or only in debug build?
        //sCpuSampler.start();

        setUpANRHandler();

        start();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
//        BaseUtils.getAppContext().registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {
//                    ThreadPoolManager.getInstance().getScheduledExecutor().schedule(new Runnable() {
//                        @Override
//                        public void run() {
//                            sCpuSampler.stop();
//                            INSTANCE.stop();
//                        }
//                    }, 15, TimeUnit.SECONDS);
//                } else {
//                    ThreadPoolManager.getInstance().getExecutor().execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            sCpuSampler.start();
//                            INSTANCE.start();
//                        }
//                    });
//                }
//            }
//        }, intentFilter);

        mDefaultANRHandler = new ANRHandler() {
            @Override
            public void onApplicationNotResponding() {
                Log.e(LOG_TAG, "ANR signal is received! : " + getAllThreadStackTrace() + " " + dumpForSigQuit());
            }
        };
    }

    public static String getAllThreadStackTrace() {
        StringBuilder sb = new StringBuilder();
        Log.d(LOG_TAG, "main");
        Throwable mainThrowable = new Throwable();
        mainThrowable.setStackTrace(Looper.getMainLooper().getThread().getStackTrace());
        sb.append(Log.getStackTraceString(mainThrowable));
        Log.d(LOG_TAG, Log.getStackTraceString(mainThrowable));

        return sb.toString();
    }

    public ANRMonitor(ANRHandler anrHandler) {
        mANRHandler = anrHandler;
        init();
    }

    public synchronized void start() {
        if (!mIsRunning) {
            mIsRunning = true;
        }
    }

    public synchronized void stop() {
        mIsRunning = false;
    }
}
