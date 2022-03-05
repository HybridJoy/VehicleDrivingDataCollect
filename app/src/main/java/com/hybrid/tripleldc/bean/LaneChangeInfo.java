package com.hybrid.tripleldc.bean;

import java.util.List;

/**
 * Author: Joy
 * Created Time: 2021/7/13-17:02
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/13 )
 * <p>
 * Describe:
 */
public class LaneChangeInfo {
    // id (主键)
    private int id;
    // 设备名称
    private String deviceName;
    // 时间片ID
    private long timeSliceID;
    // 时间片大小
    private int timeSliceInterval;
    // 开始时间
    private String startTime;
    // 结束时间
    private String endTime;
    // 时间片内是否变道
    private boolean isLaneChanged = false;
    // 变道开始时间
    private String laneChangeStartTime;
    // 变道结束时间
    private String laneChangeEndTime;
    // 变道类型 1 左变道 2 右变道 0 无变道类型数据
    private int laneChangedType = LaneChangeType.NO_TYPE;
    // 时间片内的加速度读数
    private List<Acceleration> accelerationData;
    // 时间片内的角速度读数
    private List<AngularRate> angularRateData;
    // 时间片内的方向读数
    private List<Orientation> orientationData;
    // 时间片内的GPS读数
    private List<GPSPosition> gpsPositionData;

    // 数据库字段
    private int accelerationStartID;
    private int accelerationEndID;
    private int gyroangelStartID;
    private int gyroangelEndID;
    private int orientationStartID;
    private int orientationEndID;
    private int gpsPositionStartID;
    private int gpsPositionEndID;

    public static class LaneChangeType {
        public static final int NO_TYPE = 0;
        public static final int LEFT = 1;
        public static final int RIGHT = 2;
    }

    public LaneChangeInfo(String deviceName, long timeSliceID, int timeSliceInterval, String startTime) {
        this.deviceName = deviceName;
        this.timeSliceID = timeSliceID;
        this.timeSliceInterval = timeSliceInterval;
        this.startTime = startTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public long getTimeSliceID() {
        return timeSliceID;
    }

    public void setTimeSliceID(long timeSliceID) {
        this.timeSliceID = timeSliceID;
    }

    public int getTimeSliceInterval() {
        return timeSliceInterval;
    }

    public void setTimeSliceInterval(int timeSliceInterval) {
        this.timeSliceInterval = timeSliceInterval;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isLaneChanged() {
        return isLaneChanged;
    }

    public void setLaneChanged(boolean laneChanged) {
        isLaneChanged = laneChanged;
    }

    public String getLaneChangeStartTime() {
        return laneChangeStartTime;
    }

    public void setLaneChangeStartTime(String laneChangeStartTime) {
        this.laneChangeStartTime = laneChangeStartTime;
    }

    public String getLaneChangeEndTime() {
        return laneChangeEndTime;
    }

    public void setLaneChangeEndTime(String laneChangeEndTime) {
        this.laneChangeEndTime = laneChangeEndTime;
    }

    public int getLaneChangedType() {
        return laneChangedType;
    }

    public void setLaneChangedType(int laneChangedType) {
        this.laneChangedType = laneChangedType;
    }

    public List<Acceleration> getAccelerationData() {
        return accelerationData;
    }

    public void setAccelerationData(List<Acceleration> accelerationData) {
        this.accelerationData = accelerationData;
    }

    public List<AngularRate> getGyroAngelData() {
        return angularRateData;
    }

    public void setGyroAngelData(List<AngularRate> angularRateData) {
        this.angularRateData = angularRateData;
    }

    public List<Orientation> getOrientationData() {
        return orientationData;
    }

    public void setOrientationData(List<Orientation> orientationData) {
        this.orientationData = orientationData;
    }

    public List<GPSPosition> getGpsPositionData() {
        return gpsPositionData;
    }

    public void setGpsPositionData(List<GPSPosition> gpsPositionData) {
        this.gpsPositionData = gpsPositionData;
    }

    public int getAccelerationStartID() {
        return accelerationStartID;
    }

    public void setAccelerationStartID(int accelerationStartID) {
        this.accelerationStartID = accelerationStartID;
    }

    public int getAccelerationEndID() {
        return accelerationEndID;
    }

    public void setAccelerationEndID(int accelerationEndID) {
        this.accelerationEndID = accelerationEndID;
    }

    public int getGyroangelStartID() {
        return gyroangelStartID;
    }

    public void setGyroangelStartID(int gyroangelStartID) {
        this.gyroangelStartID = gyroangelStartID;
    }

    public int getGyroangelEndID() {
        return gyroangelEndID;
    }

    public void setGyroangelEndID(int gyroangelEndID) {
        this.gyroangelEndID = gyroangelEndID;
    }

    public int getOrientationStartID() {
        return orientationStartID;
    }

    public void setOrientationStartID(int orientationStartID) {
        this.orientationStartID = orientationStartID;
    }

    public int getOrientationEndID() {
        return orientationEndID;
    }

    public void setOrientationEndID(int orientationEndID) {
        this.orientationEndID = orientationEndID;
    }

    public int getGpsPositionStartID() {
        return gpsPositionStartID;
    }

    public void setGpsPositionStartID(int gpsPositionStartID) {
        this.gpsPositionStartID = gpsPositionStartID;
    }

    public int getGpsPositionEndID() {
        return gpsPositionEndID;
    }

    public void setGpsPositionEndID(int gpsPositionEndID) {
        this.gpsPositionEndID = gpsPositionEndID;
    }
}
