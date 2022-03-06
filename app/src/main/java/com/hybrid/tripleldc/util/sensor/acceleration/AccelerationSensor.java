package com.hybrid.tripleldc.util.sensor.acceleration;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.io.RealmHelper;
import com.hybrid.tripleldc.util.sensor.BaseSensor;
import com.hybrid.tripleldc.util.system.DateUtil;

import java.util.Locale;


public class AccelerationSensor extends BaseSensor {
    private static final String TAG = "AccelerationSensor";
    private final AccelerationCallback accelerationCallback;
    private int accelerationLatestID = -1;

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
    public void register() {
        // 注册加速度传感器
        if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), samplingPeriodUs)) {
            LogUtil.i(TAG, "加速度传感器可用！");
            isAvailable = true;
        } else {
            LogUtil.i(TAG, "加速度传感器不可用！");
            isAvailable = false;
        }
    }

    @Override
    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void activeSensor() {
        this.accelerationLatestID = RealmHelper.getInstance().getInertialSensorDataLatestID(Acceleration.class);
        LogUtil.d(TAG, String.format(Locale.ENGLISH, "set acceleration latest id as %d", accelerationLatestID));
    }

    @Override
    protected void sensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // values[0]  x方向加速度
            // values[1]  y方向加速度
            // values[2]  z方向加速度
            float[] accelerationValues = event.values.clone();
            Acceleration acceleration = new Acceleration(accelerationValues);
            acceleration.setId(++accelerationLatestID);
            acceleration.setSampleTime(DateUtil.getTimestampString(System.currentTimeMillis()));
            accelerationCallback.Acceleration(acceleration);
        }
    }

    @Override
    protected void accuracyChanged(Sensor sensor, int accuracy) {

    }
}
