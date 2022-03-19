package com.hybrid.tripleldc.util.io;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.AngularRate;
import com.hybrid.tripleldc.bean.Device;
import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.bean.GravityAcceleration;
import com.hybrid.tripleldc.bean.InertialSequence;
import com.hybrid.tripleldc.bean.LinearAcceleration;
import com.hybrid.tripleldc.bean.Orientation;
import com.hybrid.tripleldc.config.DataConst;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.realm.Realm;
import io.realm.RealmModel;

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

    private List<RealmHolder> realmHolders = new ArrayList<>();

    // lock for multi threads
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

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

    public void close() {
        releaseRealmInstance();
    }

    private static class RealmHelperInstance {
        private static final RealmHelper instance = new RealmHelper();
    }

    public static RealmHelper getInstance() {
        return RealmHelperInstance.instance;
    }

    private Realm createOrFindRealm() {
        Realm realm = null;

        long threadID = Thread.currentThread().getId();
        LogUtil.d(TAG, String.format(Locale.ENGLISH, "curr thread id: %d", threadID));

        // check exist
        lock.readLock().lock();
        boolean exist = false;
        for (int i = 0; i < realmHolders.size(); i++) {
            if (realmHolders.get(i).threadID == threadID) {
                exist = true;
                realm = realmHolders.get(i).realm;
                LogUtil.d(TAG, String.format(Locale.ENGLISH, "found exist instance %s", realm));
                break;
            }
        }
        lock.readLock().unlock();

        // create
        if (!exist) {
            lock.writeLock().lock();
            realm = Realm.getDefaultInstance();
            realmHolders.add(new RealmHolder(realm, threadID));
            lock.writeLock().unlock();

            LogUtil.d(TAG, String.format(Locale.ENGLISH, "create new instance %s", realm));
        }

        return realm;
    }

    private void releaseRealmInstance() {
        long threadID = Thread.currentThread().getId();

        lock.writeLock().lock();
        for (int i = 0; i < realmHolders.size(); i++) {
            RealmHolder realmHolder = realmHolders.get(i);
            if (realmHolder.threadID == threadID) {
                LogUtil.d(TAG, String.format(Locale.ENGLISH, "release instance %s", realmHolder.realm));
                realmHolder.realm.close();
                realmHolders.remove(realmHolder);
                break;
            }
        }
        lock.writeLock().unlock();
    }

    public String getDeviceName() {
        LogUtil.d(TAG, "get device name");

        Realm realm = createOrFindRealm();

        Device device = realm.where(Device.class).findFirst();
        return device == null ? DataConst.System.DEFAULT_DEVICE_NAME : device.getName();
    }

    public void updateDeviceName(String name) {
        LogUtil.d(TAG, "update device name");

        Realm realm = createOrFindRealm();

        Device device = realm.where(Device.class).findFirst();
        realm.beginTransaction();
        if (device == null) {
            device = new Device();
            device.setId(1);
            device.setName(name);
            realm.copyToRealm(device);
        } else {
            device.setName(name);
        }
        realm.commitTransaction();
    }

    public <T extends RealmModel> int getInertialSensorDataLatestID(Class<T> clazz) {
        LogUtil.d(TAG, String.format(Locale.ENGLISH, "get %s latest id", clazz.getSimpleName()));

        Realm realm = createOrFindRealm();

        Number latestID = realm.where(clazz).max("id");
        return latestID == null ? -1 : latestID.intValue();
    }

    public <T extends RealmModel> List<T> loadAllInertialSensorData(Class<T> clazz) {
        LogUtil.d(TAG, String.format(Locale.ENGLISH, "load all %s", clazz.getSimpleName()));

        Realm realm = createOrFindRealm();

        return realm.copyFromRealm(realm.where(clazz).findAll());
    }

    public <T extends RealmModel> List<T> loadInertialSensorData(Class<T> clazz, int lower, int upper) {
        LogUtil.d(TAG, String.format(Locale.ENGLISH, "load %s from %d to %d", clazz.getSimpleName(), lower, upper));

        Realm realm = createOrFindRealm();

        return realm.copyFromRealm(realm.where(clazz).between("id", lower, upper).findAll());
    }

    public void saveInertialSequence(final InertialSequence inertialSequence, boolean sync) {
        LogUtil.d(TAG, "save inertial sequence");

        Realm realm = createOrFindRealm();

        if (sync) {
            realm.beginTransaction();
            realm.copyToRealm(inertialSequence.getAccelerations());
            realm.copyToRealm(inertialSequence.getAngularRates());
            realm.copyToRealm(inertialSequence.getOrientations());
            realm.copyToRealm(inertialSequence.getGravityAccelerations());
            realm.copyToRealm(inertialSequence.getLinearAccelerations());
            realm.copyToRealm(inertialSequence.getGpsPositions());
            realm.commitTransaction();
        } else {
            realm.executeTransactionAsync(tRealm -> {
                tRealm.copyToRealm(inertialSequence.getAccelerations());
                tRealm.copyToRealm(inertialSequence.getAngularRates());
                tRealm.copyToRealm(inertialSequence.getOrientations());
                tRealm.copyToRealm(inertialSequence.getGravityAccelerations());
                tRealm.copyToRealm(inertialSequence.getLinearAccelerations());
                tRealm.copyToRealm(inertialSequence.getGpsPositions());
            });
        }
    }

    public void cleanSensorDatabase(boolean sync) {
        LogUtil.d(TAG, "clear sensor database");

        Realm realm = createOrFindRealm();

        if (sync) {
            realm.beginTransaction();
            realm.where(Acceleration.class).findAll().deleteAllFromRealm();
            realm.where(AngularRate.class).findAll().deleteAllFromRealm();
            realm.where(Orientation.class).findAll().deleteAllFromRealm();
            realm.where(GPSPosition.class).findAll().deleteAllFromRealm();
            realm.where(GravityAcceleration.class).findAll().deleteAllFromRealm();
            realm.where(LinearAcceleration.class).findAll().deleteAllFromRealm();
            realm.commitTransaction();
        } else {
            realm.executeTransactionAsync(tRealm -> {
                tRealm.where(Acceleration.class).findAll().deleteAllFromRealm();
                tRealm.where(AngularRate.class).findAll().deleteAllFromRealm();
                tRealm.where(Orientation.class).findAll().deleteAllFromRealm();
                tRealm.where(GPSPosition.class).findAll().deleteAllFromRealm();
                tRealm.where(GravityAcceleration.class).findAll().deleteAllFromRealm();
                tRealm.where(LinearAcceleration.class).findAll().deleteAllFromRealm();
            });
        }
    }
}
