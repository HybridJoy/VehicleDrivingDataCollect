package com.hybrid.tripleldc;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.hybrid.tripleldc.databinding.ActivityMainBinding;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.system.AppUtil;
import com.hybrid.tripleldc.util.ui.ToastUtil;
import com.hybrid.tripleldc.view.activity.DataCollectActivity;
import com.hybrid.tripleldc.view.activity.base.BaseActivity;
import com.hybrid.tripleldc.view.widget.MainControlView;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private static final int PERMISSION_READ_EXTERNAL_STORAGE_CODE = 1001;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE_CODE = 1001;

    private final MainControlView.OperationCallback operationCallback = new MainControlView.OperationCallback() {
        @Override
        public void onDataCollection() {
            LogUtil.d(TAG, "enter DataCollectActivity");
            startActivity(new Intent(MainActivity.this, DataCollectActivity.class));
        }

        @Override
        public void onLocalization() {
            ToastUtil.showNormalToast("努力开发中，敬请期待！");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // init main control callback
        binding.mainControlArea.setOperationCallback(operationCallback);
        
        requestPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            ToastUtil.showNormalToast("暂无配置项！");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 请求需要的权限
     */
    private void requestPermissions() {
        AppUtil.requestPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE,
                PERMISSION_READ_EXTERNAL_STORAGE_CODE, "应用运行时需要进行本地存储，请授予该权限");
        AppUtil.requestPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                PERMISSION_WRITE_EXTERNAL_STORAGE_CODE, "应用运行时需要进行本地存储，请授予该权限");
    }
}