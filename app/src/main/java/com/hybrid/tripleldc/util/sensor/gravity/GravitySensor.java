package com.hybrid.tripleldc.util.sensor.gravity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.GravityAcceleration;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.sensor.BaseSensor;
import com.hybrid.tripleldc.util.system.DateUtil;

import io.realm.Realm;

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
    protected void activeSensor() {
        Realm realm = Realm.getDefaultInstance();
        Number gravityAccelerationLatestID = realm.where(GravityAcceleration.class).max("id");
        this.gravityAccelerationLatestID = gravityAccelerationLatestID == null ? -1 : gravityAccelerationLatestID.intValue();
        realm.close();
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

    /**
     * 注册重力加速度传感器
     *
     * @return 是否支持
     */
    public Boolean registerGravitySensor() {
        isAvailable = true;

        // 注册加速度传感器
        if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_GAME)) {
            LogUtil.i(TAG, "重力加速度传感器可用！");
        } else {
            LogUtil.i(TAG, "重力加速度传感器不可用！");
            isAvailable = false;
        }
        return isAvailable;
    }

    /**
     * 注销重力加速度传感器监听
     */
    public void unregisterGravitySensor() {
        sensorManager.unregisterListener(this);
    }
}
