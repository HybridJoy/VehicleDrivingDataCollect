package com.hybrid.tripleldc.bean;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Author: Joy
 * Created Time: 2021/7/14-10:40
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/14 )
 * <p>
 * Describe:
 */
@RealmClass
public class GPSPosition implements RealmModel {
    @PrimaryKey
    private int id;
    protected String sampleTime;
    private String deviceName;

    // 经度
    private double longitude;
    // 维度
    private double latitude;

    public GPSPosition() {

    }

    public GPSPosition(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(String sampleTime) {
        this.sampleTime = sampleTime;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Double[] getValue() {
        return new Double[]{longitude, latitude};
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
