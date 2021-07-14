package com.hybrid.tripleldc.util.system;


import com.hybrid.tripleldc.util.io.LogUtil;

import java.io.IOException;

/**
 * Author: Joy
 * Created Time: 2021/7/13-17:31
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/13 )
 * <p>
 * Describe:
 */
public class SystemUtil {
    private static final String TAG = "SystemUtil";

    public static boolean pingIP(String ip) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process mIPAddrProcess = runtime.exec("/system/bin/ping -c 1 " + ip);
            LogUtil.d(TAG, String.format("ping -c 1 %s", ip));

            int mExitValue = mIPAddrProcess.waitFor();
            LogUtil.d(TAG, "mExitValue: " + mExitValue);

            return mExitValue == 0;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
