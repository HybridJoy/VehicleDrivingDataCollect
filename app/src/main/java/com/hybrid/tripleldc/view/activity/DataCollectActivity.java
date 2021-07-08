package com.hybrid.tripleldc.view.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;

import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.GyroAngel;
import com.hybrid.tripleldc.databinding.ActivityDataCollectBinding;
import com.hybrid.tripleldc.service.DCService;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.system.AppUtil;
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

    private DCService mDCService;

    // request code
    private static final int PERMISSION_ACCESS_FINE_LOCATION_CODE = 1001;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION_CODE = 1002;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DCService.DCBinder mBinder = (DCService.DCBinder) service;
            mDCService = mBinder.getService();
            if (mDCService != null) {
                mDCService.setDataChangeCallback(dataChangeCallback);
            } else {
                LogUtil.e(TAG, "service lose!");
                throw new NullPointerException();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDCService = null;
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
        public void onGPSChanged(Location location) {
            LogUtil.d(TAG, "onGPSChanged");
            binding.dataDisplayArea.updateGPS(location.getLongitude(), location.getLatitude());
        }
    };

    private DCMainControlView.ControlCallback controlCallback = new DCMainControlView.ControlCallback() {
        @Override
        public boolean onDCStart() {
            boolean success = mDCService.startDC();
            if (success) {
                LogUtil.d(TAG, "start data collect");
                // 更新UI
                binding.mainControlArea.enableMainControl(true);
                binding.mainControlArea.setControlStatus(DCMainControlView.CollectionStatus.Start_Collect);
            } else {
                ToastUtil.showNormalToast("数据收集服务启动失败，请检查log，获得错误信息");
            }

            return success;
        }

        @Override
        public boolean onDCStop() {
            mDCService.endDC();
            LogUtil.d(TAG, "end data collect");

            // 更新UI
            binding.mainControlArea.enableMainControl(true);
            binding.mainControlArea.setControlStatus(DCMainControlView.CollectionStatus.Stop_Collect);

            return true;
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
        initDCService();
    }

    /**
     * 请求需要的权限
     */
    private void requestPermissions() {
        AppUtil.requestPermission(DataCollectActivity.this, Manifest.permission.ACCESS_FINE_LOCATION,
                PERMISSION_ACCESS_FINE_LOCATION_CODE, "应用需要位置信息，请授予该权限");
        AppUtil.requestPermission(DataCollectActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION,
                PERMISSION_ACCESS_COARSE_LOCATION_CODE, "应用需要位置信息，请授予该权限");
    }

    private void initDCService() {
        Intent bindServiceIntent = new Intent(this, DCService.class);
        bindService(bindServiceIntent, connection, BIND_AUTO_CREATE);
    }
}
