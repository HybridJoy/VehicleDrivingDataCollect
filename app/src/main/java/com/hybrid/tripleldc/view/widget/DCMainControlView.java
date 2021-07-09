package com.hybrid.tripleldc.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hybrid.tripleldc.R;
import com.hybrid.tripleldc.databinding.ViewDataCollectControlBinding;
import com.hybrid.tripleldc.util.ui.AnimatorUtil;
import com.hybrid.tripleldc.util.ui.ToastUtil;

/**
 * Author: Joy
 * Created Time: 2021/7/7-21:20
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/7 )
 * <p>
 * Describe:
 */

public class DCMainControlView extends LinearLayout {

    private static final String TAG = "DCMainControlView";

    ViewDataCollectControlBinding binding;

    private Animation mainControlTranslateAnimation;
    private Animation mainControlResetAnimation;
    private Animation laneChangedAppearAnimation;
    private Animation laneChangedResetAnimation;

    private CollectionStatus currStatus = CollectionStatus.Stop_Collect;

    private ControlCallback controlCallback;

    public interface ControlCallback {
        boolean onDCStart();

        boolean onDCStop();

        void onLaneChanged();
    }

    public enum CollectionStatus {
        Start_Collect,
        Stop_Collect
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.text_main_control:
                case R.id.img_main_control_background:
                    if (currStatus == CollectionStatus.Stop_Collect) {
                        // 避免多次点击
                        enableMainControl(false);
                        controlCallback.onDCStart();
                    } else if (currStatus == CollectionStatus.Start_Collect) {
                        // 避免多次点击
                        enableMainControl(false);
                        controlCallback.onDCStop();
                    }
                    break;
                case R.id.text_lane_change:
                case R.id.img_lane_change_background:
                    if (currStatus == CollectionStatus.Start_Collect) {
                        // 避免多次点击
                        enableLaneChanged(false);
                        controlCallback.onLaneChanged();
                    }
                    break;

            }
        }
    };

    public DCMainControlView(Context context) {
        super(context);
        initView(context);
    }

    public DCMainControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DCMainControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public DCMainControlView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        binding = ViewDataCollectControlBinding.inflate(LayoutInflater.from(context), DCMainControlView.this, true);

        // init animation
        mainControlTranslateAnimation = AnimatorUtil.generateTranslateAnimation(0.0f, -0.4f, 0.0f, 0.0f, 0, 1000);
        mainControlResetAnimation = AnimatorUtil.generateTranslateAnimation(-0.4f, 0.0f, 0.0f, 0.0f, 0, 1000);
        laneChangedAppearAnimation = AnimatorUtil.generateAlphaAnimation(0.0f, 1.0f, 1500);
        laneChangedResetAnimation = AnimatorUtil.generateAlphaAnimation(1.0f, 0.0f, 1500);

        binding.textMainControl.setOnClickListener(onClickListener);
        binding.imgMainControlBackground.setOnClickListener(onClickListener);
        binding.textLaneChange.setOnClickListener(onClickListener);
        binding.imgLaneChangeBackground.setOnClickListener(onClickListener);
    }

    public void enableMainControl(boolean enable) {
        binding.textMainControl.setEnabled(enable);
        binding.imgMainControlBackground.setEnabled(enable);
    }

    public void enableLaneChanged(boolean enable) {
        binding.textLaneChange.setEnabled(enable);
        binding.imgLaneChangeBackground.setEnabled(enable);
    }

    public void setControlCallback(ControlCallback controlCallback) {
        this.controlCallback = controlCallback;
    }

    public void setControlStatus(CollectionStatus status) {
        switch (status) {
            case Start_Collect:
                binding.textMainControl.setTextSize(30.0f);
                binding.textMainControl.setText(R.string.data_collecting);
                binding.textMainControl.startAnimation(mainControlTranslateAnimation);
                binding.imgMainControlBackground.startAnimation(mainControlTranslateAnimation);
                // lane changed btn show
                binding.imgLaneChangeBackground.startAnimation(laneChangedAppearAnimation);
                binding.textLaneChange.startAnimation(laneChangedAppearAnimation);
                binding.imgLaneChangeBackground.setVisibility(VISIBLE);
                binding.textLaneChange.setVisibility(VISIBLE);

                AnimatorUtil.colorTransit(binding.getRoot(), "backgroundColor", R.color.deepskyblue, R.color.peachpuff, 1500);

                ToastUtil.showNormalToast("Data Collect Start....");
                break;
            case Stop_Collect:
                binding.textMainControl.setTextSize(50.0f);
                binding.textMainControl.setText(R.string.start_collect);
                binding.textMainControl.startAnimation(mainControlResetAnimation);
                binding.imgMainControlBackground.startAnimation(mainControlResetAnimation);
                // lane changed btn hide
                binding.imgLaneChangeBackground.startAnimation(laneChangedResetAnimation);
                binding.textLaneChange.startAnimation(laneChangedResetAnimation);
                binding.imgLaneChangeBackground.setVisibility(GONE);
                binding.textLaneChange.setVisibility(GONE);

                AnimatorUtil.colorTransit(binding.getRoot(), "backgroundColor", R.color.peachpuff, R.color.deepskyblue, 1500);

                ToastUtil.showNormalToast("Data Collect End....");
                break;
            default:
                break;
        }

        currStatus = status;
    }

}
