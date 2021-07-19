package com.hybrid.tripleldc.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.GPSPosition;
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
import com.hybrid.tripleldc.util.system.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class DCService extends Service implements AccelerationSensor.AccelerationCallback,
        OrientSensor.OrientCallBack,
        GyroSensor.GyroCallBack,
        GPSLocationListener {

    private static final String TAG = "DataCollectService";

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
    List<GPSPosition> gpsPositions = new ArrayList<>(); // GPS数据

    private DataChangeCallback dataChangeCallback;

    public interface DataChangeCallback {
        void onAccChanged(Acceleration acceleration);

        void onGyroChanged(GyroAngel gyroAngel);

        void onGPSChanged(GPSPosition position);
    }

    /**
     * 加速度传感器更新回调
     *
     * @param acceleration 加速度
     */
    @Override
    public void Acceleration(Acceleration acceleration) {
        accelerations.add(acceleration);
        dataChangeCallback.onAccChanged(acceleration);
    }

    /**
     * 方向传感器更新回调
     *
     * @param orientation 方向
     */
    @Override
    public void Orient(Orientation orientation) {
        orientations.add(orientation);
    }

    /**
     * 陀螺仪更新回调
     *
     * @param gyro 角速度
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
        // todo 这里损失了很多数据，可以作为一个优化点
        GPSPosition position = new GPSPosition(location.getLongitude(), location.getLatitude());
        position.setSampleTime(DateUtil.getTimestampString(location.getTime()));
        gpsPositions.add(position);
        dataChangeCallback.onGPSChanged(position);
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
        return new DCBinder();
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

        // activate inertial sensor
        boolean isAcceleratorActivated = mAccelerationSensor.setSensorState(true);
        boolean isOrientActivated = mOrientSensor.setSensorState(true);
        boolean isGyroActivated = mGyroSensor.setSensorState(true);

        isSensorActivated = isAcceleratorActivated && isGyroActivated && isOrientActivated;
        LogUtil.d(TAG, String.format("activate sensors %s, current sensor frequency: %s", isSensorActivated, sensorFrequency));

        // activate gps
        isGPSLocationOpened = gpsLocationManager.start(this, true);
        LogUtil.d(TAG, String.format("open GPS location %s", isGPSLocationOpened));

        boolean success = isSensorActivated && isGPSLocationOpened;
        if (!success) {
            LogUtil.e(TAG, String.format("Data Collection start failed, please check %s%s",
                    isSensorActivated ? "" : "inertial sensors ", isGPSLocationOpened ? "" : "GPS service"));
            // 启动失败要关闭已经开启的服务
            if (isSensorActivated) {
                mAccelerationSensor.setSensorState(false);
                mOrientSensor.setSensorState(false);
                mGyroSensor.setSensorState(false);
            }

            if (isGPSLocationOpened) {
                gpsLocationManager.stop();
            }
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
        gpsLocationManager.stop();

        isSensorActivated = false;
        isGPSLocationOpened = false;

        resetSensorData();

        LogUtil.d(TAG, "Data Collection end!");
    }

    /**
     * 获取x,y,z三轴加速度
     *
     * @return 返回时间片内缓存的加速度数据
     */
//    public List<Float[]> getAcceleration() {
//        LogUtil.d(TAG, "accelerations size: " + accelerations.size());
//        List<Float[]> accData = new ArrayList<>();
//        for (Acceleration acc : accelerations) {
//            accData.add(acc.getValue());
//        }
//        return accData;
//    }
    public List<Acceleration> getAcceleration() {
        LogUtil.d(TAG, "accelerations size: " + accelerations.size());
        return accelerations;
    }

    /**
     * 获取电子罗盘方向
     *
     * @return 返回时间片内缓存的方向数据
     */
    public List<Orientation> getOrientation() {
        LogUtil.d(TAG, "orientation size: " + orientations.size());
        return orientations;
    }

    /**
     * 获取陀螺仪偏差
     *
     * @return 返回时间片内缓存的角速度数据
     */
    public List<GyroAngel> getGyroAngel() {
        LogUtil.d(TAG, "gyroAngels size: " + gyroAngels.size());
        return gyroAngels;
    }

    /**
     * 获取GPS定位信息
     *
     * @return 返回时间片内缓存的GPS数据
     */
    public List<GPSPosition> getGPSPosition() {
        LogUtil.d(TAG, "GPSPosition size: " + gpsPositions.size());
        return gpsPositions;
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
        // inertial sensors data clear
        accelerations.clear();
        orientations.clear();
        gyroAngels.clear();

        // GPS sensor data clear
        gpsPositions.clear();

        // reset frequency control count
        mAccelerationSensor.resetFrequencyCount();
        mOrientSensor.resetFrequencyCount();
        mGyroSensor.resetFrequencyCount();
    }

    public void setDataChangeCallback(DataChangeCallback dataChangeCallback) {
        this.dataChangeCallback = dataChangeCallback;
    }
}
