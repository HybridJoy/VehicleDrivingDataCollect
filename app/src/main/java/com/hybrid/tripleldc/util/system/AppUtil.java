package com.hybrid.tripleldc.util.system;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.hybrid.tripleldc.R;
import com.hybrid.tripleldc.global.App;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.ui.DialogUtil;

import java.lang.reflect.Method;
import java.util.List;

public class AppUtil {
    private static final String TAG = "AppUtil";

    /**
     * 获取应用程序包名
     *
     * @param context
     * @return
     */
    public static synchronized String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用程序名称
     */
    public static synchronized String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 打开其他应用程序
     * 只知道包名
     *
     * @param packageName
     * @return
     * @throws Exception
     */
    public static boolean openOtherApp(String packageName) throws Exception {
        if (TextUtils.isEmpty(packageName)) {
            LogUtil.e(TAG, String.format("app info error: packageName: %s", packageName));
            return false;
        }

        PackageInfo pi = null;
        try {
            pi = App.getInstance().getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);

            List<ResolveInfo> apps = App.getInstance().getPackageManager().queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                packageName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;
                ComponentName cn = new ComponentName(packageName, className);

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(cn);
                App.getInstance().startActivity(intent);
                LogUtil.d(TAG, String.format("launch %s successful", packageName));
                return true;
            } else {
                LogUtil.e(TAG, String.format("not find %s", packageName));
                return false;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, String.format("launch %s exception", packageName));
            throw e;
        }
    }

    /**
     * 打开其他应用程序
     * 知道包名和类名
     *
     * @param packageName
     * @param activityName
     * @return
     * @throws Exception
     */
    public static boolean openOtherApp(String packageName, String activityName) throws Exception {
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(activityName)) {
            LogUtil.e(TAG, String.format("app info error: packageName: %s activityName: %s", packageName, activityName));
            return false;
        }

        try {
            ComponentName cn = new ComponentName(packageName, activityName);

            Intent intent = new Intent();
            intent.addCategory(intent.CATEGORY_LAUNCHER);
            intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cn);

            App.getInstance().startActivity(intent);
            LogUtil.d(TAG, String.format("launch %s-%s successful", packageName, activityName));
            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, String.format("launch %s-%s exception", packageName, activityName));
            throw e;
        }
    }

    /**
     * 杀死后台的应用程序
     *
     * @param packageName
     */
    public static void killBackgroundApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            LogUtil.e(TAG, String.format("app info error: packageName: %s", packageName));
            return;
        }

        try {
            ActivityManager activityManager = (ActivityManager) App.getInstance().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.killBackgroundProcesses(packageName);
            LogUtil.d(TAG, String.format("kill %s successful", packageName));
        } catch (Exception e) {
            LogUtil.e(TAG, String.format("kill packageName %s: Exception", packageName));
            e.printStackTrace();
        }
    }

    /**
     * 杀死运行中的进程
     *
     * @param packageName
     */
    public static void killRunningProcess(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            LogUtil.e(TAG, String.format("app info error: packageName: %s", packageName));
            return;
        }

        try {
            ActivityManager activityManager = (ActivityManager) App.getInstance().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> infos = activityManager.getRunningAppProcesses();
            boolean isFindProcess = false;
            for (ActivityManager.RunningAppProcessInfo info : infos) {
                if (info.processName.equals(packageName)) {
                    isFindProcess = true;
                    android.os.Process.killProcess(info.pid);
                }
            }
            if (isFindProcess) {
                LogUtil.d(TAG, String.format("kill %s successful", packageName));
            } else {
                LogUtil.d(TAG, String.format("not find %s", packageName));
            }
        } catch (Exception e) {
            LogUtil.e(TAG, String.format("kill packageName %s: Exception", packageName));
            e.printStackTrace();
        }
    }

    /**
     * 强制关闭app
     *
     * @param packageName
     */
    public static void forceStopApp(String packageName) {
        ActivityManager activityManager = (ActivityManager) App.getInstance().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

        Method method = null;
        try {
            method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(activityManager, packageName);
            LogUtil.d(TAG, String.format("force kill %s successful", packageName));
        } catch (Exception e) {
            LogUtil.e(TAG, String.format("force kill %s: Exception", packageName));
            e.printStackTrace();
        }
    }

    /**
     * 请求权限
     *
     * @param activity
     * @param permission
     */
    public static void requestPermission(@NonNull Activity activity, @NonNull final String permission, @IntRange(from = 0) final int requestCode, @NonNull final String tips) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                DialogInterface.OnClickListener acceptClickListener = (dialog, which) -> {
                    ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);

                    LogUtil.d(TAG, String.format("request permission %s", permission));
                    dialog.cancel();
                };
                DialogInterface.OnClickListener cancelClickListener = (dialog, which) -> {
                    LogUtil.d(TAG, String.format("user not allow request permission %s", permission));
                    dialog.cancel();
                };
                DialogUtil.createDialog(activity, R.string.permission_request_dialog_title, tips,
                        R.string.dialog_accept, R.string.dialog_cancel, acceptClickListener, cancelClickListener)
                        .show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
        }
    }
}
