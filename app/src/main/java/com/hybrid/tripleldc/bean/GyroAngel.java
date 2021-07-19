package com.hybrid.tripleldc.bean;

/**
 * Author: Tasun
 * Time: 2020/8/7-20:15:06
 */
public class GyroAngel {
    private int id;
    private float xComponent;
    private float yComponent;
    private float zComponent;
    private String sampleTime;

    public GyroAngel() {

    }

    /**
     * 加速度三维读数 float[]大小必须为3，且以x,y,z的顺序存值
     *
     * @param acceleration
     */
    public GyroAngel(float[] acceleration) {
        setValue(acceleration);
    }

    /**
     * 加速度三维读数 float[]大小必须为3，且以x,y,z的顺序存值
     *
     * @param acceleration
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

    public String getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(String sampleTime) {
        this.sampleTime = sampleTime;
    }

    public float truncation(float a) {
        return ((float) Math.round(a * 1000)) / 1000;
    }

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
