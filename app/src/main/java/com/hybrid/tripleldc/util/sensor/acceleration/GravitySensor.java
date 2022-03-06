package com.hybrid.tripleldc.util.sensor.acceleration;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import com.hybrid.tripleldc.bean.GravityAcceleration;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.io.RealmHelper;
import com.hybrid.tripleldc.util.sensor.BaseSensor;
import com.hybrid.tripleldc.util.system.DateUtil;

import java.util.Locale;

/**
 * Author: Joy
 * Created Time: 2022/3/4-22:20
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2022/3/4 )
 * <p>
 * Describe:
 */
public class GravitySensor extends BaseSensor {
    private static final String TAG = "GravitySensor";

    private final GravityCallback gravityCallback;
    private int gravityAccelerationLatestID = -1;

    /**
     * 更新回调
     */
    public interface GravityCallback {
        void Gravity(GravityAcceleration acceleration);
    }

    public GravitySensor(Context context, GravityCallback callback) {
        super(context);
        this.gravityCallback = callback;
    }

    @Override
    public void register() {
        // 注册重力加速度传感器
        if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), samplingPeriodUs)) {
            LogUtil.i(TAG, "重力加速度传感器可用！");
            isAvailable = true;
        } else {
            LogUtil.i(TAG, "重力加速度传感器不可用！");
            isAvailable = false;
        }
    }

    @Override
    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void activeSensor() {
        this.gravityAccelerationLatestID = RealmHelper.getInstance().getInertialSensorDataLatestID(GravityAcceleration.class);
        LogUtil.d(TAG, String.format(Locale.ENGLISH, "set gravity acceleration latest id as %d", gravityAccelerationLatestID));
    }

    @Override
    protected void sensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            // values[0]  x方向重力加速度分量
            // values[1]  y方向重力加速度分量
            // values[2]  z方向重力加速度分量
            float[] gravityValues = event.values.clone();
            GravityAcceleration acceleration = new GravityAcceleration(gravityValues);
            acceleration.setId(++gravityAccelerationLatestID);
            acceleration.setSampleTime(DateUtil.getTimestampString(System.currentTimeMillis()));
            gravityCallback.Gravity(acceleration);
        }
    }

    @Override
    protected void accuracyChanged(Sensor event, int accuracy) {

    }
}
