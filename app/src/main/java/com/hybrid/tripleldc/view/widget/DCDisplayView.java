package com.hybrid.tripleldc.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hybrid.tripleldc.databinding.ViewDataCollectDisplayBinding;

/**
 * Author: Joy
 * Created Time: 2021/7/7-22:14
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/7 )
 * <p>
 * Describe:
 */
public class DCDisplayView extends LinearLayout {

    private ViewDataCollectDisplayBinding binding;

    public DCDisplayView(Context context) {
        super(context);
        initView(context);
    }

    public DCDisplayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DCDisplayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public DCDisplayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        binding = ViewDataCollectDisplayBinding.inflate(LayoutInflater.from(context), DCDisplayView.this, true);
    }

    public void updateAcceleration(float accelerationX, float accelerationY, float accelerationZ) {
        binding.textAccelerationX.setText(String.format("%.2f", accelerationX));
        binding.textAccelerationY.setText(String.format("%.2f", accelerationY));
        binding.textAccelerationZ.setText(String.format("%.2f", accelerationZ));
    }

    public void updateGyro(float GyroX, float GyroY, float GyroZ) {
        binding.textGyroX.setText(String.format("%.2f", GyroX));
        binding.textGyroY.setText(String.format("%.2f", GyroY));
        binding.textGyroZ.setText(String.format("%.2f", GyroZ));
    }

    public void updateGPS(double longitude, double latitude) {
        binding.textGpsLongitude.setText(String.format("%.3f", longitude));
        binding.textGpsLatitude.setText(String.format("%.3f", latitude));
    }
}
