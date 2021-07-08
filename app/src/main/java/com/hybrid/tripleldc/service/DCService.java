package com.hybrid.tripleldc.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.GyroAngel;
import com.hybrid.tripleldc.bean.Orientation;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.location.GPSLocationListener;
import com.hybrid.tripleldc.util.location.GPSLocationManager;
import com.hybrid.tripleldc.util.location.GPSProviderStatus;
import com.hybrid.tripleldc.util.sensor.BaseSensor;
import com.hybrid.tripleldc.util.sensor.acceleration.AccelerationSensor;
import com.hybrid.tripleldc.util.sensor.gyroscope.GyroSensor;
import com.hybrid.tripleldc.util.sensor.orientation.OrientSensor;

import java.util.ArrayList;
import java.util.List;

public class DCService extends Service implements AccelerationSensor.AccelerationCallback,
        OrientSensor.OrientCallBack,
        GyroSensor.GyroCallBack,
        GPSLocationListener {

    private static final String TAG = "DCService";

    private int sensorFrequency = BaseSensor.Default_Frequency;

    private boolean isSensorActivated = false; // 传感器是否激活
    private AccelerationSensor mAccelerationSensor; // 加速度传感器
    private GyroSensor mGyroSensor; // 陀螺仪传感器
    private OrientSensor mOrientSensor; // 方向传感器

    private boolean isGPSLocationOpened = false; // 定位服务是否开启成功
    private GPSLocationManager gpsLocationManager;

    List<Acceleration> accelerations = new ArrayList<>(); // 加速度传感器数据
    List<Orientation> orientations = new ArrayList<>(); // 方向传感器数据
    List<GyroAngel> gyroAngels = new ArrayList<>(); // 陀螺仪数据
    List<Location> gpsLocations = new ArrayList<>(); // GPS数据

    private DCBinder mBinder;

    private DataChangeCallback dataChangeCallback;
    public interface DataChangeCallback {
        void onAccChanged(Acceleration acceleration);
        void onGyroChanged(GyroAngel gyroAngel);
        void onGPSChanged(Location location);
    }

    /**
     * 加速度传感器更新回调
     *
     * @param acceleration
     */
    @Override
    public void Acceleration(Acceleration acceleration) {
        accelerations.add(acceleration);
        dataChangeCallback.onAccChanged(acceleration);
    }

    /**
     * 方向传感器更新回调
     *
     * @param orientation
     */
    @Override
    public void Orient(Orientation orientation) {
        orientations.add(orientation);
    }

    /**
     * 陀螺仪更新回调
     *
     * @param gyro
     */
    @Override
    public void Gyro(GyroAngel gyro) {
        gyroAngels.add(gyro);
        dataChangeCallback.onGyroChanged(gyro);
    }

    /**
     * GPS更新回调
     *
     * @param location 更新位置后的新的Location对象
     */
    @Override
    public void UpdateLocation(Location location) {
        gpsLocations.add(location);
        dataChangeCallback.onGPSChanged(location);
    }

    @Override
    public void UpdateStatus(String provider, int status, Bundle extras) {
        LogUtil.d(TAG, String.format("定位类型：%s", provider));
    }

    @Override
    public void UpdateGPSProviderStatus(int gpsStatus) {
        switch (gpsStatus) {
            case GPSProviderStatus.GPS_ENABLED:
                LogUtil.d(TAG, "GPS开启");
                break;
            case GPSProviderStatus.GPS_DISABLED:
                LogUtil.d(TAG, "GPS关闭");
                break;
            case GPSProviderStatus.GPS_OUT_OF_SERVICE:
                LogUtil.d(TAG, "GPS不可用");
                break;
            case GPSProviderStatus.GPS_TEMPORARILY_UNAVAILABLE:
                LogUtil.d(TAG, "GPS暂时不可用");
                break;
            case GPSProviderStatus.GPS_AVAILABLE:
                LogUtil.d(TAG, "GPS可用");
                break;
            default:
                break;
        }
    }

    public class DCBinder extends Binder {
        public DCService getService() {
            return DCService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mBinder = new DCBinder();
        return mBinder;
    }


    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate executed");

        // register inertial sensors
        mAccelerationSensor = new AccelerationSensor(this, this);
        mAccelerationSensor.registerAccelerometer();
        mOrientSensor = new OrientSensor(this, this);
        mOrientSensor.registerOrient();
        mGyroSensor = new GyroSensor(this, this);
        mGyroSensor.registerGyro();

        // open gps location
        gpsLocationManager = GPSLocationManager.getInstances(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAccelerationSensor.unregisterAccelerometer();
        mOrientSensor.unregisterOrient();
        mGyroSensor.unregisterGyro();

        gpsLocationManager.stop();
        LogUtil.d(TAG, "onDestroy executed");
    }

    /**
     * 数据收集服务是否开始
     * 由传感器是否激活和GPS定位是否正常开启来判断
     *
     * @return isSensorActivated
     */
    public boolean isDCRunning() {
        return isSensorActivated && isGPSLocationOpened;
    }

    /**
     * 数据收集服务：开启
     * 打开传感器数据接收器
     *
     * @return 是否开启成功
     */
    public boolean startDC() {
        if (isSensorActivated || isGPSLocationOpened) { // 如果上一次未正常结束数据收集，先结束上一轮数据收集
            endDC();
        }

        boolean isAcceleratorActivated = mAccelerationSensor.setSensorState(true);
        // boolean isOrientActivated = mOrientSensor.setSensorState(true);
        boolean isGyroActivated = mGyroSensor.setSensorState(true);

        isSensorActivated = isAcceleratorActivated && isGyroActivated;
        LogUtil.d(TAG, String.format("activate sensors %s, current sensor frequency: %s", isSensorActivated, sensorFrequency));


        isGPSLocationOpened = gpsLocationManager.start(this, true);
        LogUtil.d(TAG, String.format("open GPS location %s", isGPSLocationOpened));

        boolean success = isSensorActivated && isGPSLocationOpened;
        if (!success) {
            LogUtil.e(TAG, String.format("Data Collection start failed, please check %s%s",
                    isSensorActivated ? "" : "inertial sensors ", isGPSLocationOpened ? "" : "GPS service"));
        } else {
            LogUtil.d(TAG, "Data Collection start!");
        }
        return success;
    }

    /**
     * 数据收集服务：关闭
     * 重置传感器数据，关闭传感器
     */
    public void endDC() {
        mAccelerationSensor.setSensorState(false);
        mOrientSensor.setSensorState(false);
        mGyroSensor.setSensorState(false);
        resetSensorData();

        gpsLocationManager.stop();
        gpsLocations.clear();

        isSensorActivated = false;
        isGPSLocationOpened = false;

        LogUtil.d(TAG, "Data Collection end!");
    }

    /**
     * 获取x,y,z三轴加速度
     *
     * @return
     */
    public List<Float[]> getAcceleration() {
        LogUtil.d(TAG, "accelerations size: " + accelerations.size());
        List<Float[]> accData = new ArrayList<>();
        for (Acceleration acc : accelerations) {
            accData.add(acc.getValue());
        }
        return accData;
    }

    /**
     * 获取电子罗盘方向
     *
     * @return
     */
    public List<Double[]> getOrientation() {
        LogUtil.d(TAG, "orientations size: " + orientations.size());
        List<Double[]> orientData = new ArrayList<>();
        for (Orientation orient : orientations) {
            orientData.add(orient.getValue());
        }
        return orientData;
    }

    /**
     * 获取陀螺仪偏差
     *
     * @return
     */
    public List<Float[]> getGyroAngel() {
        LogUtil.d(TAG, "gyroAngels size: " + gyroAngels.size());
        List<Float[]> gyroData = new ArrayList<>();
        for (GyroAngel gyro : gyroAngels) {
            gyroData.add(gyro.getValue());
        }
        return gyroData;
    }

    /**
     * 配置传感器频率
     *
     * @param frequency 在已有频率基础上的缩小倍数
     */
    public void configSensorFrequency(int frequency) {
        this.sensorFrequency = frequency;
        mAccelerationSensor.setFrequency(sensorFrequency);
        mOrientSensor.setFrequency(sensorFrequency);
        mGyroSensor.setFrequency(sensorFrequency);

        LogUtil.d(TAG, "sensor frequency change to " + frequency);
    }

    /**
     * 重置传感器数据
     */
    public void resetSensorData() {
        accelerations.clear();
        orientations.clear();
        gyroAngels.clear();

        mAccelerationSensor.resetFrequencyCount();
        mOrientSensor.resetFrequencyCount();
        mGyroSensor.resetFrequencyCount();
    }

    public void setDataChangeCallback(DataChangeCallback dataChangeCallback) {
        this.dataChangeCallback = dataChangeCallback;
    }
}
