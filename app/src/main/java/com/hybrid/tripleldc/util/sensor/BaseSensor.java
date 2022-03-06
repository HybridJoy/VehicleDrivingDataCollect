package com.hybrid.tripleldc.util.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Author: Tasun
 * Time: 2020/8/7-21:10:32
 * Describe: sensor基类
 */

public abstract class BaseSensor implements SensorEventListener {
    private static final String TAG = "BaseSensor";
    // 默认频率控制倍率为1，即以传感器以原频率进行采样
    // 若该值不为1，如值设为2，则每2次输出一次传感器采样数据，即实际采样频率 = 原采样频率 / 缩小倍率
    public static final int DefaultZoomOutRatio = 1;
    // 传感器采样频率级别，以系统底层配置为准
    public static final int DefaultSamplingPeriodUs = SensorManager.SENSOR_DELAY_GAME;

    // 频率控制倍率
    private int zoomOutRatio = DefaultZoomOutRatio;
    // 当前次数
    private int currTimes = 0;

    // sensor是否可用
    protected boolean isAvailable = true;
    // sensor是否被激活
    private boolean isSensorActivated = false;

    protected SensorManager sensorManager;

    // 传感器采样频率
    protected int samplingPeriodUs = DefaultSamplingPeriodUs;

    public BaseSensor() {

    }

    public BaseSensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isSensorActivated) {
            return;
        }

        currTimes++;
        if (currTimes % zoomOutRatio == 0) {
            sensorChanged(event);
            currTimes = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        accuracyChanged(sensor, accuracy);
    }

    public abstract void register();

    public abstract void unregister();

    protected abstract void activeSensor();

    protected abstract void sensorChanged(SensorEvent event);

    protected abstract void accuracyChanged(Sensor event, int accuracy);

    public void setZoomOutRatio(int zoomOutRatio) {
        this.zoomOutRatio = zoomOutRatio;
    }

    public boolean setSensorState(boolean sensorActivated) {
        isSensorActivated = isAvailable && sensorActivated;
        if (isSensorActivated) {
            activeSensor();
        }
        return isSensorActivated;
    }

    public void resetFrequencyCount() {
        currTimes = 0;
    }
}
