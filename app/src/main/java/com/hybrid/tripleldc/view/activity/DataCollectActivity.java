package com.hybrid.tripleldc.view.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;

import com.hybrid.tripleldc.R;
import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.bean.GyroAngel;
import com.hybrid.tripleldc.bean.LaneChangeInfo;
import com.hybrid.tripleldc.config.DataConst;
import com.hybrid.tripleldc.config.UIConst;
import com.hybrid.tripleldc.databinding.ActivityDataCollectBinding;
import com.hybrid.tripleldc.service.DCService;
import com.hybrid.tripleldc.service.DUService;
import com.hybrid.tripleldc.util.TripleLDCUtil;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.system.AppUtil;
import com.hybrid.tripleldc.util.ui.DialogUtil;
import com.hybrid.tripleldc.util.ui.ToastUtil;
import com.hybrid.tripleldc.view.activity.base.BaseActivity;
import com.hybrid.tripleldc.view.widget.DCMainControlView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Author: Joy
 * Created Time: 2021/7/7-20:47
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/7 )
 * <p>
 * Describe:
 */
public class DataCollectActivity extends BaseActivity {
    private static final String TAG = "DataCollectActivity";
    ActivityDataCollectBinding binding;

    // data collect and upload service
    private DCService mDCService;
    private DUService mDUService;

    // workflow control
    private boolean isDataCollecting = false;
    private boolean isDataUploading = false;
    // 重传次数
    private int reUploadCount = 0;

    // 当前时间片的变道数据
    private long currTimeSliceID;
    private LaneChangeInfo currLaneChangeInfo;

    // 需要上传的变道数据
    List<LaneChangeInfo> laneChangeInfoData;
    List<LaneChangeInfo> tempLaneChangeInfoData;

    // 最大重传次数
    private static final int Max_Re_Upload_Times = 9999;

    // permission request code
    private static final int PERMISSION_ACCESS_FINE_LOCATION_CODE = 1001;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION_CODE = 1002;
    private static final int PERMISSION_ACCESS_INTERNET_CODE = 1003;

    // handler message and delay
    private static final int MsgCollectData = 1;
    private static final int IntervalCollectData = 2000;
    private static final int MsgUploadData = 2;
    private static final int IntervalUploadData = 100000;

    private Handler mainHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MsgCollectData:
                    loadData();
                    if (isDataUploading) {
                        tempLaneChangeInfoData.add(currLaneChangeInfo);
                    } else {
                        laneChangeInfoData.add(currLaneChangeInfo);
                    }

                    // 如果仍然在进行数据采集
                    if (isDataCollecting) {
                        // 加入缓存后生成下一次的数据载体
                        currLaneChangeInfo = new LaneChangeInfo(++currTimeSliceID, IntervalCollectData);
                        // 下一次收集数据的时间
                        mainHandler.sendEmptyMessageDelayed(MsgCollectData, IntervalCollectData);
                    }
                    break;
                case MsgUploadData:
                    // set workflow variable
                    isDataUploading = true;
                    mDUService.uploadLaneChangeInfo(laneChangeInfoData, httpCallback);

                    if (isDataCollecting) {
                        // 下一次上传数据的时间
                        mainHandler.sendEmptyMessageDelayed(MsgUploadData, IntervalUploadData);
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    // data collect service connection
    private ServiceConnection mDCServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DCService.DCBinder mBinder = (DCService.DCBinder) service;
            mDCService = mBinder.getService();
            if (mDCService != null) {
                mDCService.setDataChangeCallback(dataChangeCallback);
            } else {
                LogUtil.e(TAG, "DC service lose!");
                throw new NullPointerException();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDCService = null;
            LogUtil.d(TAG, "DC service disconnect");
        }
    };

