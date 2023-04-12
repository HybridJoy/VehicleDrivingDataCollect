package com.hybrid.tripleldc.util.io;

import android.os.AsyncTask;
import android.util.Log;


import com.hybrid.tripleldc.BuildConfig;
import com.hybrid.tripleldc.global.App;
import com.hybrid.tripleldc.util.system.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class LogUtil {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "LogUtil";
    private static final String DEFAULT_TAG = "TripleLDC";

    private static List<String> logs = new ArrayList<>();
    private static List<String> cacheLogs = new ArrayList<>();

    private static boolean isNeedWriteLogToFile = true;
    private static boolean isWritingLog = false;
    private static final int writeLogLimit = 20;

    private enum LogType {
        Verbose, Debug, Info, Warn, Error, Assert;
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
        write(LogType.Error, tag, msg);
    }

    public static void e(String msg) {
        Log.e(DEFAULT_TAG, msg);
        write(LogType.Error, DEFAULT_TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (!DEBUG) {
            return;
        }
        Log.d(tag, msg);
        write(LogType.Debug, tag, msg);
    }

    public static void d(String msg) {
        if (!DEBUG) {
            return;
        }
        Log.d(DEFAULT_TAG, msg);
        write(LogType.Debug, DEFAULT_TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (!DEBUG) {
            return;
        }
        Log.i(tag, msg);
        write(LogType.Info, tag, msg);
    }

    public static void i(String msg) {
        if (!DEBUG) {
            return;
        }
        Log.i(DEFAULT_TAG, msg);
        write(LogType.Info, DEFAULT_TAG, msg);
    }

    public static void w(String tag, String msg) {
        if (!DEBUG) {
            return;
        }
        Log.w(tag, msg);
        write(LogType.Warn, tag, msg);
    }

    public static void w(String msg) {
        if (!DEBUG) {
            return;
        }
        Log.w(DEFAULT_TAG, msg);
        write(LogType.Warn, DEFAULT_TAG, msg);
    }

    public static void v(String tag, String msg) {
        if (!DEBUG) {
            return;
        }
        Log.v(tag, msg);
        write(LogType.Verbose, tag, msg);

    }

    public static void v(String msg) {
        if (!DEBUG) {
            return;
        }
        Log.v(DEFAULT_TAG, msg);
        write(LogType.Verbose, DEFAULT_TAG, msg);
    }

    /**
     * 立即写入缓存中的log
     */
    public static void flushRemainLog() {
        new WriteLogTask().execute();
    }

    private static void write(LogType type, String tag, String content) {
        if (!isNeedWriteLogToFile) {
            return;
        }

        String writeLog = String.format("%s %s/%s: %s\n",
                DateUtil.getCurrDateAndTimeString(true, true),
                getLogTypeDescribe(type), tag, content);

        if (isWritingLog) {
            cacheLogs.add(writeLog);
        } else {
            logs.add(writeLog);
            if (logs.size() >= writeLogLimit) {
                new WriteLogTask().execute();
            }
        }
    }

    private static String getLogTypeDescribe(LogType type) {
        switch (type) {
            case Verbose:
                return "V";
            case Debug:
                return "D";
            case Info:
                return "I";
            case Warn:
                return "W";
            case Error:
                return "E";
            case Assert:
                return "A";
            default:
                return "U";
        }
    }

    private static final String fileSuffix = ".log";
    private static final String storageFolder = String.format("%s/Log", App.getInstance().getExternalFilesDir(null));

    private static class WriteLogTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isWritingLog = true;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String fileName = String.format("%s%s", DateUtil.getCurrDateString(false), fileSuffix);
            String filePath = String.format("%s/%s", storageFolder, fileName);

            for (String log : logs) {
                FileIOUtil.writeFileFromString(filePath, log, true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            logs.clear();
            logs.addAll(cacheLogs);
            cacheLogs.clear();
            isWritingLog = false;
            LogUtil.i(TAG, "complete log save");
        }
    }
}
