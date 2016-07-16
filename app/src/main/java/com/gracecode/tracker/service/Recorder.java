package com.gracecode.tracker.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.gracecode.tracker.R;
import com.gracecode.tracker.TrackerApplication;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.step.StepDetector;
import com.gracecode.tracker.step.StepService;
import com.gracecode.tracker.ui.activity.Preference;
import com.gracecode.tracker.util.Helper;
import com.gracecode.tracker.util.Notifier;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @todo - need to fix recording flag errors
 */
interface Binder {
    public static final int STATUS_RECORDING = 0x0000;
    public static final int STATUS_STOPPED = 0x1111;

    public void startRecord();

    public void stopRecord();

    public int getStatus();

    public ArchiveMeta getMeta();

    public Archiver getArchive();

    public AMapLocation getLastRecord();
}

public class Recorder extends Service {
    TrackerApplication trackerApplication;
    public static boolean isGpsEnable = false;
    protected ServiceBinder serviceBinder = null;
    private SharedPreferences sharedPreferences;
    private Archiver archiver;

    private GaoDeListener listener;
    private StatusListener statusListener;

    private ArchiveNameHelper nameHelper;
    private String archivName;
    private Helper helper;
    private Context context;
    private Notifier notifier;

    private static final String RECORDER_SERVER_ID = "Tracker Service";
    private static final String PREF_STATUS_FLAG = "Tracker Service Status";
    private TimerTask notifierTask;
    private Timer timer = null;

    /*****************记步*********************/
    private static final String TAG = "StepService";
    public static boolean serviceFlag = false;// 服务开关标志
    public static boolean stableWalkStatusFlag = false;// 稳定走路状态标志，为true的话SELECTED_STEP=CURRENT_SETP
    public static int SELECTED_STEP = 0;// 筛选过的步数
    private int stepToday;
    private int stepHistory = -1;
    private static final int MIN_STEP_IN_10SECONDS = 10;// 10s内最小步数，人类的一步在0.2s到2s之间
    private static final int MAX_STEP_IN_10SECONDS = 50;// 10s内最大步数
    // 传感器
    private SensorManager mSensorManager;
    private Sensor mSensor, mStepDetectorSensor, mStepCountSensor;
    private StepDetector mStepDetector;// 信号监听记步类
    // 通知
    private static final int NOTIFICATION_STEP_SERVICE = 1;// 通知标记
    private static final String NOTIFICATION_STEP_SERVICE_TICKER = "通知";// 通知ticker，在通知刚生成时在手机最上方弹出的一闪而过的提示
    private static final String NOTIFICATION_STEP_SERVICE_TITLE = "通知标题";// 通知标题
    private static final String NOTIFICATION_STEP_SERVICE_CONTENT = " 通知内容";// 通知内容
    private static final float SCALE_STEP_CALORIES = 43.22f;
    private Notification.Builder builder;
    private Notification mNotification;
    // 线程
    private Thread selectStepThread;// 此线程用来筛选步数
    private Thread updateStepThread;// 此线程用来更新通知栏的步数

    private LocationManager locationManager;


