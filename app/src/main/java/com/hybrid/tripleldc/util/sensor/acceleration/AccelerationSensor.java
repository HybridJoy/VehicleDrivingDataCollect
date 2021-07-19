package com.hybrid.tripleldc.util.sensor.acceleration;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.sensor.BaseSensor;
import com.hybrid.tripleldc.util.system.DateUtil;


public class AccelerationSensor extends BaseSensor {
    private static final String TAG = "AccelerationSensor";
    private AccelerationCallback accelerationCallback;
    private float[] accelerationValues = new float[3];

    /**
     * 更新回调
     */
    public interface AccelerationCallback {
        void Acceleration(Acceleration acceleration);
    }

    public AccelerationSensor(Context context, AccelerationCallback callback) {
        super(context);
        this.accelerationCallback = callback;
    }

    @Override
    protected void activeSensor() {

    }

    @Override
    protected void sensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // values[0]  x方向加速度
            // values[1]  y方向加速度
            // values[2]  z方向加速度
            accelerationValues = event.values.clone();
            Acceleration acceleration = new Acceleration(accelerationValues);
            acceleration.setSampleTime(DateUtil.getTimestampString(System.currentTimeMillis()));
            accelerationCallback.Acceleration(acceleration);
        }
    }

    @Override
    protected void accuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 注册加速度传感器
     *
     * @return 是否支持
     */
    public Boolean registerAccelerometer() {
        isAvailable = true;

        // 注册加速度传感器
        if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME)) {
            LogUtil.i(TAG, "加速度传感器可用！");
        } else {
            LogUtil.i(TAG, "加速度传感器不可用！");
            isAvailable = false;
        }
        return isAvailable;
    }

    /**
     * 注销加速度传感器监听
     */
    public void unregisterAccelerometer() {
        sensorManager.unregisterListener(this);
    }
}
