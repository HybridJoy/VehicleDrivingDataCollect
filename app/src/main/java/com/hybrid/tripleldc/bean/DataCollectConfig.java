package com.hybrid.tripleldc.bean;

/**
 * Author: Joy
 * Created Time: 2021/7/21-20:48
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/21 )
 * <p>
 * Describe:
 */
public class DataCollectConfig {
    public String deviceName = "";
    public boolean isUploadData;
    public boolean isUseTestServer;
    public int sensorFrequency;
    public int dataSampleInterval;
    public int dataUploadInterval;
    public int maxReUploadTimes;
}
