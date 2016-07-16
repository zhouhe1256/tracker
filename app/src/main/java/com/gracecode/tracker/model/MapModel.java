package com.gracecode.tracker.model;

import cn.bmob.v3.BmobObject;

/**
 * Created by zhouh on 16-3-7.
 */
public class MapModel  {
    private Integer sportID;
    private double latitude;
    private double longitude;
    private double altitude;
    private String pointDate;
    private double toPreviousDistance;
    private long toPreviousDuration;
    private double toStartDistance;
    private long toStartDuration;
    private double speed;
    private String isSport;
    private double horizontalAccuracy;
    private double verticalAccuracy;
    private String isKMPoint;

    public Integer getSportID() {
        return sportID;
    }

    public void setSportID(Integer sportID) {
        this.sportID = sportID;
    }

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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getPointDate() {
        return pointDate;
    }

    public void setPointDate(String poIntegerDate) {
        this.pointDate = poIntegerDate;
    }

    public double getToPreviousDistance() {
        return toPreviousDistance;
    }

    public void setToPreviousDistance(double toPreviousDistance) {
        this.toPreviousDistance = toPreviousDistance;
    }

    public long getToPreviousDuration() {
        return toPreviousDuration;
    }

    public void setToPreviousDuration(long toPreviousDuration) {
        this.toPreviousDuration = toPreviousDuration;
    }

    public double getToStartDistance() {
        return toStartDistance;
    }

    public void setToStartDistance(double toStartDistance) {
        this.toStartDistance = toStartDistance;
    }

    public long getToStartDuration() {
        return toStartDuration;
    }

    public void setToStartDuration(long toStartDuration) {
        this.toStartDuration = toStartDuration;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getIsSport() {
        return isSport;
    }

    public void setIsSport(String isSport) {
        this.isSport = isSport;
    }

    public double getHorizontalAccuracy() {
        return horizontalAccuracy;
    }

    public void setHorizontalAccuracy(double horizontalAccuracy) {
        this.horizontalAccuracy = horizontalAccuracy;
    }

    public double getVerticalAccuracy() {
        return verticalAccuracy;
    }

    public void setVerticalAccuracy(double verticalAccuracy) {
        this.verticalAccuracy = verticalAccuracy;
    }

    public String getIsKMPoint() {
        return isKMPoint;
    }

    public void setIsKMPoint(String isKMPoint) {
        this.isKMPoint = isKMPoint;
    }
}
