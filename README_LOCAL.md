# 车辆行驶数据收集APP

## 介绍

基于Android智能手机，收集车辆行驶过程中的加速度、角速度和GPS位置等信息

### 目录结构
```
.
|-- app
|---- src
|   |---- main  
|   |   |---- res                                               // UI资源文件
|   |   |---- java/com/hybrid/tripleldc                         // 核心代码
|   |   |   |---- bean
|   |   |   |       |---- base/BaseSensorData.java              // 传感器数据基类
|   |   |   |       |---- Acceleration.java                     // 加速度
|   |   |   |       |---- ArgbEvaluator.java                    // 渐变色插值
|   |   |   |       |---- DataCollectConfig.java                // 数据收集配置
|   |   |   |       |---- Device.java                           // 设备
|   |   |   |       |---- GPSPosition.java                      // GPS位置
|   |   |   |       |---- GravityAcceleration.java              // 重力加速度
|   |   |   |       |---- InertialSequence.java                 // 惯性数据序列（实时上传时使用）
|   |   |   |       |---- LaneChangeInfo.java                   // 变道事件（实时上传时使用）
|   |   |   |       |---- LinearAcceleration.java               // 线性加速度
|   |   |   |       |---- Orientation.java                      // 方向
|   |   |   |---- config
|   |   |   |       |---- DataConst.java                        // 数据格式，网络请求前缀等全局配置
|   |   |   |       |---- UIConst.java                          // 提示和弹框消息
|   |   |   |---- control
|   |   |   |       |---- DataCollectControl.java               // 数据收集（实时上传）的流程和控制逻辑
|   |   |   |       |---- OfflineDataCollectControl.java        // 数据收集（存储到本地）的流程和控制逻辑
|   |   |   |---- global
|   |   |   |       |---- App.java                              // 自定义Application（进行Realm数据库配置）
|   |   |   |       |---- TripleLDCMigration.java               // Realm数据库版本配置
|   |   |   |       |---- TripleLDCModule.java                  // Realm数据库表配置
|   |   |   |---- service
|   |   |   |       |---- DCService.java                        // 数据收集服务
|   |   |   |       |---- DUService.java                        // 数据上传服务（实时上传） 
|   |   |   |---- util
|   |   |   |       |---- io
|   |   |   |       |   |---- AsyncTaskRunner.java              // 异步任务
|   |   |   |       |   |---- FileIOUtil.java                   // 文件IO
|   |   |   |       |   |---- LogUtil.java                      // 日志工具
|   |   |   |       |   |---- RealmHelper.java                  // Realm数据库
|   |   |   |       |---- location
|   |   |   |       |   |---- GPSLocation.java                  // GPS位置
|   |   |   |       |   |---- GPSLocationListener.java          // GPS位置回调
|   |   |   |       |   |---- GPSLocationManager.java           // GPS服务注册管理
|   |   |   |       |   |---- GPSProviderStatus.java            // GPS服务状态
|   |   |   |       |---- sensor
|   |   |   |       |   |---- acceleration                      // 加速度计
|   |   |   |       |   |   |---- AccelerationSensor.java
|   |   |   |       |   |   |---- GravitySensor.java
|   |   |   |       |   |   |---- LinearAccelerationSensor.java
|   |   |   |       |   |---- gyroscope                         // 陀螺仪
|   |   |   |       |   |   |---- GyroSensor.java
|   |   |   |       |   |---- orientation                       // 方向
|   |   |   |       |   |   |---- OrientSensor.java
|   |   |   |       |   |---- BaseSensor.java                   // 传感器基类
|   |   |   |       |---- system
|   |   |   |       |   |---- AppUtil.java                      // 应用权限请求
|   |   |   |       |   |---- DateUtil.java                     // 日期格式转换
|   |   |   |       |   |---- SystemUtil.java                   // 系统文件调用
|   |   |   |       |---- task
|   |   |   |       |   |---- SensorDataExportTask.java         // 传感器数据导出
|   |   |   |       |---- ui
|   |   |   |       |   |---- AnimatorUtil.java                 // 动画
|   |   |   |       |   |---- DialogUtil.java                   // 对话框
|   |   |   |       |   |---- ToastUtil.java                    // 提示
|   |   |   |       |---- TripleLDCUtil.java                
|   |   |   |---- view
|   |   |   |       |---- activity                              // 界面
|   |   |   |       |   |--- base                               // 界面基类
|   |   |   |       |   |   |--- BaseActivity.java              
|   |   |   |       |   |---- DataCollectActivity.java          // 数据收集（实时上传）界面
|   |   |   |       |   |---- GPSTestActivity.java               
|   |   |   |       |   |---- OfflineDataCollectActivity.java   // 数据收集（存储到本地）界面
|   |   |   |       |   |---- SettingActivity.java              // 传感器数据导出界面
|   |   |   |       |   |---- TestActivity.java                  
|   |   |   |       |---- fragment
|   |   |   |       |---- widget                                // UI组件
|   |   |   |       |   |---- DCConfigView.java                 // 数据收集配置
|   |   |   |       |   |---- DCDisplayView.java                // 数据收集展示
|   |   |   |       |   |---- DCMainControlView.java            // 数据收集控制
|   |   |   |       |   |---- MainControlView.java              // 主界面控制
|   |   |   |       |   |---- MainDisplayView.java              // 主界面展示
|   |   |   |---- MainActivity.java                             // 程序入口（主界面）
|   |   |---- AndroidManifest.xml                               // UI资源文件
|---- build.grale                                               // gradle编译脚本
|---- proguard-rules.pro                                        // 混淆文件

```
### 主要界面展示
#### 主界面
<div style="display:flex;">
   <div style="margin-right: 20px">
       <img src="./figure/home%20interface%201.jpeg" alt="home interface normal" style="width: 250px; height: auto;">
   </div>
   <div>
       <img src="./figure/home%20interface%202.jpeg" alt="home interface working" style="width: 250px; height: auto;">
   </div>
