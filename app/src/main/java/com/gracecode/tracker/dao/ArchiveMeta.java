package com.gracecode.tracker.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.gracecode.tracker.ui.activity.Tracker;
import com.gracecode.tracker.util.Helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class ArchiveMeta {
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String END_TIME = "END_TIME";
    public static final String START_TIME = "START_TIME";
    public static final String DISTANCE = "DISTANCE";
    public static final String CALORIE = "CALORIE";
    public static final String WEIGHT="WEIGHT";
    public static final String HEIGHT="HEIGHT";
    public static final String CLIBING="CLIBING";
    public static final String STEP="STEP";
    public static final String SPEED="SPEED";

    // public static final String
    public static final String TABLE_NAME = "step_meta";

    public static final double KM_PER_HOUR_CNT = 3.597;
    public static final int TO_KILOMETRE = 1000;
    public static final double K=1.036;
    private static final String COST_TIME_FORMAT = "%02d:%02d:%02d";

    protected Archiver archive;
    private SQLiteDatabase database;
    private static final int FUNC_AVG = 0x1;
    private static final int FUNC_MAX = 0x2;
    private static final int FUNC_MIN = 0x3;
    private String s;
    public ArchiveMeta(Archiver archive) {
        this.archive = archive;
        this.database = archive.database;
    }
    public Archiver getArchive(){return archive;
    }
    protected boolean set(String name, String value) {

        ContentValues values = new ContentValues();
        values.put(Archiver.DATABASE_COLUMN.META_NAME, name);
        values.put(Archiver.DATABASE_COLUMN.META_VALUE, value);

        long result = 0;
        try {
            if (isExists(name)) {
                result = database.update(TABLE_NAME, values,
                        Archiver.DATABASE_COLUMN.META_NAME + "='" + name + "'", null);
            } else {
                result = database.insert(TABLE_NAME, null, values);
            }
        } catch (Exception e) {
            Helper.Logger.e(e.getMessage());
        }

        // 自动返回最后更新的数据更新时间
//        if (result > 0) {
//            File file = new File(archive.getName());
//            file.setLastModified(getEndTime().getTime());
//        }

        return result > 0 ? true : false;
    }

    protected String get(String name) {
        Cursor cursor;
        String result = "";
        try {
            String sql = "SELECT " + Archiver.DATABASE_COLUMN.META_VALUE
                    + " FROM " + TABLE_NAME
                    + " WHERE " + Archiver.DATABASE_COLUMN.META_NAME + "='" + name + "'"
                    ;

            cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();

            result = cursor.getString(cursor.getColumnIndex(Archiver.DATABASE_COLUMN.META_VALUE));
            cursor.close();
        } catch (SQLiteException e) {
            Helper.Logger.e(e.getMessage());
        } catch (CursorIndexOutOfBoundsException e) {
            Helper.Logger.e(e.getMessage());
        } catch (IllegalStateException e) {
            Helper.Logger.e(e.getMessage());
        }
            s = result;
            return result;



    }

    protected String get(String name, String defaultValue) {
        String value = get(name);
        if (value.equals("") && defaultValue.length() > 0) {
            return defaultValue;
        }

        return value;
    }

    protected boolean isExists(String name) {
        Cursor cursor;
        int count = 0;
        try {
            cursor = database.rawQuery(
                    "SELECT count(id) AS count"
                            + " FROM " + TABLE_NAME
                            + " WHERE " + Archiver.DATABASE_COLUMN.META_NAME + "='" + name + "'", null);
            cursor.moveToFirst();

            count = cursor.getInt(cursor.getColumnIndex(Archiver.DATABASE_COLUMN.COUNT));
            cursor.close();
        } catch (Exception e) {
            Helper.Logger.e(e.getMessage());
        }

        return count > 0 ? true : false;
    }

    public Date getStartTime() {

        try {
            long startTime = Long.parseLong(get(START_TIME));
            return new Date(startTime);
        } catch (Exception e) {
            return null;
        }
    }

    public Date getEndTime() {
        try {
            long endTime = Long.parseLong(get(END_TIME)) - Tracker.tm;
            return new Date(endTime);
        } catch (Exception e) {
            return null;
        }
    }

    public String getRawCostTimeString() {
        return getBetweenTimeString(getStartTime(), getEndTime());
    }

    public String getCostTimeStringByNow() {
        return getBetweenTimeString(getStartTime(), new Date(System.currentTimeMillis()));
    }
    public long getCostTimeLongByNow() {
        return getBetweenTimeLong(getStartTime(), new Date(System.currentTimeMillis()));
    }
    public long getBetweenTimeLong(Date start, Date end) {
        try {
            long startTimeStamp = start.getTime();
            long endTimeStamp = end.getTime();
            long between = endTimeStamp - startTimeStamp - Tracker.tm;


            return between;
        } catch (NullPointerException e) {
            return 0;
        }
    }
    public String getBetweenTimeString(Date start, Date end) {
        try {
            long startTimeStamp = start.getTime();
            long endTimeStamp = end.getTime();
            long between = endTimeStamp - startTimeStamp - Tracker.tm;
            long day = between / (24 * 60 * 60 * 1000);
            long hour = (between / (60 * 60 * 1000) - day * 24);
            long minute = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long second = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);

            return String.format(COST_TIME_FORMAT, hour, minute, second);
        } catch (NullPointerException e) {
            return "";
        }
    }

    public boolean setStartTime(Date date) {
        long time = date.getTime();
        return set(START_TIME, String.valueOf(time));
    }

    public boolean setEndTime(Date date) {
        long time = date.getTime();
        return set(END_TIME, String.valueOf(time));
    }

    public String getDescription() {
        return get(DESCRIPTION);
    }

    public boolean setDescription(String description) {
        boolean result = set(DESCRIPTION, description);
        return result;
    }

    public long getCount() {
        Cursor cursor;
        long count = 0;
        try {
            cursor = database.rawQuery(
                    "SELECT count(id) AS count FROM "
                            + Archiver.TABLE_NAME
                            + " LIMIT 1", null);
            cursor.moveToFirst();

            count = cursor.getLong(cursor.getColumnIndex(Archiver.DATABASE_COLUMN.COUNT));
            cursor.close();
        } catch (Exception e) {
            Helper.Logger.e(e.getMessage());
        }

        return count;
    }
    public String getClimbingValue(){
        String value=get(CLIBING,String.valueOf(0));
        //return String.valueOf(getClimbing());
        return value.equals("0")?String.valueOf(getClimbing()):value;
        // return get(CLIBING,String.valueOf(0)).equals("0")?getClimbing():get(CLIBING,String.valueOf(0));
    }
    /**
     * 卡路里数据
     */
    public String getCalorie(){
        return get(CALORIE,String.valueOf(0));
    }
    public boolean setCalorie(String data){
        return set(CALORIE,data);
    }
    /**
     *步数
     */
    public String getTotalStep(){
        return get(STEP,String.valueOf(0));
    }
    public String getStep(){
        ArrayList<LocationModel> locations = archive.fetchStepAll();
        int climbing = 0;
        for (int i = 0; i < locations.size(); i++) {
            LocationModel location = locations.get(i);
            climbing+= location.getStep();
        }
        return String.valueOf(climbing);
        // return null;
    }
    public boolean setStep(int step){
        return set(STEP,String.valueOf(step));
    }
    /**
     * 当前速度
     */
    public boolean setSpeedByNow(String data){
        return set("SPEED",data);
    }
    public String getSpeedByNow(){
        return get("SPEED");
    }
    /**
     * 设置身高
     */
