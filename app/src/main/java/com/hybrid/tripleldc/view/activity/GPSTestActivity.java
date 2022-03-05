package com.hybrid.tripleldc.view.activity;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.hybrid.tripleldc.R;
import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.databinding.ActivityGpsTestBinding;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.location.GPSLocationListener;
import com.hybrid.tripleldc.util.location.GPSLocationManager;
import com.hybrid.tripleldc.util.location.GPSProviderStatus;
import com.hybrid.tripleldc.util.system.AppUtil;
import com.hybrid.tripleldc.view.activity.base.BaseActivity;

/**
 * Author: Joy
 * Created Time: 2021/7/7-19:26
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/7 )
 * <p>
 * Describe:
 */
public class GPSTestActivity extends BaseActivity implements GPSLocationListener {
    private static final String TAG = "GPSTestActivity";

    private ActivityGpsTestBinding binding;

    private GPSLocationManager gpsLocationManager;

    // request code
    private static final int PERMISSION_ACCESS_FINE_LOCATION_CODE = 1001;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION_CODE = 1002;


    /**
     * GPS更新回调
     *
     * @param position 更新位置后的新的Location对象
     */
    @Override
    public void UpdateLocation(GPSPosition position) {
        binding.timeTxt.setText(String.format("时间：%s", position.getSampleTime()));
        binding.longitudeText.setText(String.format("经度：%s", position.getLongitude()));
        binding.latitudeText.setText(String.format("纬度：%s", position.getLatitude()));
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

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_gps_btn:
                    gpsLocationManager.start(GPSTestActivity.this, true);
                    LogUtil.d(TAG,"GPS location start");
                    break;
                case R.id.stop_gps_btn:
                    gpsLocationManager.stop();
                    LogUtil.d(TAG,"GPS location end");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        binding = ActivityGpsTestBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        binding.displayText.setText("GPS 测试");

        initGPSLocation();
        initButtonClickListener();
        requestPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在onPause()方法终止定位
        gpsLocationManager.stop();
    }


    private void initGPSLocation() {
        gpsLocationManager = GPSLocationManager.getInstances(GPSTestActivity.this);
    }

    private void initButtonClickListener() {
        binding.startGpsBtn.setOnClickListener(clickListener);
        binding.stopGpsBtn.setOnClickListener(clickListener);
    }

    /**
     * 请求需要的权限
     */
    private void requestPermissions() {
        AppUtil.requestPermission(GPSTestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION,
                PERMISSION_ACCESS_FINE_LOCATION_CODE, "应用需要位置信息，请授予该权限");
        AppUtil.requestPermission(GPSTestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION,
                PERMISSION_ACCESS_COARSE_LOCATION_CODE, "应用需要位置信息，请授予该权限");
    }
}
