package com.hybrid.tripleldc.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.hybrid.tripleldc.bean.LaneChangeInfo;
import com.hybrid.tripleldc.config.DataConst;
import com.hybrid.tripleldc.util.io.LogUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Author: Joy
 * Created Time: 2021/7/13-14:40
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/13 )
 * <p>
 * Describe:
 */
public class DUService extends Service {
    private static final String TAG = "DataUploadService";

    private static final MediaType JSON =  MediaType.parse("application/json; charset=utf-8");
    private static final String SERVER_URL = DataConst.OkHttpConfig.SERVER_URL;

    private OkHttpClient mOkHttpClient;
    private Gson gson = new Gson();

    private DUBinder mBinder;
    public class DUBinder extends Binder {
        public DUService getService() {
            return DUService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mBinder = new DUBinder();
        return mBinder;
    }

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate executed");
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(200, TimeUnit.SECONDS)
                .readTimeout(200, TimeUnit.SECONDS)
                .writeTimeout(200, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy executed");
    }

    /**
     * 测试服务器连接
     * @param severUrl
     * @param callback
     */
    public void testServerConnect(String severUrl, Callback callback) {
        if (severUrl.equals("")) {
            severUrl = SERVER_URL;
        }

        Request request = new Request.Builder()
                .url(severUrl + DataConst.Request.REQUEST_TEST_SERVER_CONNECT)
                .tag(DataConst.RequestTag.REQUEST_TEST_SERVER_CONNECT_TAG)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(callback);
    }

    /**
     * 上传变道数据
     * @param laneChangeInfos
     * @param callback
     */
    public void uploadLaneChangeInfo(List<LaneChangeInfo> laneChangeInfos, Callback callback) {
        String json = gson.toJson(laneChangeInfos);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(SERVER_URL + DataConst.Request.REQUEST_UPLOAD_LANE_CHANGE_INFO)
                .post(body)
                .tag(DataConst.RequestTag.REQUEST_UPLOAD_LANE_CHANGE_INFO_TAG)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(callback);
    }
}
