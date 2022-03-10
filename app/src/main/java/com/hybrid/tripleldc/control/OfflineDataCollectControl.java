package com.hybrid.tripleldc.control;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.hybrid.tripleldc.bean.AngularRate;
import com.hybrid.tripleldc.bean.DataCollectConfig;
import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.bean.InertialSequence;
import com.hybrid.tripleldc.bean.LinearAcceleration;
import com.hybrid.tripleldc.service.DCService;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.io.RealmHelper;
import com.hybrid.tripleldc.view.activity.DataCollectActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: Joy
 * Created Time: 2022/3/4-15:54
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2022/3/4 )
 * <p>
 * Describe:
 */
public class OfflineDataCollectControl implements DCService.DataChangeCallback {
    private static final String TAG = "OfflineDataCollectControl";

    private String deviceName;

    // DataCollect
    private DCService dataCollectService;
    private boolean isDataCollecting = false;

    // UI Thread Handler
    private final Handler uiThreadHandler;

    private enum NotifyType {
        DefaultError,
        TipsMessage,
        ToastMessage,
        AccUpdate,
        GyroUpdate,
        GpsUpdate,
        ConfigShow
    }

    private Handler dataProcessHandler;
    private int intervalCollectData = DefaultIntervalCollectData;

    // data process handler message and delay
    private static final int MsgCollectData = 1;
    private static final int DefaultIntervalCollectData = 5000;
    private static final int MsgDeviceNameChange = 3;

    public OfflineDataCollectControl(Handler mainThreadHandler) {
        this.uiThreadHandler = mainThreadHandler;

        this.deviceName = RealmHelper.getInstance().getDeviceName();
        initDataProcessHandler();
    }

    public void release() {
        dataProcessHandler.removeCallbacksAndMessages(null);
        dataProcessHandler = null;

        dataCollectService.enableService(false);
        dataCollectService = null;
    }

    @Override
    public void onAccChanged(LinearAcceleration acceleration) {
        notifyUIUpdate(OfflineDataCollectControl.NotifyType.AccUpdate, acceleration.getValue());
    }

    @Override
    public void onGyroChanged(AngularRate angularRate) {
        notifyUIUpdate(OfflineDataCollectControl.NotifyType.GyroUpdate, angularRate.getValue());
    }

    @Override
    public void onGPSChanged(GPSPosition position) {
        notifyUIUpdate(OfflineDataCollectControl.NotifyType.GpsUpdate, position.getValue());
    }

    /**
     * 数据收集服务状态发生变化时调用
     */
    public void notifyDataCollectServiceCanUse(DCService service, boolean canUse) {
        dataCollectService = service;
        if (!canUse) {
            return;
        }
        dataCollectService.enableService(true);
        dataCollectService.setDataChangeCallback(OfflineDataCollectControl.this);

        checkServiceSituation();
    }

    /**
     * 开启数据采集
     *
     * @return 数据采集服务是否开启成功
     */
    public boolean startDataCollect() {
        boolean success = dataCollectService.startDC();
        if (success) {
            // set workflow variable
            isDataCollecting = true;
            LogUtil.d(TAG, "start data collect");

            // 生成数据采集消息
            dataProcessHandler.sendEmptyMessageDelayed(MsgCollectData, intervalCollectData);
        }

        return success;
    }

    /**
     * 停止数据采集
     */
    public void stopDataCollect() {
        // set workflow variable
        isDataCollecting = false;

        // 移除当前存在的数据收集消息
        dataProcessHandler.removeMessages(MsgCollectData);
        // 立即进行一次数据收集
        dataProcessHandler.sendEmptyMessage(MsgCollectData);
        // 停止数据收集服务
        dataCollectService.endDC();
        LogUtil.d(TAG, "end data collect");
    }

    /**
     * 获取当前配置
     *
     * @return 当前配置 {@link DataCollectConfig}
     */
    public DataCollectConfig getCurrentConfig() {
        DataCollectConfig config = new DataCollectConfig();
        config.deviceName = this.deviceName;
        config.sensorFrequency = dataCollectService.getSensorFrequency();
        config.dataSampleInterval = this.intervalCollectData;
        return config;
    }

