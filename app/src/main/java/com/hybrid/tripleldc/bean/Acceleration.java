package com.hybrid.tripleldc.bean;


import com.hybrid.tripleldc.bean.base.BaseSensorData;

import org.jetbrains.annotations.NotNull;

/**
 * Author: Tasun
 * Time: 2020/8/7-20:09:25
 */
public class Acceleration extends BaseSensorData {
    private float xComponent;
    private float yComponent;
    private float zComponent;

    public Acceleration() {

    }

    /**
     * 加速度三维读数 float[]大小必须为3，且以x,y,z的顺序存值
     *
     * @param acceleration 加速度三维读数
     */
    public Acceleration(float[] acceleration) {
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
        return "Acceleration{" +
                "xComponent=" + xComponent +
                ", yComponent=" + yComponent +
                ", zComponent=" + zComponent +
                ", sampleTime=" + sampleTime +
                '}';
    }

}
