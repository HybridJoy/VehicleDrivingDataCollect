package com.hybrid.tripleldc.bean;

import com.google.gson.Gson;
import com.hybrid.tripleldc.global.App;
import com.hybrid.tripleldc.util.io.FileIOUtil;
import com.hybrid.tripleldc.util.system.DateUtil;

import java.util.List;

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
        Gson gson = new Gson();
        // 转为json
        String accelerationJson = gson.toJson(getAccelerations());
        String angularRateJson = gson.toJson(getAngularRates());
        String orientationJson = gson.toJson(getOrientations());
        String gpsPositionJson = gson.toJson(getGpsPositions());
        String gravityAccelerationJson = gson.toJson(getGravityAccelerations());
        String linearAccelerationJson = gson.toJson(getLinearAccelerations());

        // 写文件
        boolean success = writeSensorDataToFile(accelerationJson, "Acceleration");
        success = success && writeSensorDataToFile(angularRateJson, "AngularRate");
        success = success && writeSensorDataToFile(orientationJson, "Orientation");
        success = success && writeSensorDataToFile(gpsPositionJson, "GPSPosition");
        success = success && writeSensorDataToFile(gravityAccelerationJson, "Gravity");
        success = success && writeSensorDataToFile(linearAccelerationJson, "LinearAcceleration");

        return success;
    }

    private static final String fileSuffix = ".json";
    private static final String storageFolder = String.format("%s/InertialSequence", App.getInstance().getExternalFilesDir(null));

    private static boolean writeSensorDataToFile(String dataJson, String sensorType) {
        String fileName = String.format("%s_%s%s", DateUtil.getCurrDateString(false), sensorType, fileSuffix);
        String filePath = String.format("%s/%s", storageFolder, fileName);

        return FileIOUtil.writeFileFromString(filePath, dataJson, false);
    }
}
