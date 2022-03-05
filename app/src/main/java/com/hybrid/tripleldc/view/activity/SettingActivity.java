package com.hybrid.tripleldc.view.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;

import com.hybrid.tripleldc.R;
import com.hybrid.tripleldc.bean.InertialSequence;
import com.hybrid.tripleldc.databinding.ActivitySettingBinding;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.io.RealmHelper;
import com.hybrid.tripleldc.util.ui.DialogUtil;
import com.hybrid.tripleldc.view.activity.base.BaseActivity;

/**
 * Author: Joy
 * Created Time: 2022/3/4-22:53
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2022/3/4 )
 * <p>
 * Describe:
 */
public class SettingActivity extends BaseActivity {
    private static final String TAG = "SettingActivity";

    private ActivitySettingBinding binding;

    private static final int MsgCleanSensorDatabase = 1;

    private final Handler mainHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MsgCleanSensorDatabase:
                    cleanSensorDatabase();
                    showTipsDialog("清除成功", false);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private final View.OnClickListener clickListener = v -> {
        switch (v.getId()) {
            case R.id.btn_export_sensor_data:
                boolean success = exportData();
                showTipsDialog(success ? "导出成功" : "导出失败，请检查数据后重试", !success);
                break;
            case R.id.btn_clean_sensor_database:
                DialogInterface.OnClickListener acceptClickListener = (dialog, which) -> {
                    mainHandler.sendEmptyMessage(MsgCleanSensorDatabase);
                    dialog.cancel();
                };
                DialogInterface.OnClickListener cancelClickListener = (dialog, which) -> {
                    dialog.cancel();
                };
                DialogUtil.createDialog(this, R.string.normal_dialog_title, "确定清空传感器数据库吗？",
                        R.string.dialog_accept, R.string.dialog_cancel, acceptClickListener, cancelClickListener).show();
            default:
                break;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        binding = ActivitySettingBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        initButtonClickListener();
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy");

        // 移除所有消息
        mainHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    private void initButtonClickListener() {
        binding.btnExportSensorData.setOnClickListener(clickListener);
        binding.btnCleanSensorDatabase.setOnClickListener(clickListener);
    }

    private boolean exportData() {
        // 从数据库中读取数据
        InertialSequence inertialSequence = RealmHelper.getInstance().loadInertialSequence();

        // 写文件
        return inertialSequence.writeToFile();
    }

    private void cleanSensorDatabase() {
       RealmHelper.getInstance().cleanSensorDatabase(true);
    }

    /**
     * 以对话框的形式展示提示信息
     *
     * @param msg        提示信息
     * @param isErrorMsg 是否是错误信息
     */
    private void showTipsDialog(String msg, boolean isErrorMsg) {
        DialogUtil.createDialog(this, isErrorMsg ? R.string.error_dialog_title : R.string.normal_dialog_title, msg, R.string.dialog_accept, -1).show();
    }
}