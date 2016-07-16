package com.gracecode.tracker.model;

import java.util.List;

import cn.bmob.v3.BmobObject;

/**
 * Created by zhouh on 16-3-3.
 */
public class UserModel extends BmobObject {
    private Integer userID;
    private Integer sportID;
    private float totalDistance;
    private long totalDuration;
    private String time;
    private double calorie;
    private Integer stepCount;
    private String isComplete;
    private List<MapModel> map;
    //private List<StepModel> step;

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public Integer getSportID() {
        return sportID;
    }

    public void setSportID(Integer sportID) {
        this.sportID = sportID;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = totalDistance;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getCalorie() {
        return calorie;
    }

    public void setCalorie(double calorie) {
        this.calorie = calorie;
    }

    public Integer getStepCount() {
        return stepCount;
    }

    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }

    public String getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(String isComplete) {
        this.isComplete = isComplete;
    }

    public List<MapModel> getMap() {
        return map;
    }

    public void setMap(List<MapModel> map) {
        this.map = map;
    }

    /*public List<StepModel> getStep() {
        return step;
    }

    public void setStep(List<StepModel> step) {
        this.step = step;
    }*/
}
