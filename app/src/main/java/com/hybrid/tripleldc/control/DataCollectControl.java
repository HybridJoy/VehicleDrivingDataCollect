package com.hybrid.tripleldc.control;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.DataCollectConfig;
import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.bean.GyroAngel;
import com.hybrid.tripleldc.bean.LaneChangeInfo;
import com.hybrid.tripleldc.config.DataConst;
import com.hybrid.tripleldc.config.UIConst;
import com.hybrid.tripleldc.service.DCService;
import com.hybrid.tripleldc.service.DUService;
import com.hybrid.tripleldc.util.TripleLDCUtil;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.system.DateUtil;
import com.hybrid.tripleldc.view.activity.DataCollectActivity.UIMessage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Author: Joy
 * Created Time: 2021/7/21-11:37
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/21 )
 * <p>
 * Describe: Controller part of {@link com.hybrid.tripleldc.view.activity.DataCollectActivity}
 */
public class DataCollectControl implements DUService.UploadCallback, DCService.DataChangeCallback {
    private static final String TAG = "DataCollectControl";

    // todo 改为获取设备名
    private String deviceName = DataConst.System.DEFAULT_DEVICE_NAME;
    // DataCollect
    private DCService dataCollectService;
    private boolean isDataCollecting = false;
    // 当前时间片变道数据
    private long currTimeSliceID;
    private LaneChangeInfo currLaneChangeInfo;
    // 需要上传的变道数据
    List<LaneChangeInfo> laneChangeInfoData;
    List<LaneChangeInfo> tempLaneChangeInfoData;

    // DataUpload
    private DUService dataUploadService;
    private boolean isNeedUploadData = true;
    private boolean isDataUploading = false;
    private int reUploadCount;
    private int maxReUploadTimes = Default_Max_Re_Upload_Times;
    // 最大重传次数
    private static final int Default_Max_Re_Upload_Times = 1000;

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
    private int intervalUploadData = DefaultIntervalUploadData;

    // data process handler message and delay
    private static final int MsgCollectData = 1;
    private static final int DefaultIntervalCollectData = 5000;
    private static final int MsgUploadData = 2;
    private static final int DefaultIntervalUploadData = 20000;
    private static final int MsgDeviceNameChange = 3;

    public DataCollectControl(Handler mainThreadHandler) {
        this.uiThreadHandler = mainThreadHandler;

        initDataProcessHandler();
        // default init
        currTimeSliceID = TripleLDCUtil.generateTimeSliceIDOriginByDate();
    }

    public void release() {
        dataProcessHandler.removeCallbacksAndMessages(null);
        dataProcessHandler = null;

        dataCollectService.enableService(false);
        dataCollectService = null;
        dataUploadService.enableService(false);
        dataUploadService = null;

        currLaneChangeInfo = null;
        if (laneChangeInfoData != null) {
            laneChangeInfoData.clear();
        }
        if (tempLaneChangeInfoData != null) {
            tempLaneChangeInfoData.clear();
        }
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        String requestTag = (String) call.request().tag();
        if (requestTag.equals(DataConst.RequestTag.REQUEST_UPLOAD_LANE_CHANGE_INFO_TAG)) {
            LogUtil.e(TAG, String.format("data upload failed, retry: %d", ++reUploadCount));
            // 数据重传机制
            // isDataUploading一直为true,新到数据持续写入 tempLaneChangeInfoData
            if (reUploadCount < maxReUploadTimes) {
                notifyUIUpdate(NotifyType.ToastMessage, String.format("数据上传失败，重试(%s)", reUploadCount));
                dataUploadService.uploadLaneChangeInfo(laneChangeInfoData, DataCollectControl.this);
            } else {
                // todo 写本地数据库操作

                LogUtil.e(TAG, String.format("retry time reach to max(%d), write to local DB", maxReUploadTimes));
            }
        } else {
            // 默认显示错误消息
            notifyUIUpdate(NotifyType.DefaultError, e.getMessage());
        }
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        boolean success = response.isSuccessful();
        String requestTag = (String) response.request().tag();
        LogUtil.e(TAG, String.format("%s %s, code: %s", response.request().url(), success ? "success" : "fail", response.code()));

        if (requestTag.equals(DataConst.RequestTag.REQUEST_TEST_SERVER_CONNECT_TAG)) { // 测试服务器连接
            notifyUIUpdate(NotifyType.TipsMessage, success ? UIConst.DialogMessage.TEST_SERVER_CONNECT_SUCCESSFUL : UIConst.DialogMessage.TEST_SERVER_CONNECT_FAILED);
        } else if (requestTag.equals(DataConst.RequestTag.REQUEST_GET_LATEST_TIME_SLICE_ID_TAG)) { // 获取最新时间片ID
            if (!success) {
                return;
            }

            String serverLatestTimeSliceID;
            try {
                serverLatestTimeSliceID = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                LogUtil.e(TAG, "parse data error");
                return;
            }

            if (serverLatestTimeSliceID.equals("0")) {
                notifyUIUpdate(NotifyType.ToastMessage, String.format("服务器时间无效，无须校正，当前时间片ID为 %s", currTimeSliceID));
            } else {
                long serverLatestTimeSliceIDLong = Long.parseLong(serverLatestTimeSliceID);
                if (serverLatestTimeSliceIDLong / 10000 == currTimeSliceID / 10000) {
                    currTimeSliceID = Long.parseLong(serverLatestTimeSliceID);
                    notifyUIUpdate(NotifyType.ToastMessage, String.format("校正时间片ID为 %s", serverLatestTimeSliceID));
                } else {
                    notifyUIUpdate(NotifyType.ToastMessage, String.format("服务器最新数据采集时间 %s", serverLatestTimeSliceIDLong / 10000));
                }
            }
        } else if (requestTag.equals(DataConst.RequestTag.REQUEST_UPLOAD_LANE_CHANGE_INFO_TAG)) { // 上传变道数据
            if (!success) {
                return;
            }

            // 成功就重置重传计数
            reUploadCount = 0;

            // 数据上传成功的逻辑
            // set workflow variable
            isDataUploading = false;
            laneChangeInfoData.clear();
            laneChangeInfoData.addAll(tempLaneChangeInfoData);
            tempLaneChangeInfoData = new ArrayList<>();

            notifyUIUpdate(NotifyType.ToastMessage, "数据上传成功");
            LogUtil.d(TAG, String.format("upload lane change data success, get temp data (%s)", laneChangeInfoData.size()));
        }

        // 主动关闭response
        response.close();
    }

