package com.hybrid.tripleldc.util.sensor.acceleration;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.hybrid.tripleldc.bean.LinearAcceleration;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.io.RealmHelper;
import com.hybrid.tripleldc.util.sensor.BaseSensor;
import com.hybrid.tripleldc.util.system.DateUtil;

import java.util.Locale;

/**
 * Author: Joy
 * Created Time: 2022/3/5-17:14
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2022/3/5 )
 * <p>
 * Describe:
 */
public class LinearAccelerationSensor extends BaseSensor {
    private static final String TAG = "LinearAccelerationSensor";
    private final LinearAccelerationCallback accelerationCallback;
    private int linearAccelerationLatestID = -1;

    /**
     * 更新回调
     */
    public interface LinearAccelerationCallback {
        void LinearAcceleration(LinearAcceleration acceleration);
    }

    public LinearAccelerationSensor(Context context, LinearAccelerationCallback callback) {
        super(context);
        this.accelerationCallback = callback;
    }

    @Override
    protected void activeSensor() {
        this.linearAccelerationLatestID = RealmHelper.getInstance().getInertialSensorDataLatestID(LinearAcceleration.class);
        LogUtil.d(TAG, String.format(Locale.ENGLISH, "set linear acceleration latest id as %d", linearAccelerationLatestID));
    }

    @Override
    protected void sensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // values[0]  x方向加速度
            // values[1]  y方向加速度
            // values[2]  z方向加速度
            float[] accelerationValues = event.values.clone();
            LinearAcceleration acceleration = new LinearAcceleration(accelerationValues);
            acceleration.setId(++linearAccelerationLatestID);
            acceleration.setSampleTime(DateUtil.getTimestampString(System.currentTimeMillis()));
            accelerationCallback.LinearAcceleration(acceleration);
        }
    }

    @Override
    protected void accuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 注册线性加速度传感器
     *
     * @return 是否支持
     */
    public Boolean registerAccelerometer() {
        isAvailable = true;

        // 注册加速度传感器
        if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_GAME)) {
            LogUtil.i(TAG, "线性加速度传感器可用！");
        } else {
            LogUtil.i(TAG, "线性加速度传感器不可用！");
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
