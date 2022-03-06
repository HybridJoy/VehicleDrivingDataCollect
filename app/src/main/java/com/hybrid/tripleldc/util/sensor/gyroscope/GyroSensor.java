package com.hybrid.tripleldc.util.sensor.gyroscope;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.hybrid.tripleldc.bean.AngularRate;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.io.RealmHelper;
import com.hybrid.tripleldc.util.sensor.BaseSensor;
import com.hybrid.tripleldc.util.system.DateUtil;

import java.util.Locale;


public class GyroSensor extends BaseSensor {
    private static final String TAG = "GyroSensor";
    private final GyroCallBack gyroCallBack;
    private int angularRateLatestID = -1;

    /**
     * 陀螺仪更新回调
     */
    public interface GyroCallBack {
        void Gyro(AngularRate gyro);
    }

    public GyroSensor(Context context, GyroCallBack gyroCallBack) {
        super(context);
        this.gyroCallBack = gyroCallBack;
    }

    @Override
    public void register() {
        // 注册陀螺仪
        if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), samplingPeriodUs)) {
            LogUtil.i(TAG, "陀螺仪传感器可用！");
            isAvailable = true;
        } else {
            LogUtil.i(TAG, "陀螺仪传感器不可用！");
            isAvailable = false;
        }
    }

    @Override
    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void activeSensor() {
        this.angularRateLatestID = RealmHelper.getInstance().getInertialSensorDataLatestID(AngularRate.class);
        LogUtil.d(TAG, String.format(Locale.ENGLISH, "set angular rate latest id as %d", angularRateLatestID));
    }

    @Override
    protected void sensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // values[0] x方向上的角速度
            // values[1] y方向上的角速度
            // values[2] z方向上的角速度
            float[] angularRateValues = event.values.clone();
            AngularRate angularRate = new AngularRate(angularRateValues);
            angularRate.setId(++angularRateLatestID);
            angularRate.setSampleTime(DateUtil.getTimestampString(System.currentTimeMillis()));
            gyroCallBack.Gyro(angularRate);
        }
    }

    @Override
    protected void accuracyChanged(Sensor event, int accuracy) {

    }
}
