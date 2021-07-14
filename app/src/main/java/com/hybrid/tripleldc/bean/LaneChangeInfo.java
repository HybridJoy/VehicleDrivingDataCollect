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
    // 时间片ID (主键)
    private long timeSliceID;
    // 时间片大小
    private int timeSliceInterval;
    // 时间片内是否变道
    private boolean isLaneChanged = false;
    // 变道时间
    private long laneChangedTimestamp;
    // 时间片内的加速度读数
    private List<Acceleration> accelerationData;
    // 时间片内的角速度读数
    private List<GyroAngel> gyroAngelData;
    // 时间片内的GPS读数
    private List<GPSPosition> gpsPositionData;

    public LaneChangeInfo(long timeSliceID, int timeSliceInterval) {
        this.timeSliceID = timeSliceID;
        this.timeSliceInterval = timeSliceInterval;
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

    public boolean isLaneChanged() {
        return isLaneChanged;
    }

    public void setLaneChanged(boolean laneChanged) {
        isLaneChanged = laneChanged;
    }

    public long getLaneChangedTimestamp() {
        return laneChangedTimestamp;
    }

    public void setLaneChangedTimestamp(long laneChangedTimestamp) {
        this.laneChangedTimestamp = laneChangedTimestamp;
    }

    public List<Acceleration> getAccelerationData() {
        return accelerationData;
    }

    public void setAccelerationData(List<Acceleration> accelerationData) {
        this.accelerationData = accelerationData;
    }

    public List<GyroAngel> getGyroAngelData() {
        return gyroAngelData;
    }

    public void setGyroAngelData(List<GyroAngel> gyroAngelData) {
        this.gyroAngelData = gyroAngelData;
    }

    public List<GPSPosition> getGpsPositionData() {
        return gpsPositionData;
    }

    public void setGpsPositionData(List<GPSPosition> gpsPositionData) {
        this.gpsPositionData = gpsPositionData;
    }
}
