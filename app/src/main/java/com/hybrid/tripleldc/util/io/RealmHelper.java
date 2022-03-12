package com.hybrid.tripleldc.util.io;

import android.os.Looper;

import com.hybrid.tripleldc.bean.Device;
import com.hybrid.tripleldc.bean.InertialSequence;
import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.AngularRate;
import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.bean.GravityAcceleration;
import com.hybrid.tripleldc.bean.LinearAcceleration;
import com.hybrid.tripleldc.bean.Orientation;
import com.hybrid.tripleldc.config.DataConst;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Handler;
import android.os.Message;

import io.realm.Realm;
import io.realm.RealmModel;
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

    private static final String TAG = "RealmHelper";

    private Realm mRealm;
    private long currThreadID;

    private List<RealmHolder> realmHolders = new ArrayList<>();

    private static final int MsgCheck = 1;
    private static final int IntervalMsgCheck = 20000;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MsgCheck:
                    // TODO 检测realm实例对应的线程是否存活
                    handler.sendEmptyMessageDelayed(MsgCheck, IntervalMsgCheck);
                default:
                    break;
            }
        }
    };

    private static class RealmHolder {
        public Realm realm;
        public long threadID;

        public RealmHolder(Realm realm, long threadID) {
            this.realm = realm;
            this.threadID = threadID;
        }
    }

    private RealmHelper() {
        // handler.sendEmptyMessageDelayed(MsgCheck, IntervalMsgCheck);
    }

    public RealmHelper reset() {
        // 解决多线程访问问题
        createOrFindRealm();
        return this;
    }

    public void close() {
        releaseRealmInstance();
    }

    private static class RealmHelperInstance {
        private static final RealmHelper instance = new RealmHelper();
    }

    public static RealmHelper getInstance() {
        return RealmHelperInstance.instance.reset();
    }

    private void createOrFindRealm() {
        currThreadID = Thread.currentThread().getId();
        LogUtil.d(TAG, String.format(Locale.ENGLISH, "curr thread id: %d", currThreadID));

        // check exist
        for (int i = 0; i < realmHolders.size(); i++) {
            if (realmHolders.get(i).threadID == currThreadID) {
                mRealm = realmHolders.get(i).realm;
                LogUtil.d(TAG, String.format(Locale.ENGLISH, "found exist instance %s", mRealm));
                return;
            }
        }

        // create
        mRealm = Realm.getDefaultInstance();
        realmHolders.add(new RealmHolder(mRealm, currThreadID));
        LogUtil.d(TAG, String.format(Locale.ENGLISH, "create new instance %s", mRealm));
    }

    private void releaseRealmInstance() {
        long threadID = Thread.currentThread().getId();
        for (int i = 0; i < realmHolders.size(); i++) {
            RealmHolder realmHolder = realmHolders.get(i);
            if(realmHolder.threadID == threadID) {
                LogUtil.d(TAG, String.format(Locale.ENGLISH, "release instance %s", realmHolder.realm));

                realmHolder.realm.close();
                realmHolders.remove(realmHolder);
                return;
            }
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

    public String getDeviceName() {
        Device device = mRealm.where(Device.class).findFirst();

        return device == null ? DataConst.System.DEFAULT_DEVICE_NAME : device.getName();
    }

    public void updateDeviceName(String name) {
        Device device = mRealm.where(Device.class).findFirst();
        mRealm.beginTransaction();
        if (device == null) {
            device = new Device();
            device.setId(1);
            device.setName(name);
            mRealm.copyToRealm(device);
        } else {
            device.setName(name);
        }
        mRealm.commitTransaction();
    }

    public <T extends RealmModel> int getInertialSensorDataLatestID(Class<T> clazz) {
        Number latestID = mRealm.where(clazz).max("id");
        return latestID == null ? -1 : latestID.intValue();
    }
}
