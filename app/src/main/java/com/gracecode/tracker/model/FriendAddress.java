package com.gracecode.tracker.model;

import cn.bmob.v3.BmobObject;

/**
 * Created by zhouh on 16-3-3.
 */
public class FriendAddress extends BmobObject{
    private String userName;//用户姓名
    private double latitude; //纬度
    private double longitude;//经度
    private int stop;//如果为0当前状态为暂停,如果为1当前状态为跑步状态
    private float distance;//当前跑步的距离
    private float speed;//配速
    private long fristTime;
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public long getFristTime() {
        return fristTime;
    }

    public void setFristTime(long fristTime) {
        this.fristTime = fristTime;
    }
}
