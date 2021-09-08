package com.hybrid.tripleldc.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.hybrid.tripleldc.R;
import com.hybrid.tripleldc.databinding.ViewMainDisplayBinding;

/**
 * Author: Joy
 * Created Time: 2021/7/22-18:16
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/22 )
 * <p>
 * Describe:
 */
public class MainDisplayView extends LinearLayout {

    private ViewMainDisplayBinding binding;

    public MainDisplayView(Context context) {
        super(context);
        initView(context);
    }

    public MainDisplayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MainDisplayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public MainDisplayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        binding = ViewMainDisplayBinding.inflate(LayoutInflater.from(context), MainDisplayView.this, true);

        // loading image show by random
        int index = (int)(Math.random() * 99 + 1);
        Glide.with(MainDisplayView.this)
                .load(index < 50 ? R.drawable.vehicle_move_1 : R.drawable.vehicle_move_2)
                .into(binding.imgShow);
    }
}