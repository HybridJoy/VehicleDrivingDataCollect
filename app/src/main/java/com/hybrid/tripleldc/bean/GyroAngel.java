package com.hybrid.tripleldc.bean;

import com.hybrid.tripleldc.bean.base.BaseSensorData;

import org.jetbrains.annotations.NotNull;

/**
 * Author: Tasun
 * Time: 2020/8/7-20:15:06
 */
public class GyroAngel extends BaseSensorData {
    private float xComponent;
    private float yComponent;
    private float zComponent;

    public GyroAngel() {

    }

    /**
     * 角速度三维读数 float[]大小必须为3，且以x,y,z的顺序存值
     *
     * @param gyroangel 角速度三维读数
     */
    public GyroAngel(float[] gyroangel) {
        setValue(gyroangel);
    }

    /**
     * 角速度三维读数 float[]大小必须为3，且以x,y,z的顺序存值
     *
     * @param gyroangel 角速度三维读数
     */
    public void setValue(float[] gyroangel) {
        xComponent = gyroangel[0];
        yComponent = gyroangel[1];
        zComponent = gyroangel[2];
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
        return "GyroAngel{" +
                "xComponent=" + xComponent +
                ", yComponent=" + yComponent +
                ", zComponent=" + zComponent +
                ", sampleTime=" + sampleTime +
                '}';
    }

}