    @Override
    public void onAccChanged(Acceleration acceleration) {
        notifyUIUpdate(NotifyType.AccUpdate, acceleration.getValue());
    }

    @Override
    public void onGyroChanged(GyroAngel gyroAngel) {
        notifyUIUpdate(NotifyType.GyroUpdate, gyroAngel.getValue());

    }

    @Override
    public void onGPSChanged(GPSPosition position) {
        notifyUIUpdate(NotifyType.GpsUpdate, position.getValue());
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
        dataCollectService.setDataChangeCallback(DataCollectControl.this);

        checkServiceSituation();
    }

    /**
     * 数据上传服务状态发生变化时调用
     */
    public void notifyDataUploadServiceCanUse(DUService service, boolean canUse) {
        dataUploadService = service;
        if (!canUse) {
            return;
        }
        dataUploadService.enableService(true);
        dataUploadService.testServerConnect("", DataCollectControl.this);

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

            // init data cache
            if (laneChangeInfoData == null) {
                laneChangeInfoData = new ArrayList<>();
            }
            if (tempLaneChangeInfoData == null) {
                tempLaneChangeInfoData = new ArrayList<>();
            }

            // 生成数据载体
            currLaneChangeInfo = new LaneChangeInfo(deviceName, ++currTimeSliceID, intervalCollectData, DateUtil.getTimestampString(System.currentTimeMillis()));
            // 生成数据采集消息
            dataProcessHandler.sendEmptyMessageDelayed(MsgCollectData, intervalCollectData);
            // 生成数据上传消息
            if (isNeedUploadData && !dataProcessHandler.hasMessages(MsgUploadData)) { // 需要上传数据 且当前没有数据上传消息
                dataProcessHandler.sendEmptyMessageDelayed(MsgUploadData, intervalUploadData);
            }
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
     * 设置变道标记
     *
     * @param isLeftChange 是否是左变道
     */
    public void setLaneChangedFlag(boolean isLeftChange) {
        // 变道标记
        currLaneChangeInfo.setLaneChanged(true);
        currLaneChangeInfo.setLaneChangedType(isLeftChange ? LaneChangeInfo.LaneChangeType.LEFT : LaneChangeInfo.LaneChangeType.RIGHT);
        currLaneChangeInfo.setLaneChangedTime(DateUtil.getTimestampString(System.currentTimeMillis()));
    }

    /**
     * 获取当前配置
     * @return
     */
    public DataCollectConfig getCurrentConfig() {
        DataCollectConfig config = new DataCollectConfig();
        config.deviceName = this.deviceName;
        config.isUploadData = this.isNeedUploadData;
        config.isUseTestServer = dataUploadService.isUseTestServer();
        config.sensorFrequency = dataCollectService.getSensorFrequency();
        config.dataSampleInterval = this.intervalCollectData;
        config.dataUploadInterval = this.intervalUploadData;
        config.maxReUploadTimes = this.maxReUploadTimes;
        return config;
    }

    /**
     * 更新配置
     */
    public void updateConfig(DataCollectConfig config) {
        LogUtil.d(TAG, "data collect config update");

        if (!this.deviceName.equals(config.deviceName)) {
            this.deviceName = config.deviceName;
            notifyUIUpdate(NotifyType.ToastMessage, "设备名改变");
            dataProcessHandler.sendEmptyMessage(MsgDeviceNameChange);
        }
        this.isNeedUploadData = config.isUploadData;
        dataUploadService.setUseTestServer(config.isUseTestServer);

        if (config.sensorFrequency > 0) {
            dataCollectService.configSensorFrequency(config.sensorFrequency);
        } else {
            notifyUIUpdate(NotifyType.ToastMessage, "设置传感器频率失败，需要大于0");
        }

        if (config.dataSampleInterval > 500) {
            this.intervalCollectData = config.dataSampleInterval;
        } else {
            notifyUIUpdate(NotifyType.ToastMessage, "设置数据采样间隔失败，需要大于500");
        }

        if (config.dataUploadInterval > 5000) {
            this.intervalUploadData = config.dataUploadInterval;
        } else {
            notifyUIUpdate(NotifyType.ToastMessage, "设置数据上传间隔失败，需要大于5000");
        }

        if (config.maxReUploadTimes > 5) {
            this.maxReUploadTimes = config.maxReUploadTimes;
        } else {
            notifyUIUpdate(NotifyType.ToastMessage, "设置失败最大重传次数失败，需要大于5");
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
                        loadData();
                        if (isDataUploading) {
                            tempLaneChangeInfoData.add(currLaneChangeInfo);
                            LogUtil.d(TAG, String.format("data uploading, data add in temp, temp size: %s", tempLaneChangeInfoData.size()));
                        } else {
                            laneChangeInfoData.add(currLaneChangeInfo);
                        }

                        // 如果仍然在进行数据采集
                        if (isDataCollecting) {
                            // 加入缓存后生成下一次的数据载体
                            currLaneChangeInfo = new LaneChangeInfo(deviceName, ++currTimeSliceID, intervalCollectData, DateUtil.getTimestampString(System.currentTimeMillis()));
                            // 下一次收集数据的时间
                            dataProcessHandler.sendEmptyMessageDelayed(MsgCollectData, intervalCollectData);
                        }
                        break;
                    case MsgUploadData:
                        if (!isNeedUploadData) {
                            return;
                        }

                        // set workflow variable
                        isDataUploading = true;
                        // 添加提示
                        notifyUIUpdate(NotifyType.ToastMessage, "数据上传中，请勿断开网络或离开本界面");
                        dataUploadService.uploadLaneChangeInfo(laneChangeInfoData, DataCollectControl.this);

                        if (isDataCollecting) {
                            // 下一次上传数据的时间
                            dataProcessHandler.sendEmptyMessageDelayed(MsgUploadData, intervalUploadData);
                        }
                        break;
                    case MsgDeviceNameChange:
                        // 更新时间片ID
                        dataUploadService.getLatestTimeSliceID(deviceName, DataCollectControl.this);
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void checkServiceSituation() {
        if (dataCollectService != null && dataUploadService != null) {
            notifyUIUpdate(NotifyType.ConfigShow, null);
        }
    }

    /**
     * 从 {@link DCService} 缓存区获取数据装载到 {@link LaneChangeInfo} 中
     */
    private void loadData() {
        currLaneChangeInfo.setAccelerationData(dataCollectService.getAcceleration());
        currLaneChangeInfo.setGyroAngelData(dataCollectService.getGyroAngel());
        currLaneChangeInfo.setOrientationData(dataCollectService.getOrientation());
        currLaneChangeInfo.setGpsPositionData(dataCollectService.getGPSPosition());
        currLaneChangeInfo.setEndTime(DateUtil.getTimestampString(System.currentTimeMillis()));

        // 数据装载完毕后，清空缓存
        dataCollectService.resetSensorData();
    }

    /**
     * 需要更新UI时调用
     *
     * @param type 更新缘由
     * @param msg  备注 or 数据
     */
    private void notifyUIUpdate(@NotNull NotifyType type, @Nullable Object msg) {
        Message message = new Message();
        switch (type) {
            case DefaultError:
                message.what = UIMessage.SHOW_ERROR_MESSAGE;
                break;
            case TipsMessage:
                message.what = UIMessage.SHOW_TIPS_MESSAGE;
                break;
            case ToastMessage:
                message.what = UIMessage.SHOW_TOAST_MESSAGE;
                break;
            case AccUpdate:
                message.what = UIMessage.ACCELERATION_UPDATE;
                break;
            case GyroUpdate:
                message.what = UIMessage.GYROANGEL_UPDATE;
                break;
            case GpsUpdate:
                message.what = UIMessage.GPS_UPDATE;
                break;
            case ConfigShow:
                message.what = UIMessage.SHOW_CONFIG_DIALOG;
                break;
        }

        message.obj = msg;
        uiThreadHandler.sendMessage(message);
    }
}
