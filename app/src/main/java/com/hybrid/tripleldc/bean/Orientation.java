package com.hybrid.tripleldc.bean;

import org.jetbrains.annotations.NotNull;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Author: Tasun
 * Time: 2020/8/7-20:09:13
 */
@RealmClass
public class Orientation implements RealmModel {
    @PrimaryKey
    private int id;
    protected String sampleTime;
    private String deviceName;

    private double xComponent;
    private double yComponent;
    private double zComponent;
    private int tag; // 1 为加速度计回调 2 为磁场传感器回调

    public Orientation() {

    }

    /**
     * 方向三维读数 double[]大小必须为3，且以z,x,y的顺序存值
     *
     * @param orientation 方向三维读数
     */
    public Orientation(double[] orientation) {
        setValue(orientation);
    }

    /**
     * 方向三维读数 double[]大小必须为3，且以z,x,y的顺序存值
     *
     * @param orientation 方向三维读数
     */
    public void setValue(double[] orientation) {
        zComponent = orientation[0];
        xComponent = orientation[1];
        yComponent = orientation[2];
    }

    public Double[] getValue() {
        return new Double[]{truncation(xComponent), truncation(yComponent), truncation(zComponent), (double) getTag()};
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

    public double getXComponent() {
        return xComponent;
    }

    public void setXComponent(float xComponent) {
        this.xComponent = xComponent;
    }

    public double getYComponent() {
        return yComponent;
    }

    public void setYComponent(float yComponent) {
        this.yComponent = yComponent;
    }

    public double getZComponent() {
        return zComponent;
    }

    public void setZComponent(float zComponent) {
        this.zComponent = zComponent;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public double truncation(double a) {
        return ((double) Math.round(a * 1000)) / 1000;
    }

    @NotNull
    @Override
    public String toString() {
        return "Orientation{" +
                "xComponent=" + xComponent +
                ", yComponent=" + yComponent +
                ", zComponent=" + zComponent +
                ", sampleTime=" + sampleTime +
                '}';
    }
}