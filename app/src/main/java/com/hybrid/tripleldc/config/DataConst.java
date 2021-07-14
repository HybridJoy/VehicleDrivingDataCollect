package com.hybrid.tripleldc.config;

public class DataConst {

    public static class OkHttpConfig {
        public static String ALIYUN_SERVER_SEVER_URL = "http://47.112.138.173:20558";
        public static String LOCAL_SERVER_URL = "http://192.168.123.24:20558";

        // 在此配置服务器URL
        public static String SERVER_URL = ALIYUN_SERVER_SEVER_URL;
    }

    public static class HttpErrorCode {
        public static int RESOURCE_NOT_FIND = 404;
        public static int SERVER_MISTAKE = 500;
    }

    public static class RequestTag {
        public static String REQUEST_EMPTY_TAG = "request_empty_tag";

        public static String REQUEST_TEST_PREFIX = "request_test_";
        public static String REQUEST_TEST_SERVER_CONNECT_TAG = REQUEST_TEST_PREFIX + "server_connect";

        public static String REQUEST_UPLOAD_PREFIX = "request_upload_";
        public static String REQUEST_UPLOAD_LANE_CHANGE_INFO_TAG = REQUEST_UPLOAD_PREFIX + "drive_trajectory";

        public static String REQUEST_GET_PREFIX = "request_get_";

        public static String REQUEST_TEST_FUNCTION_TAG = "request_test_function_tag";

    }

    public static class Request {
        public static String REQUEST_TEST_SERVER_CONNECT = "/V2I/linktest";
        public static String REQUEST_UPLOAD_LANE_CHANGE_INFO = "/V2I/DrivingTrajCollection";

    }

    public static class Device {
        public static String DEVICE_NAME_PREFIX = "device_";
        public static int DEVICE_NAME_LENGTH = 20;
    }
}
