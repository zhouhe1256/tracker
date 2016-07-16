package com.gracecode.tracker.util;

import android.content.Context;

import com.gracecode.tracker.R;
import com.jjoe64.graphview.DefaultLabelFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by dengt on 16-1-22.
 */
public class DateAxisLabelFormatter extends DefaultLabelFormatter {
    protected final SimpleDateFormat mDateFormat;
    protected final Calendar mCalendar;

    public DateAxisLabelFormatter(Context context) {
        this.mDateFormat = new SimpleDateFormat(context.getString(R.string.sort_time_format), Locale.CHINA);
        this.mCalendar = Calendar.getInstance();
    }

    public DateAxisLabelFormatter(Context context, SimpleDateFormat dateFormat) {
        this.mDateFormat = dateFormat;
        this.mCalendar = Calendar.getInstance();
    }

    public String formatLabel(double value, boolean isValueX) {
        if(isValueX) {
            this.mCalendar.setTimeInMillis((long)value);
            return this.mDateFormat.format(Long.valueOf(this.mCalendar.getTimeInMillis()));
        } else {
            return super.formatLabel(value, isValueX);
        }
    }}