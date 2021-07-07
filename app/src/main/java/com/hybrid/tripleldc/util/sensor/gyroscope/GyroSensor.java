package com.hybrid.tripleldc.util.sensor.gyroscope;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.hybrid.tripleldc.bean.GyroAngel;
import com.hybrid.tripleldc.util.sensor.BaseSensor;


public class GyroSensor extends BaseSensor {
    private static final String TAG = "GyroSensor";
    private GyroCallBack gyroCallBack;
    float[] gyroValues = new float[3];
    float[] angleXYZ = new float[3];
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;

    /**
     * 陀螺仪更新回调
     */
    public interface GyroCallBack {
        void Gyro(GyroAngel gyro);
    }

    public GyroSensor(Context context, GyroCallBack gyroCallBack) {
        super(context);
        this.gyroCallBack = gyroCallBack;
    }

    /**
     * 注册陀螺仪
     *
     * @return 是否支持陀螺仪功能
     */
    public Boolean registerGyro() {
        isAvailable = true;

        // 注册陀螺仪
        if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_GAME)) {
            Log.i(TAG, "陀螺仪传感器可用！");
        } else {
            Log.i(TAG, "陀螺仪传感器不可用！");
            isAvailable = false;
        }
        return isAvailable;
    }

    /**
     * 注销陀螺仪监听器
     */
    public void unregisterGyro() {
        sensorManager.unregisterListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void activeSensor() {
        gyroValues = new float[3]; // 重置数据
        timestamp = SystemClock.elapsedRealtimeNanos();
    }

    @Override
    protected void sensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // values[0] x方向上的角速度
            // values[1] y方向上的角速度
            // values[2] z方向上的角速度
            if (timestamp != 0) {
                // 得到两次检测到手机旋转的时间差（纳秒），并将其转化为秒
                final float dT = (event.timestamp - timestamp) * NS2S;
                // 将手机在各个轴上的旋转角度相加，即可得到当前位置相对于初始位置的旋转弧度
                for (int i = 0; i <= 2; i++) {
                    gyroValues[i] += event.values[i] * dT;
                    // 将弧度转化为角度
                    angleXYZ[i] = (float) Math.toDegrees(gyroValues[i]);
                }

                GyroAngel gyroAngel = new GyroAngel(angleXYZ);
                gyroAngel.setTimestamp(System.currentTimeMillis());
                gyroCallBack.Gyro(gyroAngel);
            }
            timestamp = event.timestamp;
        }
    }

    @Override
    protected void accuracyChanged(Sensor event, int accuracy) {

    }
}
