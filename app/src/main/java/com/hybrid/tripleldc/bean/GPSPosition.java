package com.hybrid.tripleldc.bean;


/**
 * Author: Joy
 * Created Time: 2021/7/14-10:40
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/14 )
 * <p>
 * Describe:
 */
public class GPSPosition {
    private int id;
    // 经度
    private double longitude;
    // 维度
    private double latitude;
    // 时间戳
    private String sampleTime;

    public GPSPosition(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Double[] getValue() {
        return new Double[]{longitude, latitude};
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(String sampleTime) {
        this.sampleTime = sampleTime;
    }
}
