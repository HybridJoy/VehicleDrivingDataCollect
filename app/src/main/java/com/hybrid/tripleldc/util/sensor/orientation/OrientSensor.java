package com.hybrid.tripleldc.util.sensor.orientation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import com.hybrid.tripleldc.bean.Orientation;
import com.hybrid.tripleldc.util.sensor.BaseSensor;
import com.hybrid.tripleldc.util.system.DateUtil;


/**
 * 方向传感器
 */

public class OrientSensor extends BaseSensor {
    private static final String TAG = "OrientSensor";
    private OrientCallBack orientCallBack;
    float[] accelerometerValues = new float[3];
    float[] magneticValues = new float[3];
    double[] orientValues = new double[3];

    public interface OrientCallBack {
        /**
         * 方向回调
         */
        void Orient(Orientation orientation);
    }

    public OrientSensor(Context context, OrientCallBack orientCallBack) {
        super(context);
        this.orientCallBack = orientCallBack;
    }

    /**
     * 注册加速度传感器和地磁场传感器
     *
     * @return 是否支持方向功能
     */
    public Boolean registerOrient() {
        isAvailable = true;

        // 注册加速度传感器
        if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME)) {
            Log.i(TAG, "加速度传感器可用！");
        } else {
            Log.i(TAG, "加速度传感器不可用！");
            isAvailable = false;
        }

        // 注册地磁场传感器
        if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME)) {
            Log.i(TAG, "地磁传感器可用！");
        } else {
            Log.i(TAG, "地磁传感器不可用！");
            isAvailable = false;
        }
        return isAvailable;
    }

    /**
     * 注销方向监听器
     */
    public void unregisterOrient() {
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void activeSensor() {

    }

    @Override
    protected void sensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.clone();
            calcOrient(1);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues = event.values.clone();
            calcOrient(2);
        }
    }

    @Override
    protected void accuracyChanged(Sensor event, int accuracy) {

    }

    /**
     * 根据加速度传感器和地磁场传感器的数据计算方向
     */
    private void calcOrient(int tag) {
        float[] R = new float[9];
        float[] values = new float[3];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
        SensorManager.getOrientation(R, values);

        // values[0](0~359) 磁场北方向和y轴围绕z轴的角度 0=North, 90=East, 180=South, 270=West
        // values[1](-180~180) 绕x轴旋转
        // values[2](-90~90) 绕y轴旋转的角度
        double azimuth = Math.toDegrees(values[0]);
        if (azimuth < 0) {
            azimuth = azimuth + 360;
        }
        double pitch = Math.toDegrees(values[1]);
        double roll = Math.toDegrees(values[2]);

        orientValues[0] = azimuth;
        orientValues[1] = pitch;
        orientValues[2] = roll;

        Orientation orientation = new Orientation(orientValues);
        orientation.setTag(tag);
        orientation.setSampleTime(DateUtil.getTimestampString(System.currentTimeMillis()));
        orientCallBack.Orient(orientation);
    }
}
