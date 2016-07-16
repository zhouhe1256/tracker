package com.gracecode.tracker;

import android.app.Application;
import android.location.Location;

import com.amap.api.location.AMapLocation;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.model.UserModel;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import cn.bmob.v3.Bmob;

/**
 * Created by bjcathay on 16-1-26.
 */
public class TrackerApplication extends Application{
    private static  TrackerApplication single =new TrackerApplication();
    private ArrayList<AMapLocation> tenlocations;
    private ArchiveMeta meta;
    private UserModel userModel ;
    public ArrayList<AMapLocation> getTenlocations(){
        return tenlocations;
    }
    public float distance;
    public static boolean ff = true;//暂停时等于false
    public static boolean fff = true;//暂停时等于false
    public static int count = 0;
    public static int count_2 = 0;
    public String pace;
    public static float d1 = 0;
    public Date pauseStartDate;
    public Date pauseEndDate;

    public Date aveDate;
    public static int TIME = 0;
    public static int DISTANCE = 0;
    public static boolean tixing = true;
    public static int SPOIT_ID = 0;
    public ArrayList<AMapLocation>  pauseLocation=new ArrayList<AMapLocation>();

    public static TrackerApplication getInstance() {
            return single;
    }

    @Override
    public void onCreate() {
        // 应用程序入口处调用,避免手机内存过小，杀死后台进程,造成SpeechUtility对象为null
        // 设置你申请的应用appid
        StringBuffer param = new StringBuffer();
        param.append("appid="+getString(R.string.app_id));
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(TrackerApplication.this, param.toString());
        super.onCreate();
        // 初始化 Bmob SDK
        // 使用时请将第二个参数Application ID替换成你在Bmob服务器端创建的Application ID
        Bmob.initialize(this, "ed175c5da0318e158d348868b0cf6c1d");

    }

    public String getPace() {
        return pace;
    }

    public void setPace(String pace) {
        this.pace = pace;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setTenlocations(ArrayList<AMapLocation> tenlocations){
        this.tenlocations=tenlocations;
    }
    public void clearDate(){
        tenlocations.clear();
    }
    public void setMeta(ArchiveMeta meta){
        this.meta=meta;
    }
    public ArchiveMeta getMeta(){
        return meta;
    }

    public void setPauseStartDate(Date pauseStartDate) {
        this.pauseStartDate = pauseStartDate;
    }

    public Date getPauseStartDate() {
        return pauseStartDate;
    }

    public void setPauseEndDate(Date pauseEndDate) {
        this.pauseEndDate = pauseEndDate;
    }

    public Date getPauseEndDate() {
        return pauseEndDate;
    }

    public void setAveDate(Date aveDate) {
        this.aveDate = aveDate;
    }

    public Date getAveDate() {
        return aveDate;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
}