    /**
     * 更新配置
     */
    public void updateConfig(DataCollectConfig config) {
        LogUtil.d(TAG, "data collect config update");

        if (!this.deviceName.equals(config.deviceName)) {
            this.deviceName = config.deviceName;
            // 数据收集服务配置设备名
            dataCollectService.configDeviceName(deviceName);

            notifyUIUpdate(OfflineDataCollectControl.NotifyType.ToastMessage, "设备名改变");
            dataProcessHandler.sendEmptyMessage(MsgDeviceNameChange);
        }

        if (config.sensorFrequency > 0) {
            dataCollectService.configSensorFrequency(config.sensorFrequency);
        } else {
            notifyUIUpdate(OfflineDataCollectControl.NotifyType.ToastMessage, "设置传感器频率失败，需要大于0");
        }

        if (config.dataSampleInterval > 500) {
            this.intervalCollectData = config.dataSampleInterval;
        } else {
            notifyUIUpdate(OfflineDataCollectControl.NotifyType.ToastMessage, "设置数据采样间隔失败，需要大于500");
        }
    }

    /**
     * 初始化数据操作handler
     */
    private void initDataProcessHandler() {
        dataProcessHandler = new Handler(Looper.myLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MsgCollectData:
                        // 保存上个时间片的数据
                        saveData();
                        notifyUIUpdate(OfflineDataCollectControl.NotifyType.ToastMessage, "数据保存成功");

                        // 如果仍然在进行数据采集
                        if (isDataCollecting) {
                            // 下一次收集数据的时间
                            dataProcessHandler.sendEmptyMessageDelayed(MsgCollectData, intervalCollectData);
                        }
                        break;
                    case MsgDeviceNameChange:
                        // 更新设备名
                        RealmHelper.getInstance().updateDeviceName(deviceName);
                        notifyUIUpdate(OfflineDataCollectControl.NotifyType.ToastMessage, "设备名更改成功");
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void checkServiceSituation() {
        if (dataCollectService != null) {
            // 数据收集服务配置设备名
            dataCollectService.configDeviceName(deviceName);
            // 通知主线程
            notifyUIUpdate(OfflineDataCollectControl.NotifyType.ConfigShow, null);
        }
    }

    /**
     * 从 {@link DCService} 缓存区获取数据保存到数据库中
     */
    private void saveData() {
        InertialSequence inertialSequence = new InertialSequence();
        // 获得当前的数据
        inertialSequence.setAccelerations(dataCollectService.getAcceleration());
        inertialSequence.setAngularRates(dataCollectService.getAngularRate());
        inertialSequence.setOrientations(dataCollectService.getOrientation());
        inertialSequence.setGravityAccelerations(dataCollectService.getGravityAcceleration());
        inertialSequence.setLinearAccelerations(dataCollectService.getLinearAcceleration());
        inertialSequence.setGpsPositions(dataCollectService.getGPSPosition());

        // 数据装载完毕后，清空缓存
        dataCollectService.resetSensorData();

        // 存入数据库
        RealmHelper.getInstance().saveInertialSequence(inertialSequence, false);
    }

    /**
     * 需要更新UI时调用
     *
     * @param type 更新缘由
     * @param msg  备注 or 数据
     */
    private void notifyUIUpdate(@NotNull OfflineDataCollectControl.NotifyType type, @Nullable Object msg) {
        Message message = new Message();
        switch (type) {
            case DefaultError:
                message.what = DataCollectActivity.UIMessage.SHOW_ERROR_MESSAGE;
                break;
            case TipsMessage:
                message.what = DataCollectActivity.UIMessage.SHOW_TIPS_MESSAGE;
                break;
            case ToastMessage:
                message.what = DataCollectActivity.UIMessage.SHOW_TOAST_MESSAGE;
                break;
            case AccUpdate:
                message.what = DataCollectActivity.UIMessage.ACCELERATION_UPDATE;
                break;
            case GyroUpdate:
                message.what = DataCollectActivity.UIMessage.GYROANGEL_UPDATE;
                break;
            case GpsUpdate:
                message.what = DataCollectActivity.UIMessage.GPS_UPDATE;
                break;
            case ConfigShow:
                message.what = DataCollectActivity.UIMessage.SHOW_CONFIG_DIALOG;
                break;
        }

        message.obj = msg;
        uiThreadHandler.sendMessage(message);
    }
}