</div>

#### 数据收集界面
<div style="display:flex;">
   <div style="margin-right: 20px">
       <img src="./figure/data%20collection%20interface%20normal.jpeg" alt="data collection interface normal" style="width: 250px; height: auto;">
   </div>
   <div>
       <img src="./figure/data%20collection%20interface%20working.jpeg" alt="data collection interface working" style="width: 250px; height: auto;">
   </div>
</div>

#### 数据收集配置弹窗
**注意：**
长按数据收集界面 **Start/Collecting** Button 展示
<div style="display:flex;">
   <div style="margin-right: 20px">
       <img src="./figure/data%20collection%20config%20dialog%20online.jpeg" alt="data collection config dialog online" style="width: 250px; height: auto;">
   </div>
   <div style="margin-right: 20px">
       <img src="./figure/data%20collection%20config%20dialog%20offline.jpeg" alt="data collection config dialog offline" style="width: 250px; height: auto;">
   </div>
</div>

#### 数据导出界面
<div style="display:flex;">
   <div style="margin-right: 20px">
       <img src="./figure/data%20export%20interface%20normal.jpeg" alt="data exprot interface normal" style="width: 250px; height: auto;">
   </div>
   <div style="margin-right: 20px">
       <img src="./figure/data%20export%20interface%20working.jpeg" alt="data exprot interface working" style="width: 250px; height: auto;">
   </div>
   <div style="margin-right: 20px">
       <img src="./figure/data%20export%20interface%20finished.jpeg" alt="data exprot interface finished" style="width: 250px; height: auto;">
   </div>
</div>

### 数据存储目录
#### 日志文件
**注意：**
需要修改 [LogUtil](./app/src/main/java/com/hybrid/tripleldc/util/io/LogUtil.java)，打开写日志开关
```java
private static boolean isNeedWriteLogToFile = true;
```
**日志输出目录：** /sdcard/Android/data/com.hybrid.tripleldc/files/Log

#### 传感器数据文件
在数据导出界面进行导出后，保存到以下目录  
**传感器数据输出目录：** /sdcard/Android/data/com.hybrid.tripleldc/files/InertialSequence

### 其他
<font face="STCAIYUN" size=3 color=green>请按照自己的需求参考各种模块的实现方式吧~</font>