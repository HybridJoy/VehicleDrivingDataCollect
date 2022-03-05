package com.hybrid.tripleldc.util.location;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;

import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.bean.GravityAcceleration;
import com.hybrid.tripleldc.util.system.DateUtil;

import io.realm.Realm;

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
    private GPSLocationListener mGpsLocationListener;

    private int gpsPositionLatestID = -1;

    public GPSLocation(GPSLocationListener gpsLocationListener) {
        this.mGpsLocationListener = gpsLocationListener;

        Realm realm = Realm.getDefaultInstance();
        Number gpsPositionLatestID = realm.where(GPSPosition.class).max("id");
        this.gpsPositionLatestID = gpsPositionLatestID == null ? -1 : gpsPositionLatestID.intValue();
        realm.close();
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
