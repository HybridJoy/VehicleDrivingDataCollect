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
import com.hybrid.tripleldc.databinding.ViewMainControlBinding;
import com.hybrid.tripleldc.util.ui.AnimatorUtil;

/**
 * Author: Joy
 * Created Time: 2021/7/18-16:44
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/18 )
 * <p>
 * Describe:
 */
public class MainControlView extends LinearLayout {

    private static final String TAG = "MainControlView";

    private ViewMainControlBinding binding;

    private Animation operationAppearAnimation;
    private Animation operationResetAnimation;

    private boolean isOperationShow = false;

    private OperationCallback operationCallback;
    public interface OperationCallback {
        void onDataCollection();
        void onLocalization();
    }

    private final OnClickListener onClickListener = new OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.img_main_menu:
                    showOrHideOperation(!isOperationShow);
                    if (isOperationShow) {
                        binding.imgMainMenu.setImageResource(R.drawable.btn_clicked_double_circle_red);
                    } else {
                        binding.imgMainMenu.setImageResource(R.drawable.btn_double_circle_red);
                    }
                    break;
                case R.id.text_data_collection:
                case R.id.img_data_collection_background:
                    operationCallback.onDataCollection();
                    break;
                case R.id.text_localization:
                case R.id.img_localization_background:
                    operationCallback.onLocalization();
                    break;
                default:
                    break;
            }
        }
    };

    public MainControlView(Context context) {
        super(context);
        initView(context);
    }

    public MainControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MainControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public MainControlView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        binding = ViewMainControlBinding.inflate(LayoutInflater.from(context), MainControlView.this, true);

        // init Animation
        operationAppearAnimation = AnimatorUtil.generateAlphaAnimation(0.0f, 1.0f, 1000, false);
        operationResetAnimation = AnimatorUtil.generateAlphaAnimation(1.0f, 0.0f, 1000, false);

        // init click listener
        binding.imgMainMenu.setOnClickListener(onClickListener);
        binding.imgDataCollectionBackground.setOnClickListener(onClickListener);
        binding.imgLocalizationBackground.setOnClickListener(onClickListener);
        binding.textDataCollection.setOnClickListener(onClickListener);
        binding.textLocalization.setOnClickListener(onClickListener);

        // init long click listener

    }

    private void showOrHideOperation(boolean show) {
        int state = show ? VISIBLE : GONE;
        binding.imgDataCollectionBackground.setVisibility(state);
        binding.imgLocalizationBackground.setVisibility(state);
        binding.textDataCollection.setVisibility(state);
        binding.textLocalization.setVisibility(state);

        Animation animation = show ? operationAppearAnimation : operationResetAnimation;
        binding.imgDataCollectionBackground.startAnimation(animation);
        binding.imgLocalizationBackground.startAnimation(animation);
        binding.textDataCollection.startAnimation(animation);
        binding.textLocalization.startAnimation(animation);

        isOperationShow = show;
    }

    public void setOperationCallback(OperationCallback callback) {
        this.operationCallback = callback;
    }
}