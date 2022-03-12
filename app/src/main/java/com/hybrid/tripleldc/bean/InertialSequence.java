package com.hybrid.tripleldc.bean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hybrid.tripleldc.global.App;
import com.hybrid.tripleldc.util.io.FileIOUtil;
import com.hybrid.tripleldc.util.io.RealmHelper;
import com.hybrid.tripleldc.util.system.DateUtil;

import java.util.List;

import io.realm.RealmModel;

/**
 * Author: Joy
 * Created Time: 2022/3/5-22:03
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2022/3/5 )
 * <p>
 * Describe:
 */
public class InertialSequence {
    private static final String TAG = "InertialSequence";

    private List<Acceleration> accelerations;
    private List<GravityAcceleration> gravityAccelerations;
    private List<LinearAcceleration> linearAccelerations;
    private List<AngularRate> angularRates;
    private List<Orientation> orientations;
    private List<GPSPosition> gpsPositions;

    public InertialSequence() {

    }

    public InertialSequence(List<Acceleration> accelerations, List<GravityAcceleration> gravityAccelerations,
                            List<LinearAcceleration> linearAccelerations, List<AngularRate> angularRates,
                            List<Orientation> orientations, List<GPSPosition> gpsPositions) {
        this.accelerations = accelerations;
        this.gravityAccelerations = gravityAccelerations;
        this.linearAccelerations = linearAccelerations;
        this.angularRates = angularRates;
        this.orientations = orientations;
        this.gpsPositions = gpsPositions;
    }

    public List<Acceleration> getAccelerations() {
        return accelerations;
    }

    public void setAccelerations(List<Acceleration> accelerations) {
        this.accelerations = accelerations;
    }

    public List<GravityAcceleration> getGravityAccelerations() {
        return gravityAccelerations;
    }

    public void setGravityAccelerations(List<GravityAcceleration> gravityAccelerations) {
        this.gravityAccelerations = gravityAccelerations;
    }

    public List<LinearAcceleration> getLinearAccelerations() {
        return linearAccelerations;
    }

    public void setLinearAccelerations(List<LinearAcceleration> linearAccelerations) {
        this.linearAccelerations = linearAccelerations;
    }

    public List<AngularRate> getAngularRates() {
        return angularRates;
    }

    public void setAngularRates(List<AngularRate> angularRates) {
        this.angularRates = angularRates;
    }

    public List<Orientation> getOrientations() {
        return orientations;
    }

    public void setOrientations(List<Orientation> orientations) {
        this.orientations = orientations;
    }

    public List<GPSPosition> getGpsPositions() {
        return gpsPositions;
    }

    public void setGpsPositions(List<GPSPosition> gpsPositions) {
        this.gpsPositions = gpsPositions;
    }

    public boolean writeToFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        boolean success = splitAndWriteData(gson, getAccelerations(), "Acceleration");
        this.accelerations.clear();
        success = success && splitAndWriteData(gson, getAngularRates(), "AngularRate");
        this.angularRates.clear();
//        success = success && splitAndWriteData(gson, getOrientations(), "Orientation");
//        this.orientations.clear();
        success = success && splitAndWriteData(gson, getGpsPositions(), "GPSPosition");
        this.gpsPositions.clear();
        success = success && splitAndWriteData(gson, getGravityAccelerations(), "Gravity");
        this.gravityAccelerations.clear();
        success = success && splitAndWriteData(gson, getLinearAccelerations(), "LinearAcceleration");
        this.linearAccelerations.clear();

        return success;
    }

    // 单次写入的item最大数量
    private static final int MaxFlushItemSize = 200000;

    private <T extends RealmModel> boolean splitAndWriteData(Gson gson, List<T> sensorData, String sensorType) {
        int dataSize = sensorData.size();
        boolean firstSeg = true;
        boolean success;

        while (true) {
            if (dataSize <= MaxFlushItemSize) {
                String sensorDataJson = gson.toJson(sensorData);
                // 处理开头
                if (!firstSeg) sensorDataJson = sensorDataJson.replace("[\n  {", "\n  {");
                // 写入文件
                success = writeSensorDataToFile(sensorDataJson, sensorType, !firstSeg);
                break;
            } else {
                // 获取此次写入的数据
                List<T> subData = sensorData.subList(0, MaxFlushItemSize);
                String subDataJson = gson.toJson(subData);
                // 处理开头
                if (!firstSeg) subDataJson = subDataJson.replace("[\n  {", "\n  {");
                // 处理结尾
                subDataJson = subDataJson.replace("}\n]", "},");
                // 写入文件
                writeSensorDataToFile(subDataJson, sensorType, !firstSeg);
                // 移除已经写入的
                dataSize -= MaxFlushItemSize;
                sensorData.removeAll(subData);
                // 控制变量更新
                firstSeg = false;
            }
        }

        return success;
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
