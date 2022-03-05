package com.hybrid.tripleldc.view.widget;

import android.annotation.SuppressLint;
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

        void onDCStop();

        void onLaneChangeStart(boolean isLeftChange);

        void onLaneChangeFinish();

        void onConfigShow();
    }

    public enum CollectionStatus {
        Start_Collect,
        Stop_Collect
    }

    private final OnClickListener onClickListener = new OnClickListener() {
        @SuppressLint("NonConstantResourceId")
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
                case R.id.text_left_lane_change:
                case R.id.img_left_lane_change_background:
                    if (currStatus == CollectionStatus.Start_Collect) {
                        showLaneChange(false, true, false);
                        controlCallback.onLaneChangeStart(true);
                    }
                    break;
                case R.id.text_right_lane_change:
                case R.id.img_right_lane_change_background:
                    if (currStatus == CollectionStatus.Start_Collect) {
                        showLaneChange(false, true, false);
                        controlCallback.onLaneChangeStart(false);
                    }
                    break;
                case R.id.text_lane_change_finish:
                case R.id.img_lane_change_finish_background:
                    if (currStatus == CollectionStatus.Start_Collect) {
                        showLaneChange(true, false, false);
                        controlCallback.onLaneChangeFinish();
                    }
            }
        }
    };

    private final OnLongClickListener onLongClickListener = new OnLongClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.text_main_control:
                case R.id.img_main_control_background:
                    if (currStatus == CollectionStatus.Stop_Collect) {
                        controlCallback.onConfigShow();
                    }
                    return true;
            }
            return false;
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
        laneChangedAppearAnimation = AnimatorUtil.generateAlphaAnimation(0.0f, 1.0f, 1500, false);
        laneChangedResetAnimation = AnimatorUtil.generateAlphaAnimation(1.0f, 0.0f, 1500, false);

        binding.textMainControl.setOnClickListener(onClickListener);
        binding.imgMainControlBackground.setOnClickListener(onClickListener);
        binding.textLeftLaneChange.setOnClickListener(onClickListener);
        binding.imgLeftLaneChangeBackground.setOnClickListener(onClickListener);
        binding.textRightLaneChange.setOnClickListener(onClickListener);
        binding.imgRightLaneChangeBackground.setOnClickListener(onClickListener);
        binding.textLaneChangeFinish.setOnClickListener(onClickListener);
        binding.imgLaneChangeFinishBackground.setOnClickListener(onClickListener);

        binding.textMainControl.setOnLongClickListener(onLongClickListener);
        binding.imgMainControlBackground.setOnLongClickListener(onLongClickListener);
    }

    public void enableMainControl(boolean enable) {
        binding.textMainControl.setEnabled(enable);
        binding.imgMainControlBackground.setEnabled(enable);
    }

    public void enableLaneChangeOperationArea(boolean enable) {
        binding.textLeftLaneChange.setEnabled(enable);
        binding.imgLeftLaneChangeBackground.setEnabled(enable);
        binding.textRightLaneChange.setEnabled(enable);
        binding.imgRightLaneChangeBackground.setEnabled(enable);
        binding.textLaneChangeFinish.setEnabled(enable);
        binding.imgLaneChangeFinishBackground.setEnabled(enable);
    }

    public void setControlCallback(ControlCallback controlCallback) {
        this.controlCallback = controlCallback;
    }

    public void setControlStatus(CollectionStatus status) {
        switch (status) {
            case Start_Collect:
                binding.textMainControl.setTextSize(30.0f);
                binding.textMainControl.setText(R.string.data_collecting);
                // main control translate
                runMainControlAnimation(mainControlTranslateAnimation);
                // lane changed btn show
                showLaneChange(true, false, true);
                // change background color
                AnimatorUtil.colorTransit(binding.getRoot(), "backgroundColor", R.color.deepskyblue, R.color.peachpuff, 1500);

                ToastUtil.showNormalToast("Data Collect Start....");
                break;
            case Stop_Collect:
                binding.textMainControl.setTextSize(50.0f);
                binding.textMainControl.setText(R.string.start_collect);
                // main control reset
                runMainControlAnimation(mainControlResetAnimation);
                // lane changed btn hide
                showLaneChange(false, false, true);
                // change background color
                AnimatorUtil.colorTransit(binding.getRoot(), "backgroundColor", R.color.peachpuff, R.color.deepskyblue, 1500);

                ToastUtil.showNormalToast("Data Collect End....");
                break;
            default:
                break;
        }

        currStatus = status;
    }

    public void reset() {
        enableMainControl(true);
    }

    private void runMainControlAnimation(Animation animation) {
        binding.textMainControl.startAnimation(animation);
        binding.imgMainControlBackground.startAnimation(animation);
    }

    private void showLaneChange(boolean lrShow, boolean finishShow, boolean useAnimation) {
        showLaneChangLR(lrShow, useAnimation);
        showLaneChangeFinish(finishShow, useAnimation);
    }

    private void showLaneChangLR(boolean show, boolean useAnimation) {
        int status = show ? VISIBLE : GONE;
        Animation animation =  show ? laneChangedAppearAnimation : laneChangedResetAnimation;

        if (status == GONE && binding.textLeftLaneChange.getVisibility() == GONE) {
            return;
        }

        // run animation
        if (useAnimation) {
            binding.imgLeftLaneChangeBackground.startAnimation(animation);
            binding.textLeftLaneChange.startAnimation(animation);
            binding.imgRightLaneChangeBackground.startAnimation(animation);
            binding.textRightLaneChange.startAnimation(animation);
        }

        // set visibility
        binding.textLeftLaneChange.setVisibility(status);
        binding.imgLeftLaneChangeBackground.setVisibility(status);
        binding.textRightLaneChange.setVisibility(status);
        binding.imgRightLaneChangeBackground.setVisibility(status);
    }

    private void showLaneChangeFinish(boolean show, boolean useAnimation) {
        int status = show ? VISIBLE : GONE;
        Animation animation =  show ? laneChangedAppearAnimation : laneChangedResetAnimation;

        if (status == GONE && binding.textLaneChangeFinish.getVisibility() == GONE) {
            return;
        }

        // run animation
        if (useAnimation) {
            binding.textLaneChangeFinish.startAnimation(animation);
            binding.imgLaneChangeFinishBackground.startAnimation(animation);
        }

        // set visibility
        binding.textLaneChangeFinish.setVisibility(status);
        binding.imgLaneChangeFinishBackground.setVisibility(status);
    }
}