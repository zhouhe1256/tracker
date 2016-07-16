package com.gracecode.tracker.util;

import java.util.Date;

/**
 * Created by bjcathay on 16-2-24.
 */
public class TimeUtils {
    public static long getTime(Date start,Date end){
        long startTimeStamp = start.getTime();
        long endTimeStamp = end.getTime();
        long between = endTimeStamp - startTimeStamp;
        return between;
    }
    public static String getPace(long between){

        long day = between / (24 * 60 * 60 * 1000);
        long hour = (between / (60 * 60 * 1000) - day * 24);
        long minute = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long second = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
        return minute+"'"+second+"\"";
    }
}