    // data upload service connection
    private ServiceConnection mDUServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DUService.DUBinder mBinder = (DUService.DUBinder) service;
            mDUService = mBinder.getService();
            if (mDUService != null) {
                // todo

            } else {
                LogUtil.e(TAG, "DU service lose!");
                throw new NullPointerException();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDUService = null;
            LogUtil.d(TAG, "DU service disconnect");
        }
    };

    /**
     * 网络请求回调在这里处理
     */
    private Callback httpCallback = new Callback() {
        @Override
        public void onFailure(final Call call, final IOException e) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    String requestTag = (String) call.request().tag();
                    if (requestTag.equals(DataConst.RequestTag.REQUEST_UPLOAD_LANE_CHANGE_INFO_TAG)) {
                        LogUtil.e(TAG, String.format("data upload failed, retry: %d", ++reUploadCount));
                        // 数据重传机制
                        // isDataUploading一直为true,新到数据持续写入 tempLaneChangeInfoData
                        if (reUploadCount < Max_Re_Upload_Times) {
                            mDUService.uploadLaneChangeInfo(laneChangeInfoData, httpCallback);
                        } else {
                            // todo 写本地数据库操作

                            LogUtil.e(TAG, String.format("retry time reach to max(%d), write to local DB", Max_Re_Upload_Times));
                        }
                    } else {
                        // 默认显示错误消息
                        showTipsDialog(e.getMessage(), true);
                    }
                }
            });
        }

        @Override
        public void onResponse(Call call, final Response response) throws IOException {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    String requestTag = (String) response.request().tag();
                    if (requestTag.equals(DataConst.RequestTag.REQUEST_TEST_SERVER_CONNECT_TAG)) {
                        showTipsDialog(UIConst.DialogMessage.TEST_SERVER_CONNECT_SUCCESSFUL, false);
                    } else if (requestTag.equals(DataConst.RequestTag.REQUEST_UPLOAD_LANE_CHANGE_INFO_TAG)) {
                        // 成功就重置重传计数
                        reUploadCount = 0;

                        // 数据上传成功的逻辑
                        // set workflow variable
                        isDataUploading = false;
                        laneChangeInfoData.clear();
                        laneChangeInfoData.addAll(tempLaneChangeInfoData);
                        tempLaneChangeInfoData.clear();

                        LogUtil.d(TAG, "upload lane change data success");
                    }
                }
            });
        }
    };

    private DCService.DataChangeCallback dataChangeCallback = new DCService.DataChangeCallback() {
        @Override
        public void onAccChanged(Acceleration acceleration) {
            // LogUtil.d(TAG, "onAccChanged");
            binding.dataDisplayArea.updateAcceleration(acceleration.getXComponent(), acceleration.getYComponent(), acceleration.getZComponent());
        }

        @Override
        public void onGyroChanged(GyroAngel gyroAngel) {
            // LogUtil.d(TAG, "onGyroChanged");
            binding.dataDisplayArea.updateGyro(gyroAngel.getXComponent(), gyroAngel.getYComponent(), gyroAngel.getZComponent());
        }

        @Override
        public void onGPSChanged(GPSPosition position) {
            // LogUtil.d(TAG, "onGPSChanged");
            binding.dataDisplayArea.updateGPS(position.getLongitude(), position.getLatitude());
        }
    };

    private DCMainControlView.ControlCallback controlCallback = new DCMainControlView.ControlCallback() {
        @Override
        public boolean onDCStart() {
            boolean success = mDCService.startDC();
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
                currLaneChangeInfo = new LaneChangeInfo(currTimeSliceID, IntervalCollectData);
                // 生成数据采集消息
                mainHandler.sendEmptyMessageDelayed(MsgCollectData, IntervalCollectData);
                // 生成数据上传消息
                if (!mainHandler.hasMessages(MsgUploadData)) {
                    mainHandler.sendEmptyMessageDelayed(MsgUploadData, IntervalUploadData);
                }

                // 更新UI
                binding.dataDisplayArea.startFlush();
                binding.mainControlArea.setControlStatus(DCMainControlView.CollectionStatus.Start_Collect);
            } else {
                ToastUtil.showNormalToast("数据收集服务启动失败，请检查log，获得错误信息");
            }

            binding.mainControlArea.enableMainControl(true);
            return success;
        }

        @Override
        public boolean onDCStop() {
            // set workflow variable
            isDataCollecting = false;

            // 移除当前存在的数据收集消息
            mainHandler.removeMessages(MsgCollectData);
            // 立即进行一次数据收集
            mainHandler.sendEmptyMessage(MsgCollectData);
            // 停止数据收集服务
            mDCService.endDC();
            LogUtil.d(TAG, "end data collect");

            // 更新UI
            binding.dataDisplayArea.endFlush();
            binding.mainControlArea.enableMainControl(true);
            binding.mainControlArea.setControlStatus(DCMainControlView.CollectionStatus.Stop_Collect);

            return true;
        }

        @Override
        public void onLaneChanged() {
            // 变道标记
            currLaneChangeInfo.setLaneChanged(true);
            currLaneChangeInfo.setLaneChangedTimestamp(System.currentTimeMillis());

            // 更新UI
            binding.mainControlArea.enableLaneChanged(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        binding = ActivityDataCollectBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        binding.mainControlArea.setControlCallback(controlCallback);

        requestPermissions();
        initServices();

        // todo currSliceID 需要先判断是不是同一天 通过服务器
        currTimeSliceID = TripleLDCUtil.generateTimeSliceIDOriginByDate();
    }

    @Override
    protected void onResume() {
        LogUtil.d(TAG, "onResume");
        super.onResume();

        // 修复打开GPS页面回来，操作区域无法点击的问题
        binding.mainControlArea.reset();
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy");

        // 移除所有消息
        mainHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    /**
     * 从 {@link DCService} 缓存区获取数据装载到 {@link LaneChangeInfo} 中
     */
    private void loadData() {
        currLaneChangeInfo.setAccelerationData(mDCService.getAcceleration());
        currLaneChangeInfo.setGyroAngelData(mDCService.getGyroAngel());
        currLaneChangeInfo.setGpsPositionData(mDCService.getGPSPosition());

        // 数据装载完毕后，清空缓存
        mDCService.resetSensorData();
    }

    /**
     * 请求需要的权限
     */
    private void requestPermissions() {
        AppUtil.requestPermission(DataCollectActivity.this, Manifest.permission.ACCESS_FINE_LOCATION,
                PERMISSION_ACCESS_FINE_LOCATION_CODE, "应用需要位置信息，请授予该权限");
        AppUtil.requestPermission(DataCollectActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION,
                PERMISSION_ACCESS_COARSE_LOCATION_CODE, "应用需要位置信息，请授予该权限");
        AppUtil.requestPermission(DataCollectActivity.this, Manifest.permission.INTERNET,
                PERMISSION_ACCESS_INTERNET_CODE, "应用需要访问网络资源，请授予该权限");
    }

    /**
     * 绑定数据收集和数据上传服务
     */
    private void initServices() {
        Intent bindDCServiceIntent = new Intent(this, DCService.class);
        bindService(bindDCServiceIntent, mDCServiceConnection, BIND_AUTO_CREATE);

        Intent bindDUServiceIntent = new Intent(this, DUService.class);
        bindService(bindDUServiceIntent, mDUServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 以对话框的形式展示提示信息
     *
     * @param msg        提示信息
     * @param isErrorMsg 是否是错误信息
     */
    private void showTipsDialog(String msg, boolean isErrorMsg) {
        DialogUtil.createDialog(this, isErrorMsg ? R.string.error_dialog_title : R.string.normal_dialog_title, msg, R.string.dialog_accept, -1).show();
    }
}
