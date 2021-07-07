package com.hybrid.tripleldc.util.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.ColorRes;

import com.hybrid.tripleldc.R;
import com.hybrid.tripleldc.databinding.ColorToastBinding;
import com.hybrid.tripleldc.global.App;
import com.hybrid.tripleldc.util.system.AppUtil;


public class ToastUtil {
    private static final String TAG = "ToastUtil";

    private static Context context = App.getInstance().getApplicationContext();
    private static String appName = AppUtil.getAppName(context);

    private static Toast toast;
    private static ToastView toastView;

    private static class ToastView extends FrameLayout {

        private ColorToastBinding binding;

        public ToastView(Context context) {
            super(context);
            initView(context);
        }

        private void initView(Context context) {
            binding = ColorToastBinding.inflate(LayoutInflater.from(context), ToastView.this, true);
        }
    }

    static {
        toast = new Toast(context);
        toastView = new ToastView(context);
        toastView.binding.toastPrefix.setText(appName + ":");
        toast.setView(toastView);
    }

    public static void showNormalToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showColorToast(String msg, Boolean isErrorMsg) {
        showCustomToast(msg, isErrorMsg ? R.color.red : R.color.cyan, null);
    }

    public static void showCustomToast(String msg, @ColorRes Integer textColor, @ColorRes Integer backgroundColor) {
        if (textColor == null) {
            textColor = R.color.black;
        }
        if (backgroundColor == null) {
            backgroundColor = R.color.transparent;
        }
        toastView.binding.toastMessage.setText(msg);
        toastView.binding.toastMessage.setTextColor(context.getColor(textColor));
        toastView.binding.toastMessage.setBackgroundColor(context.getColor(backgroundColor));

        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
