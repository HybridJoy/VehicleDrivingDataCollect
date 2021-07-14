package com.hybrid.tripleldc.util;

import com.hybrid.tripleldc.util.system.DateUtil;

/**
 * Author: Joy
 * Created Time: 2021/7/13-20:08
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/13 )
 * <p>
 * Describe:
 */
public class TripleLDCUtil {
    private static final String TAG = "TripleLDCUtil";

    private static final int ID_LENGTH = 10000;

    public static long generateTimeSliceIDOriginByDate() {
        String date = DateUtil.getCurrDateString(false);
        long dateInt = Long.parseLong(date);
        long origin = dateInt * ID_LENGTH;
        return origin;
    }
}
