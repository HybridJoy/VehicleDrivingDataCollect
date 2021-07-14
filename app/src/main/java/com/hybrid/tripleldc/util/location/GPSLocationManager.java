package com.hybrid.tripleldc.util.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.hybrid.tripleldc.util.io.LogUtil;

import java.lang.ref.WeakReference;

public class GPSLocationManager {
    private static final String TAG = "GPSLocationManager";
    private static GPSLocationManager gpsLocationManager;
    private static Object objLock = new Object();
    private boolean isGpsEnabled;
    private static String mLocateType;
    private WeakReference<Context> mContext;
    private LocationManager locationManager;
    private GPSLocation mGPSLocation;
    private boolean isGuideOPenGps;
    private long minLocationInterval;
    private float minUpdateDistance;

    private static final String GPS_LOCATION_NAME = LocationManager.GPS_PROVIDER;

    private GPSLocationManager(Context context) {
        initData(context);
    }

    private void initData(Context context) {
        this.mContext = new WeakReference<>(context);
        if (mContext.get() != null) {
            locationManager = (LocationManager) (mContext.get().getSystemService(Context.LOCATION_SERVICE));
        }
        // 定位类型：GPS
        mLocateType = locationManager.GPS_PROVIDER;
        // 默认不引导打开GPS设置面板
        isGuideOPenGps = false;
        // 默认定位时间间隔为1000ms
        minLocationInterval = 0;
        // 默认位置可更新的最短距离为0m
        minUpdateDistance = 0;
    }

    public static GPSLocationManager getInstances(Context context) {
        if (gpsLocationManager == null) {
            synchronized (objLock) {
                if (gpsLocationManager == null) {
                    gpsLocationManager = new GPSLocationManager(context);
                }
            }
        }
        return gpsLocationManager;
    }

    /**
     * 方法描述：设置发起定位请求的间隔时长
     *
     * @param minTime 定位间隔时长（单位ms）
     */
    public void setScanSpan(long minTime) {
        this.minLocationInterval = minTime;
    }

    /**
     * 方法描述：设置位置更新的最短距离
     *
     * @param minDistance 最短距离（单位m）
     */
    public void setMinDistance(float minDistance) {
        this.minUpdateDistance = minDistance;
    }

    /**
     * 方法描述：开启定位（默认情况下不会强制要求用户打开GPS设置面板）
     *
     * @param gpsLocationListener
     */
    public void start(GPSLocationListener gpsLocationListener) {
        this.start(gpsLocationListener, isGuideOPenGps);
    }

    /**
     * 方法描述：开启定位
     *
     * @param gpsLocationListener
     * @param isGuideOPenGps  当用户GPS未开启时是否引导用户开启GPS
     * @return 定位服务是否开启成功
     */
    public boolean start(GPSLocationListener gpsLocationListener, boolean isGuideOPenGps) {
        this.isGuideOPenGps = isGuideOPenGps;
        if (mContext.get() == null) {
            return false;
        }
        mGPSLocation = new GPSLocation(gpsLocationListener);
        isGpsEnabled = locationManager.isProviderEnabled(GPS_LOCATION_NAME);
        if (!isGpsEnabled) {
            if (this.isGuideOPenGps) {
                openGPS();
            } else {
                LogUtil.e(TAG, "gps disable");
                return false;
            }
        }

        if (ActivityCompat.checkSelfPermission(mContext.get(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (mContext.get(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        Location lastKnownLocation = locationManager.getLastKnownLocation(mLocateType);
        mGPSLocation.onLocationChanged(lastKnownLocation);
        // 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新
        locationManager.requestLocationUpdates(mLocateType, minLocationInterval, minUpdateDistance, mGPSLocation);

        return true;
    }

    /**
     * 方法描述：转到手机设置界面，用户设置GPS
     */
    public void openGPS() {
        Toast.makeText(mContext.get(), "请打开GPS设置", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT > 15) {
            Intent intent = new Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.get().startActivity(intent);
        }
    }

    /**
     * 方法描述：终止GPS定位,该方法最好在onPause()中调用
     */
    public void stop() {
        if (mContext.get() != null) {
            if (ActivityCompat.checkSelfPermission(mContext.get(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext.get(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (mGPSLocation != null) {
                locationManager.removeUpdates(mGPSLocation);
            }
        }
    }
}
