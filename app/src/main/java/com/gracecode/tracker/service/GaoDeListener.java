
package com.gracecode.tracker.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.gracecode.tracker.R;
import com.gracecode.tracker.TrackerApplication;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.model.FriendAddress;
import com.gracecode.tracker.model.MapModel;
import com.gracecode.tracker.model.UserModel;
import com.gracecode.tracker.step.StepDetector;
import com.gracecode.tracker.ui.activity.Tracker;
import com.gracecode.tracker.util.Helper;
import com.gracecode.tracker.util.PreferencesUtils;
import com.iflytek.cloud.ErrorCode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.listener.SaveListener;

/**
 * Created by zhouh on 16-1-26.
 */
public class GaoDeListener implements AMapLocationListener {
    private AMapLocationClientOption locationOption = null;
    private AMapLocationClient locationClient = null;
    private Context context;
    private final static int ACCURACY = 3;
    private final static int CACHE_SIZE = 2;

    private Archiver archiver;
    private ArchiveMeta meta = null;
    private double lastLatitude;
    private double lastLongitude;
    private AlarmManager am;
    private PendingIntent pi;
    private HashMap<Long, AMapLocation> locationCache;
    LocationReceiver locationReceiver;
    public TrackerApplication trackerApplication;
    //精度
    public static float accuracy;
    public boolean ifcuan = true;
    protected Recorder.ServiceBinder serviceBinder = null;
    private FriendAddress friendAddress;
    private long time = 0;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                friendAddress.save(context, new SaveListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int i, String s) {

                    }
                });
                ifcuan = true;
            }

            super.handleMessage(msg);
        }
    };
    public GaoDeListener(Archiver archiver, Recorder.ServiceBinder binder, Context context) {
        this.context = context;
        this.archiver = archiver;
        this.serviceBinder = binder;
        this.locationCache = new HashMap<Long, AMapLocation>();
        init();
    }

    private boolean filter(AMapLocation location) {

        double longitude=location.getLongitude();
        double latitude=location.getLatitude();
        if (latitude == 0.0 || longitude ==0.0) {
            return false;
        }

        if(location.getSpeed()<=0.0||location.getAltitude()<=0.0){
            return false;
        }

        if(location.getAccuracy()>20){
            return false;
        }

        lastLatitude = latitude;
        lastLongitude = longitude;
        return true;
    }

    private void init() {
        trackerApplication = TrackerApplication.getInstance();
        IntentFilter intentFile = new IntentFilter();
        intentFile.addAction("repeating");
        locationReceiver = new LocationReceiver();
        context.registerReceiver(locationReceiver, intentFile);

        Intent intent = new Intent();
        intent.setAction("repeating");
        pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        locationOption = new AMapLocationClientOption();
        locationClient = new AMapLocationClient(context);
        locationClient.setLocationListener(this);
        locationOption.setOnceLocation(false);
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setInterval(2000);
        locationOption.setNeedAddress(false);

        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        // locationOption.setGpsFirst(true);
    }

    class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!locationClient.isStarted()) {
                locationClient.startLocation();
            }
        }

    }

    long lasttime;

    AMapLocation lastComputedLocation;
   // boolean startBoolean = true;//第一个点
   // Date s1 = null;//相对于上个点的时间
    //Date startDate = null;//开始点的时间
    //long duration;//相对2个点的前个点的时间
    //int counts = 0;//sportId 目前为计数...

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        if (filter(aMapLocation)) {

            accuracy=aMapLocation.getAccuracy();
            // locationCache.put(System.currentTimeMillis(), aMapLocation);
            if(TrackerApplication.fff){
                flushSingleCache(aMapLocation, System.currentTimeMillis(),1);
            }else{
                flushSingleCache(aMapLocation, System.currentTimeMillis(),0);
            }

            // 计算动态路径
            this.meta = archiver.getMeta();

            if (meta != null) {
                if (lastComputedLocation != null) {
                    float distance = trackerApplication.getDistance();
                    LatLng startLatlng = new LatLng(lastComputedLocation.getLatitude(),
                            lastComputedLocation.getLongitude());
                    LatLng endLatlng = new LatLng(aMapLocation.getLatitude(),
                            aMapLocation.getLongitude());
                    /*if(startBoolean){
                       // startBoolean = false;
                        //startDate = new Date();
                    }*/

                        if(TrackerApplication.fff){
                        trackerApplication.setDistance(
                                distance + AMapUtils.calculateLineDistance(startLatlng, endLatlng));
                             TrackerApplication.d1 =  TrackerApplication.d1+AMapUtils.calculateLineDistance(startLatlng, endLatlng);
                        }
                    Date s2 = new Date();
                   // duration = s2.getTime()-s1.getTime();

                    friendAddress = new FriendAddress();
                    friendAddress.setLongitude(aMapLocation.getLongitude());
                    friendAddress.setLatitude(aMapLocation.getLatitude());
                    friendAddress.setDistance(trackerApplication.getDistance());
                    friendAddress.setFristTime(time);
                    friendAddress.setUserName("张三");
                    if(ifcuan){
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        handler.sendMessage(msg);
                        ifcuan = false;
                    }
                    if(TrackerApplication.d1>=1000f){
                        TrackerApplication.d1 = 0;
                    }else{
                    }
                    lastComputedLocation = aMapLocation;
                    //s1 = new Date();
                   // }

                } else if (lastComputedLocation == null) {
                    lastComputedLocation = aMapLocation;
                   // s1 = new Date();
                }
                trackerApplication.setAveDate(new Date());
                trackerApplication.setPace(String.valueOf(aMapLocation.getSpeed()));
                meta.setRawDistance(trackerApplication.getDistance());
                meta.setClimbing();
                meta.setSpeedByNow(String.valueOf(aMapLocation.getSpeed()));
                // meta.setStep(Integer.valueOf(meta.getStep())+StepDetector.CURRENT_SETP);

            }




        }

    }
    /**
     * flush cache
     */
    public void flushSingleCache(AMapLocation location,Long timeMillis,int stop) {
        if (archiver.add(location, timeMillis, StepDetector.CURRENT_SETP,stop)) {
            StepDetector.CURRENT_SETP=0;
        }
    }
    /**
     * flush cache
     */
    public void flushCache(int stop) {
        Iterator<Long> iterator = locationCache.keySet().iterator();
        while (iterator.hasNext()) {
            Long timeMillis = iterator.next();
            AMapLocation location = locationCache.get(timeMillis);
            if (archiver.add(location, timeMillis, StepDetector.CURRENT_SETP,stop)) {
                Helper.Logger.i(String.format(
                        "Location(%f, %f) has been saved into database.", location.getLatitude(),
                        location.getLongitude()));
            }
        }

        locationCache.clear();
    }

    /**
     * 开始定位
     */
    public void startLocation() {
        locationClient.setLocationOption(locationOption);
        locationClient.startLocation();
        time = new Date().getTime();
        long time = SystemClock.currentThreadTimeMillis();
        am.cancel(pi);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, 1000, pi);
    }

    public void destroy() {
        am.cancel(pi);
        context.unregisterReceiver(locationReceiver);
        if (null != locationClient) {
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
        meta.setSpeedByNow("");
        trackerApplication.setPace("");
        trackerApplication.setDistance(0);
        lastComputedLocation=null;
        lasttime = 0;

    }

    /**
     * 停止定位
     */
    public void endLocation() {

        locationClient.stopLocation();




    }

}
