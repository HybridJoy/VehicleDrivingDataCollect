package com.hybrid.tripleldc.global;

import com.hybrid.tripleldc.bean.Acceleration;
import com.hybrid.tripleldc.bean.AngularRate;
import com.hybrid.tripleldc.bean.Device;
import com.hybrid.tripleldc.bean.GPSPosition;
import com.hybrid.tripleldc.bean.GravityAcceleration;
import com.hybrid.tripleldc.bean.LinearAcceleration;
import com.hybrid.tripleldc.bean.Orientation;

import io.realm.annotations.RealmModule;

/**
 * Author: Joy
 * Created Time: 2022/3/5-22:35
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2022/3/5 )
 * <p>
 * Describe:
 */

@RealmModule(classes = {
        Acceleration.class,
        AngularRate.class,
        Orientation.class,
        GPSPosition.class,
        GravityAcceleration.class,
        LinearAcceleration.class,
        Device.class // version = 2, 新增 Device
})
public class TripleLDCModule {

}
