package com.hybrid.tripleldc.util.location;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;

import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.io.RealmHelper;
import com.hybrid.tripleldc.util.system.DateUtil;

import java.util.Locale;


/**
 * Author: Joy
 * Created Time: 2021/7/7-18:19
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/7 )
 * <p>
 * Describe:
 */
public class GPSLocation implements LocationListener {
    private static final String TAG = "GPSLocation";
    private final GPSLocationListener mGpsLocationListener;

    private int gpsPositionLatestID;

    public GPSLocation(GPSLocationListener gpsLocationListener) {
        this.mGpsLocationListener = gpsLocationListener;
        this.gpsPositionLatestID = RealmHelper.getInstance().getInertialSensorDataLatestID(GPSPosition.class);

        LogUtil.d(TAG, String.format(Locale.ENGLISH, "set GPS position latest id as %d", gpsPositionLatestID));
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            // todo 这里损失了很多数据，可以作为一个优化点
            GPSPosition position = new GPSPosition(location.getLongitude(), location.getLatitude());
            position.setId(++gpsPositionLatestID);
            position.setSampleTime(DateUtil.getTimestampString(location.getTime()));
            mGpsLocationListener.UpdateLocation(position);
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        mGpsLocationListener.UpdateStatus(provider, status, extras);
        switch (status) {
            case LocationProvider.AVAILABLE:
                mGpsLocationListener.UpdateGPSProviderStatus(GPSProviderStatus.GPS_AVAILABLE);
                break;
            case LocationProvider.OUT_OF_SERVICE:
                mGpsLocationListener.UpdateGPSProviderStatus(GPSProviderStatus.GPS_OUT_OF_SERVICE);
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                mGpsLocationListener.UpdateGPSProviderStatus(GPSProviderStatus.GPS_TEMPORARILY_UNAVAILABLE);
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        mGpsLocationListener.UpdateGPSProviderStatus(GPSProviderStatus.GPS_ENABLED);
    }

    @Override
    public void onProviderDisabled(String provider) {
        mGpsLocationListener.UpdateGPSProviderStatus(GPSProviderStatus.GPS_DISABLED);
    }
}
