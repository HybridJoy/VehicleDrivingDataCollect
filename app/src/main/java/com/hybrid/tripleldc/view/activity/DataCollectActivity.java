package com.hybrid.tripleldc.view.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.hybrid.tripleldc.R;
import com.hybrid.tripleldc.control.DataCollectControl;
import com.hybrid.tripleldc.databinding.ActivityDataCollectBinding;
import com.hybrid.tripleldc.service.DCService;
import com.hybrid.tripleldc.service.DUService;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.system.AppUtil;
import com.hybrid.tripleldc.util.ui.DialogUtil;
import com.hybrid.tripleldc.util.ui.ToastUtil;
import com.hybrid.tripleldc.view.activity.base.BaseActivity;
import com.hybrid.tripleldc.view.widget.DCMainControlView;

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

    private DataCollectControl dataCollectControl;

    // permission request code
    private static final int PERMISSION_ACCESS_FINE_LOCATION_CODE = 1001;
    private static final int PERMISSION_ACCESS_BACKGROUND_LOCATION_CODE = 1002;

    public static class UIMessage {
        private static final int BASIC_MESSAGE = 100;
        public static final int SHOW_ERROR_MESSAGE = BASIC_MESSAGE + 1;
        public static final int SHOW_TIPS_MESSAGE = BASIC_MESSAGE + 2;
        public static final int SHOW_TOAST_MESSAGE = BASIC_MESSAGE + 3;

        public static final int ACCELERATION_UPDATE = BASIC_MESSAGE + 4;
        public static final int GYROANGEL_UPDATE = BASIC_MESSAGE + 5;
        public static final int GPS_UPDATE = BASIC_MESSAGE + 6;
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UIMessage.SHOW_ERROR_MESSAGE:
                    showTipsDialog((String) msg.obj, true);
                    break;
                case UIMessage.SHOW_TIPS_MESSAGE:
                    showTipsDialog((String) msg.obj, false);
                    break;
                case UIMessage.SHOW_TOAST_MESSAGE:
                    ToastUtil.showNormalToast((String) msg.obj);
                    break;
                case UIMessage.ACCELERATION_UPDATE:
                    Float[] acceleration = (Float[]) msg.obj;
                    binding.dataDisplayArea.updateAcceleration(acceleration[0], acceleration[1], acceleration[2]);
                    break;
                case UIMessage.GYROANGEL_UPDATE:
                    Float[] gyro = (Float[]) msg.obj;
                    binding.dataDisplayArea.updateGyro(gyro[0], gyro[1], gyro[2]);
                    break;
                case UIMessage.GPS_UPDATE:
                    Double[] gps = (Double[]) msg.obj;
                    binding.dataDisplayArea.updateGPS(gps[0], gps[1]);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    // data collect service connection
    private final ServiceConnection mDCServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DCService.DCBinder mBinder = (DCService.DCBinder) service;
            DCService dcService = mBinder.getService();
            if (dcService != null) {
                dataCollectControl.notifyDataCollectServiceCanUse(dcService, true);
            } else {
                LogUtil.e(TAG, "DC service lose!");
                throw new NullPointerException();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dataCollectControl.notifyDataCollectServiceCanUse(null, false);
            LogUtil.d(TAG, "DC service disconnect");
        }
    };

    // data upload service connection
    private final ServiceConnection mDUServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DUService.DUBinder mBinder = (DUService.DUBinder) service;
            DUService duService = mBinder.getService();
            if (duService != null) {
                dataCollectControl.notifyDataUploadServiceCanUse(duService, true);
            } else {
                LogUtil.e(TAG, "DU service lose!");
                throw new NullPointerException();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dataCollectControl.notifyDataUploadServiceCanUse(null, false);
            LogUtil.d(TAG, "DU service disconnect");
        }
    };

    private final DCMainControlView.ControlCallback controlCallback = new DCMainControlView.ControlCallback() {
        @Override
        public boolean onDCStart() {
            // 开始数据采集
            boolean success = dataCollectControl.startDataCollect();
            if (success) {
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
            // 停止数据采集
            dataCollectControl.stopDataCollect();

            // 更新UI
            binding.dataDisplayArea.endFlush();
            binding.mainControlArea.enableMainControl(true);
            binding.mainControlArea.setControlStatus(DCMainControlView.CollectionStatus.Stop_Collect);

            return true;
        }

        @Override
        public void onLaneChanged(boolean isLeftChange) {
            // 变道标记
            dataCollectControl.setLaneChangedFlag(isLeftChange);

            // 操作反馈
            ToastUtil.showNormalToast(String.format("car %s lane changed", isLeftChange ? "left" : "right"));
            // 更新UI
            binding.mainControlArea.enableLaneChanged(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        binding = ActivityDataCollectBinding.inflate(LayoutInflater.from(this));
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 隐藏标题栏
        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            LogUtil.e(TAG, "hide action bar failed");
            e.printStackTrace();
        }
        setContentView(binding.getRoot());

        requestPermissions();
        initServices();

        binding.mainControlArea.setControlCallback(controlCallback);
        // 注入
        dataCollectControl = new DataCollectControl(mainHandler);
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

        dataCollectControl.release();

        super.onDestroy();
    }

    /**
     * 请求需要的权限
     */
    private void requestPermissions() {
        AppUtil.requestPermission(DataCollectActivity.this, Manifest.permission.ACCESS_FINE_LOCATION,
                PERMISSION_ACCESS_FINE_LOCATION_CODE, "应用运行时需要精确位置信息，请授予该权限");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppUtil.requestPermission(DataCollectActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    PERMISSION_ACCESS_BACKGROUND_LOCATION_CODE, "应用进入后台时同样需要位置信息，请授予该权限");
        }
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
