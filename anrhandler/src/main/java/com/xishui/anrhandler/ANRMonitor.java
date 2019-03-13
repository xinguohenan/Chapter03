package com.xishui.anrhandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ANRMonitor {
    private static final String LOG_TAG = ANRMonitor.class.getSimpleName();

    public native String dumpForSigQuit();

    public interface ANRHandler {
        void onApplicationNotResponding();
    }

    private static final String KEY_THREAD_STACK = "KeyThreadStack";

    private static CpuSampler sCpuSampler = new CpuSampler(1000);

    private static ANRHandler sANRHandler = new ANRHandler() {
        @Override
        public void onApplicationNotResponding() {
//            if (BaseUtils.getProcessName().equalsIgnoreCase(BuildConfig.MAIN_PROCESS_NAME)) {
//                if (s_tcpClient != null) {
//                    s_tcpClient.setContent(getAllThreadStackTrace() + " " + INSTANCE.dumpForSigQuit());
//                }
//            } else {
//                Analytics.logEvent(false, AnalyticsEvent.PERFORMANCE, new BasicNameValuePair[] {
//                        new BasicNameValuePair(AnalyticsConstants.ACTION_NAME, "ANR"),
//                        new BasicNameValuePair(KEY_THREAD_STACK, getAllThreadStackTrace()),
//                        new BasicNameValuePair("CpuRate", sCpuSampler.getCpuRateInfo()),
//                        new BasicNameValuePair(AnalyticsConstants.ACTION_VALUE, BaseUtils.getProcessName())});
//            }
        }
    };
    public static ANRMonitor INSTANCE = new ANRMonitor(sANRHandler);

    private static final int SOCKET_PORT = 23563;
    private ANRHandler _anrHandler;
    private Thread _thread = null;
    public static final long ANR_THRESHOLD_MILLSECONDS = 6000;
    private boolean _isRunning = false;
    private static TCPClient s_tcpClient;

    public static void reportANR() {
        sANRHandler.onApplicationNotResponding();
    }

    public static void init() {
        sCpuSampler.start();
//        if (BaseUtils.getProcessName().equalsIgnoreCase(BuildConfig.KEY_PROCESS_NAME)) {
//            initKeyProcessSocketServer();
//        } else if (BaseUtils.getProcessName().equalsIgnoreCase(BuildConfig.MAIN_PROCESS_NAME)) {
//            s_tcpClient = initMainProcessSocketClient();
//        }

        INSTANCE.start();
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
        _anrHandler = anrHandler;
    }

    public synchronized void start() {
        if (!_isRunning) {
            _isRunning = true;
        }
    }

    public synchronized void stop() {
        _isRunning = false;
    }

    private static void initKeyProcessSocketServer() {
        new Thread(new TCPServer()).start();
    }

    private static TCPClient initMainProcessSocketClient() {
        TCPClient tcpClient = new TCPClient();
        new Thread(tcpClient).start();
        return tcpClient;
    }

    static class TCPServer implements Runnable {
        @Override
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(SOCKET_PORT);
                while(true) {
                    Socket socket = ss.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append("\r\n");
                    }

                    String mainThreadStackTrace = sb.toString();
                    String keyThreadStackStrace = getAllThreadStackTrace();

//                    Analytics.logEvent(false, AnalyticsEvent.PERFORMANCE, new BasicNameValuePair[] {
//                            new BasicNameValuePair(AnalyticsConstants.ACTION_NAME, "ANR"),
//                            new BasicNameValuePair("MainThreadStack", mainThreadStackTrace),
//                            new BasicNameValuePair(KEY_THREAD_STACK, keyThreadStackStrace),
//                            new BasicNameValuePair("CpuRate", sCpuSampler.getCpuRateInfo()),
//                            new BasicNameValuePair(AnalyticsConstants.ACTION_VALUE, BaseUtils.getProcessName()),
//                            new BasicNameValuePair(AnalyticsConstants.STATE_NAME, isLauncherEnabled)});

                    Log.d(LOG_TAG, "main" + mainThreadStackTrace);
                    Log.d(LOG_TAG, "key" + keyThreadStackStrace);
                    Log.d(LOG_TAG, "cpurate" + sCpuSampler.getCpuRateInfo());
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class TCPClient implements Runnable {
        private String _content;

        @Override
        public void run() {
            try {
                while (_content == null) {
                    Thread.sleep(1000);
                }
                Socket socket = new Socket("localhost", SOCKET_PORT);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                bw.write(_content);
                bw.flush();
                _content = null;
                //socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setContent(String content) {
            _content = content;
        }
    }
}
