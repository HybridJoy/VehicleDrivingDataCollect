package com.hybrid.tripleldc.util.task;

import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.AngularRate;
import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.bean.GravityAcceleration;
import com.hybrid.tripleldc.bean.LinearAcceleration;
import com.hybrid.tripleldc.bean.Orientation;
import com.hybrid.tripleldc.global.App;
import com.hybrid.tripleldc.util.io.AsyncTaskRunner;
import com.hybrid.tripleldc.util.io.FileIOUtil;
import com.hybrid.tripleldc.util.io.LogUtil;
import com.hybrid.tripleldc.util.io.RealmHelper;
import com.hybrid.tripleldc.util.system.DateUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import io.realm.RealmModel;

/**
 * Author: Joy
 * Created Time: 2022/3/16-21:39
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2022/3/16 )
 * <p>
 * Describe:
 */
public class SensorDataExportTask {
    private static final String TAG = "SensorDataExportTask";
    private final Handler handler;
    private final int msg;
    private final boolean enableMultiThread;

    public SensorDataExportTask(Handler handler, int msg, boolean enableMultiThread) {
        this.handler = handler;
        this.msg = msg;
        this.enableMultiThread = enableMultiThread;
    }

    public void execute() {
        AsyncTaskRunner taskRunner;
        final long startTime = System.currentTimeMillis();

        if (enableMultiThread) {
            taskRunner = new AsyncTaskRunner(6);
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();

            Set<Callable<Boolean>> tasks = new HashSet<>();
            tasks.add(() -> exportSensorData(Acceleration.class, gson, true));
            tasks.add(() -> exportSensorData(AngularRate.class, gson, true));
            tasks.add(() -> exportSensorData(Orientation.class, gson, true));
            tasks.add(() -> exportSensorData(GPSPosition.class, gson, true));
            tasks.add(() -> exportSensorData(GravityAcceleration.class, gson, true));
            tasks.add(() -> exportSensorData(LinearAcceleration.class, gson, true));

            taskRunner.executeTasks(tasks, result -> {
                Message message = new Message();
                message.what = msg;
                message.obj = new AsyncTaskRunner.TaskResultInfo(result != null && !result.contains(false), startTime, System.currentTimeMillis());
                handler.sendMessage(message);
            });

        } else {
            taskRunner = new AsyncTaskRunner();
            taskRunner.executeCallable(this::work, result -> {
                Message message = new Message();
                message.what = msg;
                message.obj = new AsyncTaskRunner.TaskResultInfo(result, startTime, System.currentTimeMillis());
                handler.sendMessage(message);
            });
        }
        taskRunner.cancel();
    }

    private boolean work() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        boolean result;

        // save acceleration to json
        result = exportSensorData(Acceleration.class, gson, false);
        // save angular rate to json
        result = result && exportSensorData(AngularRate.class, gson, false);
        // save orientation to json
        result = result && exportSensorData(Orientation.class, gson, false);
        // save gps position to json
        result = result && exportSensorData(GPSPosition.class, gson, false);
        // save gravity acceleration to json
        result = result && exportSensorData(GravityAcceleration.class, gson, false);
        // save linear acceleration to json
        result = result && exportSensorData(LinearAcceleration.class, gson, true);

        return result;
    }

    // 单次写入的item最大数量
    private static final int MaxFlushItemSize = 30000;

    private <T extends RealmModel> boolean exportSensorData(Class<T> clazz, Gson gson, boolean closeDB) {
        LogUtil.d(TAG, String.format("exporting %s", clazz.getSimpleName()));

        int dataSize = RealmHelper.getInstance().getInertialSensorDataLatestID(clazz);
        final int finalDataIndex = dataSize;

        boolean result = true;

        List<T> sensorData;
        int startIndex = 0, endIndex;
        do {
            endIndex = dataSize <= MaxFlushItemSize ? finalDataIndex : startIndex + MaxFlushItemSize;
            // 从数据库读取数据
            sensorData = RealmHelper.getInstance().loadInertialSensorData(clazz, startIndex, endIndex);
            // 写入文件
            result = result && writeSensorData(gson.toJson(sensorData), clazz.getSimpleName(), startIndex != 0, endIndex != finalDataIndex);
            // 更新
            startIndex += MaxFlushItemSize;
            dataSize -= MaxFlushItemSize;
        } while (endIndex != finalDataIndex);

        if (closeDB) {
            RealmHelper.getInstance().close();
        }

        return result;
    }

    private boolean writeSensorData(String dataJson, String sensorType, boolean prefixProcess, boolean postfixProcess) {
        if (prefixProcess) {
            // 处理开头
            dataJson = dataJson.replace("[\n  {", "\n  {");
        }

        if (postfixProcess) {
            // 处理结尾
            dataJson = dataJson.replace("}\n]", "},");
        }

        return writeSensorDataToFile(dataJson, sensorType, prefixProcess);
    }

    private static final String deviceName = RealmHelper.getInstance().getDeviceName();
    private static final String fileSuffix = ".json";
    private static final String storageFolder = String.format("%s/InertialSequence", App.getInstance().getExternalFilesDir(null));

    private static boolean writeSensorDataToFile(String dataJson, String sensorType, boolean append) {
        String fileName = String.format("%s_%s_%s%s", DateUtil.getCurrDateString(false), deviceName, sensorType, fileSuffix);
        String filePath = String.format("%s/%s", storageFolder, fileName);

        return FileIOUtil.writeFileFromString(filePath, dataJson, append);
    }
}
