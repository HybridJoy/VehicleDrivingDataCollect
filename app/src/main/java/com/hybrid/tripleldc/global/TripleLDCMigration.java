package com.hybrid.tripleldc.global;

import com.hybrid.tripleldc.util.io.LogUtil;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Author: Joy
 * Created Time: 2022/3/5-22:40
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2022/3/5 )
 * <p>
 * Describe:
 */
public class TripleLDCMigration implements RealmMigration {
    private static final String TAG = "TripleLDCMigration";

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        LogUtil.d(TAG, String.format("old version: {}, new version: {}", oldVersion, newVersion));

        // 增加更新的字段和新增类
        RealmSchema schema = realm.getSchema();
        if (oldVersion == 1 && newVersion == 2) {
            schema.create("Device")
                    .addField("id", int.class, FieldAttribute.PRIMARY_KEY)
                    .addField("name", String.class);
            oldVersion++;
        }
    }
}
