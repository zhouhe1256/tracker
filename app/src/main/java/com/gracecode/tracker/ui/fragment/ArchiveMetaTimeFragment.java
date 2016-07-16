package com.gracecode.tracker.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressLint("ValidFragment")
public class ArchiveMetaTimeFragment extends Fragment {
    private ArchiveMeta meta;
    private View metaLayout;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat hourFormat;
    private TextView mStartTime;
    private TextView mEndTime;
    private TextView mTotalTime;
    private Context context;

    public ArchiveMetaTimeFragment(Context context, ArchiveMeta meta) {
        this.meta = meta;
        this.context = context;
        this.dateFormat = new SimpleDateFormat(context.getString(R.string.time_format), Locale.CHINA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        metaLayout = inflater.inflate(R.layout.archive_meta_time, container, false);
        mStartTime = (TextView) metaLayout.findViewById(R.id.meta_start_time);
        mEndTime = (TextView) metaLayout.findViewById(R.id.meta_end_time);
        mTotalTime = (TextView) metaLayout.findViewById(R.id.meta_total_time);
        return metaLayout;
    }

    public void onStart() {
        super.onStart();
        updateView();
    }

    protected void updateView() {
        Date startTime = meta.getStartTime();
        Date endTime = meta.getEndTime();
        Date totalTime=new Date(endTime.getTime()-startTime.getTime());

        mStartTime.setText(
            startTime != null ?
                dateFormat.format(startTime) : getString(R.string.not_available));

        mEndTime.setText(
            endTime != null ?
                dateFormat.format(endTime) : getString(R.string.not_available));
        mTotalTime.setText(
                totalTime != null ?
                        getBetweenTimeString(startTime,endTime) : getString(R.string.not_available));

    }
    private static final String COST_TIME_FORMAT = "%02d:%02d:%02d";
    private String getBetweenTimeString(Date start, Date end) {
        try {
            long startTimeStamp = start.getTime();
            long endTimeStamp = end.getTime();
            long between = endTimeStamp - startTimeStamp;

            long day = between / (24 * 60 * 60 * 1000);
            long hour = (between / (60 * 60 * 1000) - day * 24);
            long minute = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long second = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);

            return String.format(COST_TIME_FORMAT, hour, minute, second);
        } catch (NullPointerException e) {
            return "";
        }
    }
}
