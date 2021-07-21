package com.hybrid.tripleldc.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.hybrid.tripleldc.bean.GyroAngel;
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

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String SERVER_URL = DataConst.OkHttpConfig.SERVER_URL;

    private boolean enableService = false;
    private OkHttpClient mOkHttpClient;
    private final Gson gson = new Gson();

    public interface UploadCallback extends Callback {

    }

    public class DUBinder extends Binder {
        public DUService getService() {
            return DUService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DUBinder();
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
     * 激活服务
     *
     * @param enable 是否激活
     */
    public void enableService(boolean enable) {
        enableService = enable;
    }

    /**
     * 测试服务器连接
     *
     * @param severUrl 服务器地址
     * @param callback http回调
     */
    public void testServerConnect(String severUrl, Callback callback) {
        if (!enableService) {
            LogUtil.i(TAG, "enable service first");
            return;
        }

        LogUtil.i(TAG, "testServerConnect");
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
     * 获取服务器最新的时间片ID
     *
     * @param callback http回调
     */
    public void getLatestTimeSliceID(Callback callback) {
        if (!enableService) {
            LogUtil.i(TAG, "enable service first");
            return;
        }

        LogUtil.i(TAG, "getLatestTimeSliceID");
        Request request = new Request.Builder()
                .url(SERVER_URL + DataConst.Request.REQUEST_GET_LATEST_TIME_SLICE_ID)
                .tag(DataConst.RequestTag.REQUEST_GET_LATEST_TIME_SLICE_ID_TAG)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(callback);
    }

    /**
     * 上传变道数据
     *
     * @param laneChangeInfos 变道数据
     * @param callback        http回调
     */
    public void uploadLaneChangeInfo(List<LaneChangeInfo> laneChangeInfos, Callback callback) {
        if (!enableService) {
            LogUtil.i(TAG, "enable service first");
            return;
        }

        LogUtil.i(TAG, "uploadLaneChangeInfo");
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

    /**
     * 测试用接口
     *
     * @param gyroAngels 角速度数据
     * @param callback   http回调
     */
    public void uploadGyroTest(List<GyroAngel> gyroAngels, Callback callback) {
        if (!enableService) {
            LogUtil.i(TAG, "enable service first");
            return;
        }

        LogUtil.i(TAG, "uploadGyroTest");
        String json = gson.toJson(gyroAngels);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(SERVER_URL + DataConst.Request.REQUEST_UPLOAD_GYRO_TEST)
                .post(body)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(callback);
    }
}
