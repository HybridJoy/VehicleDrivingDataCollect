package com.hybrid.tripleldc.util.io;

import com.hybrid.tripleldc.bean.InertialSequence;
import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.AngularRate;
import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.bean.GravityAcceleration;
import com.hybrid.tripleldc.bean.LinearAcceleration;
import com.hybrid.tripleldc.bean.Orientation;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Author: Joy
 * Created Time: 2022/3/5-21:53
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2022/3/5 )
 * <p>
 * Describe:
 */
public class RealmHelper {

    private Realm mRealm;

    private RealmHelper() {
        mRealm = Realm.getDefaultInstance();
    }

    private static class RealmHelperInstance {
        private static final RealmHelper instance = new RealmHelper();
    }

    public static RealmHelper getInstance() {
        return RealmHelperInstance.instance;
    }

    public void close() {
        if (mRealm != null) {
            mRealm.close();
        }
    }

    public void saveInertialSequence(final InertialSequence inertialSequence, boolean sync) {
        if (sync) {
            mRealm.beginTransaction();
            mRealm.copyToRealm(inertialSequence.getAccelerations());
            mRealm.copyToRealm(inertialSequence.getAngularRates());
            mRealm.copyToRealm(inertialSequence.getOrientations());
            mRealm.copyToRealm(inertialSequence.getGravityAccelerations());
            mRealm.copyToRealm(inertialSequence.getLinearAccelerations());
            mRealm.copyToRealm(inertialSequence.getGpsPositions());
            mRealm.commitTransaction();
        } else {
            mRealm.executeTransactionAsync(realm -> {
                realm.copyToRealm(inertialSequence.getAccelerations());
                realm.copyToRealm(inertialSequence.getAngularRates());
                realm.copyToRealm(inertialSequence.getOrientations());
                realm.copyToRealm(inertialSequence.getGravityAccelerations());
                realm.copyToRealm(inertialSequence.getLinearAccelerations());
                realm.copyToRealm(inertialSequence.getGpsPositions());
            });
        }
    }

    public InertialSequence loadInertialSequence() {
        final List<Acceleration> accelerations = mRealm.copyFromRealm(mRealm.where(Acceleration.class).findAll());
        final List<AngularRate> angularRates = mRealm.copyFromRealm(mRealm.where(AngularRate.class).findAll());
        final List<Orientation> orientations = mRealm.copyFromRealm(mRealm.where(Orientation.class).findAll());
        final List<GPSPosition> gpsPositions = mRealm.copyFromRealm(mRealm.where(GPSPosition.class).findAll());
        final List<GravityAcceleration> gravityAccelerations = mRealm.copyFromRealm(mRealm.where(GravityAcceleration.class).findAll());
        final List<LinearAcceleration> linearAccelerations = mRealm.copyFromRealm(mRealm.where(LinearAcceleration.class).findAll());

        return new InertialSequence(accelerations, gravityAccelerations, linearAccelerations, angularRates, orientations, gpsPositions);
    }

    public void cleanSensorDatabase(boolean sync) {
        // 从数据库中读取数据
        final RealmResults<Acceleration> accelerations = mRealm.where(Acceleration.class).findAll();
        final RealmResults<AngularRate> angularRates = mRealm.where(AngularRate.class).findAll();
        final RealmResults<Orientation> orientations = mRealm.where(Orientation.class).findAll();
        final RealmResults<GPSPosition> gpsPositions = mRealm.where(GPSPosition.class).findAll();
        final RealmResults<GravityAcceleration> gravityAccelerations = mRealm.where(GravityAcceleration.class).findAll();
        final RealmResults<LinearAcceleration> linearAccelerations = mRealm.where(LinearAcceleration.class).findAll();

        if (sync) {
            mRealm.beginTransaction();
            accelerations.deleteAllFromRealm();
            angularRates.deleteAllFromRealm();
            orientations.deleteAllFromRealm();
            gpsPositions.deleteAllFromRealm();
            gravityAccelerations.deleteAllFromRealm();
            linearAccelerations.deleteAllFromRealm();
            mRealm.commitTransaction();
        } else {
            mRealm.executeTransactionAsync(realm -> {
                accelerations.deleteAllFromRealm();
                angularRates.deleteAllFromRealm();
                orientations.deleteAllFromRealm();
                gpsPositions.deleteAllFromRealm();
                gravityAccelerations.deleteAllFromRealm();
                linearAccelerations.deleteAllFromRealm();
            });
        }
    }
}
