package com.hybrid.tripleldc.bean.base;

/**
 * Author: Joy
 * Created Time: 2021/9/3-19:19
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/9/3 )
 * <p>
 * Describe:
 */
public class BaseSensorData {
    private int id;
    protected String sampleTime;
    private String deviceName;

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
}
