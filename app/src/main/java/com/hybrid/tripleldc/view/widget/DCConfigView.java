package com.hybrid.tripleldc.view.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hybrid.tripleldc.R;
import com.hybrid.tripleldc.bean.DataCollectConfig;
import com.hybrid.tripleldc.config.DataConst;
import com.hybrid.tripleldc.databinding.ViewDataCollectConfigBinding;

/**
 * Author: Joy
 * Created Time: 2021/7/21-18:01
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/21 )
 * <p>
 * Describe:
 */
public class DCConfigView extends LinearLayout {

    ViewDataCollectConfigBinding binding;
    private boolean enableDataUploadOptions = true;

    private ConfigChangeCallback configChangeCallback;
    public interface ConfigChangeCallback {
        void onChange(DataCollectConfig config);
        void onDeviceNameNotSet();
    }

    private OnClickListener onClickListener = v -> {
        switch (v.getId()) {
            case R.id.text_enter:
            case R.id.img_enter_background:
                if (TextUtils.isEmpty(binding.editDeviceName.getText().toString()) ||
                        binding.editDeviceName.getText().toString().equals(DataConst.System.DEFAULT_DEVICE_NAME)) {
                    configChangeCallback.onDeviceNameNotSet();
                    return;
                }
                configChangeCallback.onChange(readConfig());
                break;
        }
    };

    public DCConfigView(Context context) {
        super(context);
        initView(context);
    }

    public DCConfigView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DCConfigView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public DCConfigView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        binding = ViewDataCollectConfigBinding.inflate(LayoutInflater.from(context), DCConfigView.this, true);

        binding.textEnter.setOnClickListener(onClickListener);
        binding.imgEnterBackground.setOnClickListener(onClickListener);
    }

    public void setConfigChangeCallback(ConfigChangeCallback callback) {
        this.configChangeCallback = callback;
    }

    public void enableDataUploadOptions(boolean enable){
        this.enableDataUploadOptions = enable;
    }

    public void displayConfig(DataCollectConfig config) {
        binding.editDeviceName.setText(config.deviceName);
        binding.editSensorFrequency.setText(String.valueOf(config.sensorFrequency));
        binding.editDataSampleInterval.setText(String.valueOf(config.dataSampleInterval));

        if (enableDataUploadOptions) {
            showDataUploadOptions(true);

            binding.checkboxIsUploadData.setChecked(config.isUploadData);
            binding.checkboxIsUseTestServer.setChecked(config.isUseTestServer);
            binding.editDataUploadInterval.setText(String.valueOf(config.dataUploadInterval));
            binding.editMaxReUploadTimes.setText(String.valueOf(config.maxReUploadTimes));
        } else {
            showDataUploadOptions(false);
        }
    }

    private void showDataUploadOptions(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;

//        binding.textIsUseTestServer.setVisibility(visibility);
//        binding.textIsUploadData.setVisibility(visibility);
//        binding.textDataUploadInterval.setVisibility(visibility);
//        binding.textMaxReUploadTimes.setVisibility(visibility);

        binding.checkboxIsUploadData.setVisibility(visibility);
        binding.checkboxIsUseTestServer.setVisibility(visibility);
        binding.editDataUploadInterval.setVisibility(visibility);
        binding.editMaxReUploadTimes.setVisibility(visibility);
    }

    private DataCollectConfig readConfig() {
        DataCollectConfig config = new DataCollectConfig();
        config.deviceName = binding.editDeviceName.getText().toString();
        config.sensorFrequency = Integer.parseInt(binding.editSensorFrequency.getText().toString());
        config.dataSampleInterval = Integer.parseInt(binding.editDataSampleInterval.getText().toString());

        if (enableDataUploadOptions) {
            config.isUploadData = binding.checkboxIsUploadData.isChecked();
            config.isUseTestServer = binding.checkboxIsUseTestServer.isChecked();
            config.dataUploadInterval = Integer.parseInt(binding.editDataUploadInterval.getText().toString());
            config.maxReUploadTimes = Integer.parseInt(binding.editMaxReUploadTimes.getText().toString());
        }

        return config;
    }
}