package com.hybrid.tripleldc.bean;

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
    private static final String TAG = "InertialSequence";

    private List<Acceleration> accelerations;
    private List<GravityAcceleration> gravityAccelerations;
    private List<LinearAcceleration> linearAccelerations;
    private List<AngularRate> angularRates;
    private List<Orientation> orientations;
    private List<GPSPosition> gpsPositions;

    public InertialSequence() {

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
}