    /**
     * 开始监听步数
     */
    @SuppressLint("InlinedApi")
    private void startStepDetector() {
        if (mStepDetector == null) {
            mStepDetector = new StepDetector(this);// 如果不为空，new一个
        }
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);// 得到传感器管理器SensorManager
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// 得到加速度计传感器
        // 如果SDK版本大于等于19，则试着获取系统自带的步数传感器
        if (Build.VERSION.SDK_INT >= 19) {
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);// 得到STEP_DETECTOR传感器
            mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);// 得到STEP_COUNTER传感器
        }
        // 如果存在系统自带的步数传感器，则使用系统的，否则使用加速度传感器
        if (mStepDetectorSensor != null) {
            mSensorManager.registerListener(mStepDetector, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
            // 开启更新通知3线程
            //  initUpdateStepDetectorThread();
            //  updateStepThread.start();
        } else if (mStepCountSensor != null) {
            mSensorManager.registerListener(mStepDetector, mStepCountSensor, SensorManager.SENSOR_DELAY_FASTEST);
            // 开启更新通知2线程
            // initUpdateStepCountThread();
            //  updateStepThread.start();
        } else {
            mSensorManager.registerListener(mStepDetector, mSensor, SensorManager.SENSOR_DELAY_FASTEST);// 注册监听
            // 开启筛选步数线程
            initSelectStepThread();
            selectStepThread.start();
            // 开启更新通知线程
            //  initUpdateStepThread();
            //  updateStepThread.start();
        }
    }

    /**
     * 初始化筛选线程
     */
    private void initSelectStepThread() {
        selectStepThread = new Thread() {
            private int step_before;// 计时之前的步数
            private int step_after;// 计时之后的步数
            private int step_diff;// 差值
            private String date_before = new java.sql.Date(System.currentTimeMillis()).toString();

            @Override
            public void run() {
                while (serviceFlag) {
                    step_before = StepDetector.CURRENT_SETP;
                    try {
                        sleep(10000);// 十秒之后
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String date_after = new java.sql.Date(System.currentTimeMillis()).toString();
                    // 如果10s后天数没有更迭
                    if (date_after.equals(date_before)) {
                        Log.i(TAG, "date_before" + date_before);
                        Log.i(TAG, "date_after" + date_after);
                        step_after = StepDetector.CURRENT_SETP;
                        step_diff = step_after - step_before;
                        if (step_diff > MIN_STEP_IN_10SECONDS && step_diff < MAX_STEP_IN_10SECONDS) {
                            stableWalkStatusFlag = true;// 进入稳定走路状态
                        } else {
                            stableWalkStatusFlag = false;// 退出稳定走路状态
                            StepDetector.CURRENT_SETP = StepDetector.CURRENT_SETP - step_diff;// 将步数不达标的无用步数减去
                        }
                    } else {
                        StepDetector.CURRENT_SETP = 0;
                        date_before = date_after;
                    }
                }
            }
        };
    }

    /**
     * 初始化更新通知线程
     */
    private void initUpdateStepThread() {
        updateStepThread = new Thread() {
            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void run() {
                while (serviceFlag) {// 当服务运行时，线程开启
                    try {
                        sleep(1000);// 每隔一秒更新一次
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (stableWalkStatusFlag) {
                        SELECTED_STEP = StepDetector.CURRENT_SETP;
                        //  archiver.getMeta().setStep(SELECTED_STEP);
//                        if (!dbHelper.updateData(db, SELECTED_STEP)) {// 每隔1s更新一次数据库，如果更新失败（此处情况为两天交替时发生）
//                            dbHelper.insertData(db, 0);// 为第二天添加一个新的数据列
//
//                            StepDetector.CURRENT_SETP = 0;
//                            SELECTED_STEP = 0;
//                        }
                    }
//                    builder.setContentTitle(SELECTED_STEP + NOTIFICATION_STEP_SERVICE_TITLE);
//                    builder.setContentText(String.format("%.1f", (SELECTED_STEP * SCALE_STEP_CALORIES) / 1000)
//                            + NOTIFICATION_STEP_SERVICE_CONTENT);
//                    if (Build.VERSION.SDK_INT >= 16) {
//                        mNotification = builder.build();
//                    } else {
//                        mNotification = builder.getNotification();
//                    }
//                    mNotification.flags = Notification.FLAG_NO_CLEAR;// 在点击通知后，通知并不会消失
//                    startForeground(NOTIFICATION_STEP_SERVICE, mNotification);// 在通知栏添加前台服务
                    // Log.i(TAG, "" + StepDetector.CURRENT_SETP);
                }
            }
        };
    }

    /**
     * 初始化更新通知线程2,当系统自带记步方法TYPE_STEP_COUNTER时启用这个线程
     */
    private void initUpdateStepCountThread() {
        updateStepThread = new Thread() {
            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void run() {
                while (serviceFlag) {// 当服务运行时，线程开启
                    try {
                        sleep(1000);// 每隔一秒更新一次
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 获得今天的步数
                    // int step = dbHelper.queryData(db, new java.sql.Date(System.currentTimeMillis()).toString());
                    int step = Integer.valueOf(archiver.getMeta().getStep());
                    // 如果stepHistory还未初始化（即等于-1），将系统总步数赋给它
                    if (stepHistory == -1) {
                        stepHistory = StepDetector.STEP_COUNT
                                - step;/** 核心算法：这个算法相当的巧妙，即使算出的结果为负数，也是我们需要的 **/
                        Log.i(TAG, "stepHistory----->" + stepHistory);
                    }
                    // 今天走的步数等于总步数减去历史步数
                    stepToday = StepDetector.STEP_COUNT
                            - stepHistory;/** 核心算法：这个算法相当的巧妙 **/
//                    if (!dbHelper.updateData(db, stepToday)) {// 每隔1s更新一次数据库，如果更新失败（此处情况为两天交替时发生）
//                        dbHelper.insertData(db, 0);// 为第二天添加一个新的数据列
//                        stepHistory = -1;
//                    }
                    // archiver.getMeta().setStep(stepToday);
//                    builder.setContentTitle(stepToday + NOTIFICATION_STEP_SERVICE_TITLE);
//                    builder.setContentText(String.format("%.1f", (stepToday * SCALE_STEP_CALORIES) / 1000)
//                            + NOTIFICATION_STEP_SERVICE_CONTENT);
//                    if (Build.VERSION.SDK_INT >= 16) {
//                        mNotification = builder.build();
//                    } else {
//                        mNotification = builder.getNotification();
//                    }
//                    mNotification.flags = Notification.FLAG_NO_CLEAR;// 在点击通知后，通知并不会消失
//                    startForeground(NOTIFICATION_STEP_SERVICE, mNotification);// 在通知栏添加前台服务
                    // Log.i(TAG, "" + StepDetector.CURRENT_SETP);
                }
            }
        };
    }

    /**
     * 初始化更新通知线程3,当系统自带记步方法TYPE_STEP_DETECTOR时启用这个线程
     */
    private void initUpdateStepDetectorThread() {
        updateStepThread = new Thread() {
            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void run() {
                while (serviceFlag) {// 当服务运行时，线程开启
                    try {
                        sleep(1000);// 每隔一秒更新一次
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // archiver.getMeta().setStep(StepDetector.STEP_DETECTOR);
//                    if (!dbHelper.updateData(db, StepDetector.STEP_DETECTOR)) {// 每隔1s更新一次数据库，如果更新失败（此处情况为两天交替时发生）
//                        dbHelper.insertData(db, 0);// 为第二天添加一个新的数据列
//                        StepDetector.STEP_DETECTOR = 0;
//                    }
//                    builder.setContentTitle(StepDetector.STEP_DETECTOR + NOTIFICATION_STEP_SERVICE_TITLE);
//                    builder.setContentText(
//                            String.format("%.1f", (StepDetector.STEP_DETECTOR * SCALE_STEP_CALORIES) / 1000)
//                                    + NOTIFICATION_STEP_SERVICE_CONTENT);
//                    if (Build.VERSION.SDK_INT >= 16) {
//                        mNotification = builder.build();
//                    } else {
//                        mNotification = builder.getNotification();
//                    }
//                    mNotification.flags = Notification.FLAG_NO_CLEAR;// 在点击通知后，通知并不会消失
//                    startForeground(NOTIFICATION_STEP_SERVICE, mNotification);// 在通知栏添加前台服务
//                    Log.i(TAG, "" + StepDetector.CURRENT_SETP);
                }
            }
        };
    }

    /*****************记步*********************/


    public class ServiceBinder extends android.os.Binder implements Binder {
        ServiceBinder() {
            trackerApplication = TrackerApplication.getInstance();
            archiver = new Archiver(getApplicationContext());
            listener = new GaoDeListener(archiver, this, getApplication());
            statusListener = new StatusListener(context);



        }
//        /**
//         * 显示通知
//         */
//        @SuppressLint("NewApi")
//        @SuppressWarnings("deprecation")
//        private void showNotification() {
//            builder = new Notification.Builder(getApplicationContext());// 使用Notification.Builder创建Notification
//            // 将通知栏设置为自定义View
//            builder.setContentTitle(SELECTED_STEP + NOTIFICATION_STEP_SERVICE_TITLE);
//            builder.setContentText(String.format("%.1f", (SELECTED_STEP * SCALE_STEP_CALORIES) / 1000)
//                    + NOTIFICATION_STEP_SERVICE_CONTENT);
//            builder.setTicker(NOTIFICATION_STEP_SERVICE_TICKER);
//            builder.setSmallIcon(R.drawable.icon);
//            if (Build.VERSION.SDK_INT >= 16) {
//                mNotification = builder.build();
//            } else {
//                mNotification = builder.getNotification();
//            }
//            mNotification.flags = Notification.FLAG_NO_CLEAR;// 在点击通知后，通知并不会消失
//            startForeground(NOTIFICATION_STEP_SERVICE, mNotification);// 在通知栏添加前台服务
//        }
        @Override
        public void startRecord() {
            if (getStatus() != ServiceBinder.STATUS_RECORDING) {
                /************/
                serviceFlag = true;// 服务开启标志置为true
                int step =0;
                // 获取今天的数据
                StepDetector.CURRENT_SETP = step;
                SELECTED_STEP = step;
               // showNotification();// 在通知栏显示
                startStepDetector();// 开始监听步数
                /************/
                listener.startLocation();
                // 设置启动时更新配置
                notifier = new Notifier(context);
                // 如果没有外置存储卡
                if (!nameHelper.isExternalStoragePresent()) {
                    helper.showLongToast(getString(R.string.external_storage_not_present));
                    return;
                }

                // 从配置文件获取距离和精度选项
                long minTime = Long.parseLong(sharedPreferences.getString(Preference.GPS_MINTIME,
                        Preference.DEFAULT_GPS_MINTIME));
                float minDistance = Float.parseFloat(sharedPreferences.getString(Preference.GPS_MINDISTANCE,
                        Preference.DEFAULT_GPS_MINDISTANCE));

                // 判定是否上次为异常退出
                boolean hasResumeName = nameHelper.hasResumeName();
                if (hasResumeName) {
                    archivName = nameHelper.getResumeName();
                    helper.showLongToast(
                            String.format(
                                    getString(R.string.use_resume_archive_file, archivName)
                            ));
                } else {
                    archivName = nameHelper.getNewName();
                }

                try {
                    archiver.open(archivName, Archiver.MODE_READ_WRITE);

                    // Set start time, if not resume from recovery
                    if (!hasResumeName) {
                        getMeta().setStartTime(new Date());
                    }


                    // 标记打开的文件，方便奔溃时恢复
                    nameHelper.setLastOpenedName(archivName);
                } catch (SQLiteException e) {
                    Helper.Logger.e(e.getMessage());
                }

                // 另开个线程展示通知信息
                notifierTask = new TimerTask() {
                    @Override
                    public void run() {
                        switch (serviceBinder.getStatus()) {
                            case ServiceBinder.STATUS_RECORDING:
                                ArchiveMeta meta = getMeta();
                                float distance = meta.getDistance() / ArchiveMeta.TO_KILOMETRE;
                                double avgSpeed = meta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT;
                                double maxSpeed = meta.getMaxSpeed() * ArchiveMeta.KM_PER_HOUR_CNT;

                                notifier.setStatusString(
                                        String.format(getString(R.string.status_format),
                                                distance, avgSpeed, maxSpeed)
                                );
                                notifier.setCostTimeString(meta.getCostTimeStringByNow());
                                notifier.publish();
                                break;

                            case ServiceBinder.STATUS_STOPPED:
                                notifier.cancel();
                                break;
                        }
                    }
                };
                timer = new Timer();
                timer.schedule(notifierTask, 0, 5000);

                // Set status from shared preferences, default is stopped.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(PREF_STATUS_FLAG, ServiceBinder.STATUS_RECORDING);
                editor.commit();

                // for umeng
                MobclickAgent.onEventBegin(context, RECORDER_SERVER_ID);

            }

        }



        public void resetStatus() {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_STATUS_FLAG, ServiceBinder.STATUS_STOPPED);
            editor.commit();
        }

        @Override
        public void stopRecord() {
            if (getStatus() == ServiceBinder.STATUS_RECORDING) {
                TrackerApplication.getInstance().setDistance(0);
                TrackerApplication.getInstance().setPace("");
//                TrackerApplication.getInstance().setPauseStartDate(null);
//                TrackerApplication.getInstance().setPauseEndDate(null);
                /*******/
                serviceFlag = false;// 服务开启标志置为false,停止筛选线程和更新线程
                stopForeground(true);// 在通知栏停止前台服务
                if (mStepDetector != null) {// 如果记步类对象依然存在
                    mSensorManager.unregisterListener(mStepDetector);// 取消监听
                    mStepDetector = null;// 将记步类对象置为null
                }
                /******/
                listener.endLocation();
                // Flush listener cache
                /*if(TrackerApplication.fff){
                    listener.flushCache(1);
                }else{
                    listener.flushCache(0);
                }*/

                // Remove listener


                ArchiveMeta meta = getMeta();
                meta.setSpeedByNow("");
                long totalCount = meta.getCount();
                if (totalCount <= 0) {
                    (new File(archivName)).delete();
                    helper.showLongToast(getString(R.string.not_record_anything));
                } else {
//
//                    if(TrackerApplication.getInstance().getPauseEndDate()!=null&&TrackerApplication.getInstance().getPauseStartDate()!=null){
//                        Date date = new Date(new Date().getTime()-(TrackerApplication.getInstance().getPauseEndDate().getTime()-TrackerApplication.getInstance().getPauseStartDate().getTime()));
//                        meta.setEndTime(date);
//                    }else{
//                        meta.setEndTime(new Date());
//                    }
                   meta.setEndTime(new Date());

                    // Show record result by toast
                    helper.showLongToast(String.format(
                            getString(R.string.result_report), String.valueOf(totalCount)
                    ));
                }

                // 清除操作
                archiver.close();
                notifier.cancel();

                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }

                nameHelper.clearLastOpenedName();

                // Set status from preference as stopped.
                resetStatus();

                MobclickAgent.onEventEnd(context, RECORDER_SERVER_ID);

            }
        }

        @Override
        public int getStatus() {
            return sharedPreferences.getInt(PREF_STATUS_FLAG, ServiceBinder.STATUS_STOPPED);
        }

        @Override
        public ArchiveMeta getMeta() {
            return archiver.getMeta();
        }

        @Override
        public Archiver getArchive() {
            return archiver;
        }

        @Override
        public AMapLocation getLastRecord() {
            return archiver.getLastRecord();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        this.context = getApplicationContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        this.nameHelper = new ArchiveNameHelper(context);
        this.helper = new Helper(context);
        if (serviceBinder == null) {
            serviceBinder = new ServiceBinder();
        }

        boolean autoStart = sharedPreferences.getBoolean(Preference.AUTO_START, false);
        boolean alreadyStarted = (serviceBinder.getStatus() == ServiceBinder.STATUS_RECORDING);

        if (autoStart || alreadyStarted) {
            if (alreadyStarted) {
                serviceBinder.resetStatus();
            }
            serviceBinder.startRecord();
        }
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        serviceBinder.stopRecord();
        listener.destroy();
        serviceFlag = false;// 服务开启标志置为false,停止筛选线程和更新线程
        stopForeground(true);// 在通知栏停止前台服务
        if (mStepDetector != null) {// 如果记步类对象依然存在
            mSensorManager.unregisterListener(mStepDetector);// 取消监听
            mStepDetector = null;// 将记步类对象置为null
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}
