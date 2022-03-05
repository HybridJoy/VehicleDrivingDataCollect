package com.hybrid.tripleldc.view.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;

import com.hybrid.tripleldc.R;
import com.hybrid.tripleldc.bean.AngularRate;
import com.hybrid.tripleldc.databinding.ActivityTestBinding;
import com.hybrid.tripleldc.service.DCService;
import com.hybrid.tripleldc.service.DUService;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.system.AppUtil;
import com.hybrid.tripleldc.view.activity.base.BaseActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Author: Joy
 * Created Time: 2021/7/19-23:40
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/19 )
 * <p>
 * Describe:
 */
public class TestActivity extends BaseActivity {

    private static final String TAG = "TestActivity";

    private ActivityTestBinding binding;

    // data collect and upload service
    private DCService mDCService;
    private DUService mDUService;

    // request code
    private static final int PERMISSION_ACCESS_FINE_LOCATION_CODE = 1001;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION_CODE = 1002;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_start_collect:
                    mDCService.startDC();
                    break;
                case R.id.btn_stop_collect:
                    mDCService.endDC();
                    break;
                case R.id.btn_data_upload:
                    List<AngularRate> angularRates = mDCService.getAngularRate();
                    mDUService.uploadGyroTest(angularRates, httpCallback);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 网络请求回调在这里处理
     */
    private final Callback httpCallback = new Callback() {
        @Override
        public void onFailure(@NotNull final Call call, @NotNull final IOException e) {

        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull final Response response) {

        }
    };

    // data collect service connection
    private final ServiceConnection mDCServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DCService.DCBinder mBinder = (DCService.DCBinder) service;
            mDCService = mBinder.getService();
            if (mDCService != null) {

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
    private final ServiceConnection mDUServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DUService.DUBinder mBinder = (DUService.DUBinder) service;
            mDUService = mBinder.getService();
            if (mDUService != null) {
                // 测试服务器连接
                mDUService.testServerConnect("", httpCallback);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        binding = ActivityTestBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        initButtonClickListener();
        requestPermissions();
        initServices();
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

    private void initButtonClickListener() {
        binding.btnStartCollect.setOnClickListener(clickListener);
        binding.btnStopCollect.setOnClickListener(clickListener);
        binding.btnDataUpload.setOnClickListener(clickListener);
    }

    /**
     * 请求需要的权限
     */
    private void requestPermissions() {
        AppUtil.requestPermission(TestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION,
                PERMISSION_ACCESS_FINE_LOCATION_CODE, "应用需要位置信息，请授予该权限");
        AppUtil.requestPermission(TestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION,
                PERMISSION_ACCESS_COARSE_LOCATION_CODE, "应用需要位置信息，请授予该权限");
    }
}
