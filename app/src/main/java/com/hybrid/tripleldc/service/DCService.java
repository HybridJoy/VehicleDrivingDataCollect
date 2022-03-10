package com.hybrid.tripleldc.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.bean.AngularRate;
import com.hybrid.tripleldc.bean.GravityAcceleration;
import com.hybrid.tripleldc.bean.LinearAcceleration;
import com.hybrid.tripleldc.bean.Orientation;
import com.hybrid.tripleldc.config.DataConst;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.location.GPSLocationListener;
import com.hybrid.tripleldc.util.location.GPSLocationManager;
import com.hybrid.tripleldc.util.location.GPSProviderStatus;
import com.hybrid.tripleldc.util.sensor.BaseSensor;
import com.hybrid.tripleldc.util.sensor.acceleration.AccelerationSensor;
import com.hybrid.tripleldc.util.sensor.acceleration.LinearAccelerationSensor;
import com.hybrid.tripleldc.util.sensor.acceleration.GravitySensor;
import com.hybrid.tripleldc.util.sensor.gyroscope.GyroSensor;
import com.hybrid.tripleldc.util.sensor.orientation.OrientSensor;

import java.util.ArrayList;
import java.util.List;

public class DCService extends Service implements AccelerationSensor.AccelerationCallback,
        OrientSensor.OrientCallBack,
        GyroSensor.GyroCallBack,
        GravitySensor.GravityCallback,
        LinearAccelerationSensor.LinearAccelerationCallback,
        GPSLocationListener {

    private static final String TAG = "DataCollectService";

    private boolean enableService = false;
    private int sensorFrequency = BaseSensor.DefaultZoomOutRatio;
    private String deviceName = DataConst.System.DEFAULT_DEVICE_NAME;

    private boolean isSensorActivated = false; // 传感器是否激活
    private AccelerationSensor mAccelerationSensor; // 加速度传感器
    private LinearAccelerationSensor mLinearAccelerationSensor; // 线性加速度传感器
    private GyroSensor mGyroSensor; // 陀螺仪传感器
    private OrientSensor mOrientSensor; // 方向传感器
    private GravitySensor mGravitySensor; // 重力传感器

    private boolean isGPSLocationOpened = false; // 定位服务是否开启成功
    private GPSLocationManager gpsLocationManager; // GPS定位传感器

    List<Acceleration> accelerations = new ArrayList<>(); // 加速度传感器数据
    List<LinearAcceleration> linearAccelerations = new ArrayList<>(); // 线性加速度数据
    List<GravityAcceleration> gravityAccelerations = new ArrayList<>(); // 重力加速度数据
    List<Orientation> orientations = new ArrayList<>(); // 方向传感器数据
    List<AngularRate> angularRates = new ArrayList<>(); // 陀螺仪数据
    List<GPSPosition> gpsPositions = new ArrayList<>(); // GPS数据

    private DataChangeCallback dataChangeCallback;

    public interface DataChangeCallback {
        void onAccChanged(LinearAcceleration acceleration);

        void onGyroChanged(AngularRate angularRate);

        void onGPSChanged(GPSPosition position);
    }

    /**
     * 加速度传感器更新回调
     *
     * @param acceleration 加速度
     */
    @Override
    public void Acceleration(Acceleration acceleration) {
        acceleration.setDeviceName(deviceName);
        accelerations.add(acceleration);
    }

    /**
     * 方向传感器更新回调
     *
     * @param orientation 方向
     */
    @Override
    public void Orient(Orientation orientation) {
        orientation.setDeviceName(deviceName);
        orientations.add(orientation);
    }

    /**
     * 陀螺仪更新回调
     *
     * @param gyro 角速度
     */
    @Override
    public void Gyro(AngularRate gyro) {
        gyro.setDeviceName(deviceName);
        angularRates.add(gyro);
        if (dataChangeCallback != null) {
            dataChangeCallback.onGyroChanged(gyro);
        }
    }

    /**
     * 重力传感器更新回调
     *
     * @param gravityAcceleration
     */

    @Override
    public void Gravity(GravityAcceleration gravityAcceleration) {
        gravityAcceleration.setDeviceName(deviceName);
        gravityAccelerations.add(gravityAcceleration);
    }

    /**
     * 线性加速度传感器更新回调
     * @param acceleration
     */

    @Override
    public void LinearAcceleration(LinearAcceleration acceleration) {
        acceleration.setDeviceName(deviceName);
        linearAccelerations.add(acceleration);
        if (dataChangeCallback != null) {
            dataChangeCallback.onAccChanged(acceleration);
        }
    }

    /**
     * GPS更新回调
     *
     * @param position 更新位置后的新的GPSPosition对象
     */
    @Override
    public void UpdateLocation(GPSPosition position) {
        position.setDeviceName(deviceName);
        gpsPositions.add(position);
        if (dataChangeCallback != null) {
            dataChangeCallback.onGPSChanged(position);
        }
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
        mAccelerationSensor.register();
        mOrientSensor = new OrientSensor(this, this);
        mOrientSensor.register();
        mGyroSensor = new GyroSensor(this, this);
        mGyroSensor.register();
        mGravitySensor = new GravitySensor(this, this);
        mGravitySensor.register();
        mLinearAccelerationSensor = new LinearAccelerationSensor(this, this);
        mLinearAccelerationSensor.register();

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
        mAccelerationSensor.unregister();
        mOrientSensor.unregister();
        mGyroSensor.unregister();
        mGravitySensor.unregister();
        mLinearAccelerationSensor.unregister();

        gpsLocationManager.stop();
        LogUtil.d(TAG, "onDestroy executed");
    }

    /**
     * 激活服务
     *
     * @param enable 是否激活
     */
    public void enableService(boolean enable) {
        enableService = enable;
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
        if (!enableService) {
            LogUtil.i(TAG, "please enable service first!");
            return false;
        }

        if (isSensorActivated || isGPSLocationOpened) { // 如果上一次未正常结束数据收集，先结束上一轮数据收集
            endDC();
        }

        // activate inertial sensor
        boolean isAcceleratorActivated = mAccelerationSensor.setSensorState(true);
        boolean isOrientActivated = mOrientSensor.setSensorState(true);
        boolean isGyroActivated = mGyroSensor.setSensorState(true);
        boolean isGravityActivated = mGravitySensor.setSensorState(true);
        boolean isLinearAccelerationActivated = mLinearAccelerationSensor.setSensorState(true);

        isSensorActivated = isAcceleratorActivated && isGyroActivated && isOrientActivated && isGravityActivated && isLinearAccelerationActivated;
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
                mGravitySensor.setSensorState(false);
                mLinearAccelerationSensor.setSensorState(false);
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
        mGravitySensor.setSensorState(false);
        mLinearAccelerationSensor.setSensorState(false);
        gpsLocationManager.stop();

        isSensorActivated = false;
        isGPSLocationOpened = false;

        // 装载完数据后会清空数据，这里暂时不清空数据了
        // resetSensorData();

        LogUtil.d(TAG, "Data Collection end!");
    }

    /**
     * 获取x,y,z三轴加速度
     *
     * @return 返回时间片内缓存的加速度数据
     */
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
    public List<AngularRate> getAngularRate() {
        LogUtil.d(TAG, "angularRate size: " + angularRates.size());
        return angularRates;
    }

    /**
     * 获取重力加速度数据
     *
     * @return 返回时间片内缓存的重力加速度数据
     */
    public List<GravityAcceleration> getGravityAcceleration() {
        LogUtil.d(TAG, "gravity acceleration size: " + gravityAccelerations.size());
        return gravityAccelerations;
    }

    /**
     * 获取线性加速度数据
     * @return 返回时间片内缓存的线性加速度数据
     */
    public List<LinearAcceleration> getLinearAcceleration() {
        LogUtil.d(TAG, "linear acceleration size: " + linearAccelerations.size());
        return linearAccelerations;
    }

    /**
     * 获取GPS定位信息
     *
     * @return 返回时间片内缓存的GPS数据
     */
    public List<GPSPosition> getGPSPosition() {
        LogUtil.d(TAG, "gps position size: " + gpsPositions.size());
        return gpsPositions;
    }

    /**
     * 获取传感器频率
     *
     * @return 传感器频率
     */
    public int getSensorFrequency() {
        return this.sensorFrequency;
    }

    /**
     * 配置传感器频率
     *
     * @param frequency 在已有频率基础上的缩小倍数
     */
    public void configSensorFrequency(int frequency) {
        this.sensorFrequency = frequency;
        mAccelerationSensor.setZoomOutRatio(sensorFrequency);
        mOrientSensor.setZoomOutRatio(sensorFrequency);
        mGyroSensor.setZoomOutRatio(sensorFrequency);
        mGravitySensor.setZoomOutRatio(sensorFrequency);
        mLinearAccelerationSensor.setZoomOutRatio(sensorFrequency);

        LogUtil.d(TAG, "sensor frequency change to " + frequency);
    }

    /**
     * 配置设备名
     *
     * @param name 需要设置的设备名称
     */
    public void configDeviceName(String name) {
        this.deviceName = name;
        LogUtil.d(TAG, "device name change to " + deviceName);
    }

    /**
     * 重置传感器数据
     */
    public void resetSensorData() {
        // 把 clear() 改为 new ArrayList<>() 解决浅拷贝问题
        // inertial sensors data clear
        accelerations = new ArrayList<>();
        orientations = new ArrayList<>();
        angularRates = new ArrayList<>();
        gravityAccelerations = new ArrayList<>();
        linearAccelerations = new ArrayList<>();

        // GPS sensor data clear
        gpsPositions = new ArrayList<>();

        // reset frequency control count
        mAccelerationSensor.resetFrequencyCount();
        mOrientSensor.resetFrequencyCount();
        mGyroSensor.resetFrequencyCount();
        mGravitySensor.resetFrequencyCount();
        mLinearAccelerationSensor.resetFrequencyCount();
    }

    public void setDataChangeCallback(DataChangeCallback dataChangeCallback) {
        this.dataChangeCallback = dataChangeCallback;
    }
}