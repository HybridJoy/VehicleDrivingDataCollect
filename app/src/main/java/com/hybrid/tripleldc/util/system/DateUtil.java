package com.hybrid.tripleldc.util.system;

import com.hybrid.tripleldc.config.DataConst;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
    private static final String TAG = "DateUtil";

    private static final SimpleDateFormat df = new SimpleDateFormat(DataConst.System.TIME_FORMAT);

    public static String getDateString(int year, int month, int day, boolean isNeedSpace) {
        String monthStr = String.valueOf(month + 1);
        String dayStr = String.valueOf(day);
        if (month + 1 < 10) {
            monthStr = "0" + monthStr;
        }
        if (day < 10) {
            dayStr = "0" + dayStr;
        }

        String spaceStr = isNeedSpace ? "-" : "";

        return String.format("%d%s%s%s%s", year, spaceStr, monthStr, spaceStr, dayStr);
    }

    public static String getTimeString(int hour, int minute, int second, boolean is24Hour) {
        boolean isMorning = true;
        if (!is24Hour && hour > 12) {
            hour -= 12;
            isMorning = false;
        }

        String hourStr = String.valueOf(hour);
        String minuteStr = String.valueOf(minute);
        String secondStr = String.valueOf(second);

        if (hour < 10) {
            hourStr = "0" + hourStr;
        }
        if (minute < 10) {
            minuteStr = "0" + minuteStr;
        }
        if (second < 10) {
            secondStr = "0" + secondStr;
        }

        return String.format("%s%s:%s:%s", (is24Hour ? "" : isMorning ? "am " : "pm "), hourStr, minuteStr, secondStr);
    }

    public static String getMilliSecondString(int ms) {
        String msStr = String.valueOf(ms);
        if (ms < 10) {
            msStr = "00" + msStr;
        } else if (ms < 100) {
            msStr = "0" + msStr;
        }

        return String.format(".%s", msStr);
    }

    public static long getTimeInMills(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        return calendar.getTimeInMillis();
    }

    public static String getCurrDateString(boolean isNeedSpace) {
        Calendar calendar = Calendar.getInstance();
        return getDateString(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), isNeedSpace);
    }

    public static String getCurrTimeString(boolean isNeedMilliSecond) {
        Calendar calendar = Calendar.getInstance();
        return String.format("%s%s",
                getTimeString(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), true),
                isNeedMilliSecond ? getCurrMilliSecondString() : "");
    }


    public static String getCurrMilliSecondString() {
        Calendar calendar = Calendar.getInstance();
        return getMilliSecondString(calendar.get(Calendar.MILLISECOND));
    }

    public static String getCurrDateAndTimeString(boolean isNeedSpace, boolean isNeedMilliSecond) {
        return String.format("%s %s", getCurrDateString(isNeedSpace), getCurrTimeString(isNeedMilliSecond));
    }

    public static String getTimestampString(long timestamp) {
        return df.format(timestamp);
    }
}
