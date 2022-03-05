package com.hybrid.tripleldc.bean;

import org.jetbrains.annotations.NotNull;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Author: Joy
 * Created Time: 2022/3/5-17:16
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2022/3/5 )
 * <p>
 * Describe:
 */
@RealmClass
public class LinearAcceleration implements RealmModel {
    @PrimaryKey
    private int id;
    protected String sampleTime;
    private String deviceName;

    private float xComponent;
    private float yComponent;
    private float zComponent;

    public LinearAcceleration() {

    }

    /**
     * 加速度三维读数 float[]大小必须为3，且以x,y,z的顺序存值
     *
     * @param acceleration 加速度三维读数
     */
    public LinearAcceleration(float[] acceleration) {
        setValue(acceleration);
    }

    /**
     * 加速度三维读数 float[]大小必须为3，且以x,y,z的顺序存值
     *
     * @param acceleration 加速度三维读数
     */
    public void setValue(float[] acceleration) {
        xComponent = acceleration[0];
        yComponent = acceleration[1];
        zComponent = acceleration[2];
    }

    public Float[] getValue() {
        return new Float[]{truncation(xComponent), truncation(yComponent), truncation(zComponent)};
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

    public float getXComponent() {
        return xComponent;
    }

    public void setXComponent(float xComponent) {
        this.xComponent = xComponent;
    }

    public float getYComponent() {
        return yComponent;
    }

    public void setYComponent(float yComponent) {
        this.yComponent = yComponent;
    }

    public float getZComponent() {
        return zComponent;
    }

    public void setZComponent(float zComponent) {
        this.zComponent = zComponent;
    }

    public float truncation(float a) {
        return ((float) Math.round(a * 1000)) / 1000;
    }

    @NotNull
    @Override
    public String toString() {
        return "LinearAcceleration{" +
                "xComponent=" + xComponent +
                ", yComponent=" + yComponent +
                ", zComponent=" + zComponent +
                ", sampleTime=" + sampleTime +
                '}';
    }

}
