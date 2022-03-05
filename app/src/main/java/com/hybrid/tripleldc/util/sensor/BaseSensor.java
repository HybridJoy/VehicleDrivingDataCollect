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
    public static final int Default_Frequency = 1;

    // 频率控制
    private int frequency = Default_Frequency;

    // 当前次数
    private int currTimes = 0;

    // sensor是否可用
    protected boolean isAvailable = true;
    // sensor是否被激活
    private boolean isSensorActivated = false;

    protected SensorManager sensorManager;

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
        if (currTimes % frequency == 0) {
            sensorChanged(event);
            currTimes = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        accuracyChanged(sensor, accuracy);
    }

    protected abstract void activeSensor();

    protected abstract void sensorChanged(SensorEvent event);

    protected abstract void accuracyChanged(Sensor event, int accuracy);

    public void setFrequency(int frequency) {
        this.frequency = frequency;
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