//    public String getHeight(){
//        return get(HEIGHT);
//    }
//    public boolean setHeight(String data){
//        return set(HEIGHT,data);
//    }
    /**
     * 设置体重
     */
//    public String getWeight(){
//        return get(WEIGHT);
//    }
//    public boolean setWeight(String data){
//        return set(WEIGHT,data);
//    }
    /**
     * 获取当前爬坡值
     */
    public float getClimbing(){
        ArrayList<AMapLocation> locations = archive.fetchAll();
        AMapLocation lastComputedLocation = null;
        float climbing = 0;
        for (int i = 0; i < locations.size(); i++) {
            AMapLocation location = locations.get(i);
            if (lastComputedLocation != null) {
                if (lastComputedLocation.getAltitude() < location.getAltitude()&&Math.abs(location.getAltitude()-lastComputedLocation.getAltitude())<0.5){
                    climbing= (float) (climbing+(location.getAltitude()-lastComputedLocation.getAltitude()));

                }else{
                    lastComputedLocation = location;
                }
            }else{
                lastComputedLocation = location;
            }

        }
        return climbing;

    }
    public boolean setClimbing() {
        float climbing = getClimbing();
        return set(CLIBING, String.valueOf(climbing));
    }


    /**
     * 获得当前已经记录的距离
     *
     * @return
     */
    public float getRawDistance() {
        ArrayList<AMapLocation> locations = archive.fetchAll();
        AMapLocation lastComputedLocation = null;
        float distance = 0;
        for (int i = 0; i < locations.size(); i++) {
            AMapLocation location = locations.get(i);
            if (lastComputedLocation != null) {
                LatLng startLatlng = new LatLng(lastComputedLocation.getLatitude(), lastComputedLocation.getLongitude());
                LatLng endLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                // 计算量坐标点距离
                distance += AMapUtils.calculateLineDistance(startLatlng, endLatlng);
                //  distance += lastComputedLocation.distanceTo(location);
            }

            lastComputedLocation = location;
        }

        return distance;
    }
    public boolean setRawDistance(float distance) {
        return set(DISTANCE, String.valueOf(distance));
    }
    public boolean setRawDistance() {
        float distance = getRawDistance();

        return set(DISTANCE, String.valueOf(distance));
    }

    public float getDistance() {
        return Float.parseFloat(get(DISTANCE, "0.0"));
    }

    private float getSpeed(int type) {
        String func;
        switch (type) {
            case FUNC_MIN:
                func="min";
                break;
            case FUNC_AVG:
                func = "avg";
                break;
            case FUNC_MAX:
            default:
                func = "max";
                break;
        }

        String sql = "SELECT " + func + "(" + Archiver.DATABASE_COLUMN.SPEED
                + ") AS " + Archiver.DATABASE_COLUMN.SPEED
                + " FROM " + Archiver.TABLE_NAME
                +" WHERE "
                + Archiver.DATABASE_COLUMN.LATITUDE+" > 0.0"
                +" AND "+Archiver.DATABASE_COLUMN.ALTITUDE+" > 0.0 "
                +" AND "+Archiver.DATABASE_COLUMN.SPEED+" > 0.0"+ " LIMIT 1";

        Cursor cursor;
        float speed = 0;
        try {
            cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();
            speed = cursor.getFloat(cursor.getColumnIndex(Archiver.DATABASE_COLUMN.SPEED));
            cursor.close();
        } catch (Exception e) {
            Helper.Logger.e(e.getMessage());
        }

        return speed;
    }

    public float getAverageSpeed() {
        return getSpeed(FUNC_AVG);
    }

    public float getMaxSpeed() {
        return getSpeed(FUNC_MAX);
    }
    public float getMinSpeed() {
        return getSpeed(FUNC_MIN);
    }

    public boolean rebuild() {
        try {
            database.execSQL("DROP TABLE " + TABLE_NAME);
            database.execSQL(Archiver.ArchiveDatabaseHelper.SQL_CREATE_META_TABLE);

            setRawDistance();
            setStartTime(new Date(archive.getFirstRecord().getTime()));
            setEndTime(new Date(archive.getLastRecord().getTime()));
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public String getName() {
        return archive.getName();
    }
    /**
     * 获得的当前消耗的卡路里
     */
    // public

}
