package com.gracecode.tracker.model;

import cn.bmob.v3.BmobObject;

/**
 * Created by zhouh on 16-3-7.
 */
public class StepModel extends BmobObject {
    private Integer sportID;
    private Integer stepCount;
    private String poIntegerDate;

    public Integer getSportID() {
        return sportID;
    }

    public void setSportID(Integer sportID) {
        this.sportID = sportID;
    }

    public Integer getStepCount() {
        return stepCount;
    }

    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }

    public String getPoIntegerDate() {
        return poIntegerDate;
    }

    public void setPoIntegerDate(String poIntegerDate) {
        this.poIntegerDate = poIntegerDate;
    }
}
