
package com.gracecode.tracker.ui.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;


import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.dao.LocationModel;
import com.gracecode.tracker.ui.activity.base.Activity;
import com.gracecode.tracker.ui.fragment.SpeedNewChartsFragment;
import com.gracecode.tracker.ui.view.MyMarkerView;
import com.markupartist.android.widget.ActionBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChartsTotal extends Activity {
    private String archiveFileName;
    private Archiver archiver;
    private String description;
    private LinearLayout chartsView;
    private LinearLayout chartsView_;
    private ArchiveMeta archiveMeta;
    private LineChart mChart;
    private LineChart mChart_;
    private LineChart bupin;
    private LineChart bufu;
    private long time;

    private ArrayList<LocationModel> locations;
    // private ArrayList<LocationModel> locationModels;
    // private SpeedNewChartsFragment speedChartsFragment;
    private SimpleDateFormat dateFormatter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chartstotal);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archiver = new Archiver(context, archiveFileName, Archiver.MODE_READ_ONLY);
        archiveMeta = archiver.getMeta();
        time =archiveMeta.getEndTime().getTime()-archiveMeta.getStartTime().getTime();
        this.locations = archiver.fetchStepAll();
        // this.locationModels=archiver.fetchStepAll();
        dateFormatter = new SimpleDateFormat(context.getString(R.string.sort_time_format),
                Locale.getDefault());
        description = archiver.getMeta().getDescription();
        if (description.length() <= 0) {
            description = getString(R.string.no_description);
        }

        // speedChartsFragment = new SpeedNewChartsFragment(context, archiver);
        mChart = (LineChart) findViewById(R.id.chart);
        mChart_ = (LineChart) findViewById(R.id.chart_);
        bupin = (LineChart) findViewById(R.id.bupin);
        bufu = (LineChart) findViewById(R.id.bufu);
        setbufuDataAndStyle();
        setbupinDataAndStyle();
        setGraphDataAndStyle();

        setGraphDataAndStyle_();

    }

    private void setbupinDataAndStyle() {
        bupin.setBackgroundResource(R.drawable.fade_red);
        bupin.setGridBackgroundColor(Color.TRANSPARENT);
        bupin.setDrawGridBackground(false);
        bupin.setBorderColor(Color.WHITE);
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        bupin.setMarkerView(mv);
        bupin.setDescription("");
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        XAxis xAxis = bupin.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGridColor(Color.WHITE);

        // xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "OpenSans-Regular.ttf");
        YAxis leftAxis = bupin.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setStartAtZero(false);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setGridColor(Color.WHITE);
        bupin.getAxisRight().setEnabled(false);
        setbupinData();
        bupin.animateX(2500, Easing.EasingOption.EaseInOutQuart);
        Legend l = bupin.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);
    }

    private void setbupinData() {
        int count = locations.size();
        ArrayList<String> xVals = new ArrayList<String>();
//        for (int i = 0; i < count; i++) {
//            xVals.add(dateFormatter.format(Long.valueOf(locations.get(i).getTime())));
//        }
        // long time=(locations.get(count).getTime()-locations.get(0).getTime())/1000;
        int num=(int)(time/600/1000)+1;
        for(int i = 0; i <=num; i++){
            xVals.add(String.valueOf(i*10));
        }

        ArrayList<Entry> yVals = new ArrayList<Entry>();

//        for (int i = 0; i < count; i++) {
//            yVals.add(new Entry(locations.get(i).getStep(), i));
//        }
        int flag=0;
        for (int i = 0; i <= num; i++) {
            long t1=locations.get(0).getTime();
            long stepnum=0;
            if(i==0){
                yVals.add(new Entry(0, 0));
            }
            for (int j=flag;j<count;j++){
                long t=locations.get(j).getTime();
                if(t<=t1+10*60*1000*(i+1)){
                    stepnum=stepnum+locations.get(j).getStep();
                    if(i==num&&j==count-1){
                        yVals.add(new Entry(stepnum, i+1));
                        break;
                    }

                }else{
                    yVals.add(new Entry(stepnum, i+1));
                    break;
                }
                flag=j;
            }

        }
        LineDataSet set1 = new LineDataSet(yVals, "步频");
        set1.setDrawCircles(false);
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        // set1.setFillFormatter();
        set1.setDrawFilled(true);
        set1.setColor(Color.WHITE);
        set1.setValueTextColor(Color.WHITE);
        set1.setFillColor(Color.WHITE);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets
        LineData data = new LineData(xVals, dataSets);
        bupin.setData(data);
    }

    private void setbufuDataAndStyle() {
        bufu.setBackgroundResource(R.drawable.fade_green);
        bufu.setGridBackgroundColor(Color.TRANSPARENT);
        bufu.setDrawGridBackground(false);
        bufu.setBorderColor(Color.WHITE);
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        bufu.setMarkerView(mv);
        bufu.setDescription("");
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        XAxis xAxis = bufu.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGridColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "OpenSans-Regular.ttf");
        YAxis leftAxis = bufu.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setStartAtZero(false);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setGridColor(Color.WHITE);
        bufu.getAxisRight().setEnabled(false);
        setbufuData();
        bufu.animateX(2500, Easing.EasingOption.EaseInOutQuart);
        Legend l = bupin.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);
    }

    private void setbufuData() {
        int count = locations.size();
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add(dateFormatter.format(Long.valueOf(locations.get(i).getTime())));
        }
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            if (i == 0) {
                yVals.add(new Entry(locations.get(i).getStep(), i));
            } else {
                long bwn = locations.get(i).getTime() - locations.get(i - 1).getTime();
                int step = locations.get(i).getStep();
                float b;
                if (step == 0) {
                    b = 0;
                } else
                {
                    b = locations.get(i).getSpeed() * (bwn / 1000) / locations.get(i).getStep();
                }
                yVals.add(new Entry(b, i));
            }
        }
        LineDataSet set1 = new LineDataSet(yVals, "步幅");
        set1.setDrawCircles(false);
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        set1.setDrawFilled(true);
        set1.setColor(Color.WHITE);
        set1.setValueTextColor(Color.WHITE);
        set1.setFillColor(Color.WHITE);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets
        LineData data = new LineData(xVals, dataSets);
        bufu.setData(data);
    }

    private void setGraphDataAndStyle() {
        mChart.setBackgroundResource(R.drawable.fade_zise);
        mChart.setGridBackgroundColor(Color.TRANSPARENT);
        mChart.setDrawGridBackground(false);
        mChart.setBorderColor(Color.WHITE);
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mChart.setMarkerView(mv);
        mChart.setDescription("");
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGridColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "OpenSans-Regular.ttf");
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setStartAtZero(false);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setGridColor(Color.WHITE);
        mChart.getAxisRight().setEnabled(false);
        setData();
        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);
    }

    private void setData() {
        int count = locations.size();
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add(dateFormatter.format(Long.valueOf(locations.get(i).getTime())));
        }
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            yVals.add(new Entry(locations.get(i).getSpeed(), i));
        }
        LineDataSet set1 = new LineDataSet(yVals, "速度");
        set1.setDrawCircles(false);
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        set1.setDrawFilled(true);
        set1.setColor(Color.WHITE);
        set1.setValueTextColor(Color.WHITE);
        set1.setFillColor(Color.WHITE);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets
        LineData data = new LineData(xVals, dataSets);
        mChart.setData(data);
    }

    private void setGraphDataAndStyle_() {
        mChart_.setBackgroundResource(R.drawable.fade_zise);
        mChart_.setGridBackgroundColor(Color.TRANSPARENT);
        mChart_.setDrawGridBackground(false);
        mChart_.setBorderColor(Color.WHITE);
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mChart_.setMarkerView(mv);
        mChart_.setDescription("");
        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        XAxis xAxis = mChart_.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGridColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "OpenSans-Regular.ttf");
        YAxis leftAxis = mChart_.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid
        // overlapping lines
        leftAxis.setStartAtZero(false);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setGridColor(Color.WHITE);
        mChart_.getAxisRight().setEnabled(false);
        setData_();
        mChart_.animateX(2500, Easing.EasingOption.EaseInOutQuart);
        Legend l = mChart_.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

    }

    private void setData_() {
        int count = locations.size();
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add(dateFormatter.format(Long.valueOf(locations.get(i).getTime())));
        }
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            yVals.add(new Entry((float) locations.get(i).getAltitude(), i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "海拔");
        set1.setDrawCircles(false);
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        set1.setDrawFilled(true);
        set1.setColor(Color.WHITE);
        set1.setValueTextColor(Color.WHITE);
        set1.setFillColor(Color.WHITE);
        // Drawable drawable = ContextCompat.getDrawable(getActivity(),
        // R.drawable.fade_red);
        // set1.setFillDrawable(drawable);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        mChart_.setData(data);
    }

    @Override
    public void onStart() {
        super.onStart();
        actionBar.setTitle("配速表");

        actionBar.removeAllActions();
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_share;
            }

            @Override
            public void performAction(View view) {
                shareToSina();
            }
        });

        // addFragment(R.id.charts, speedChartsFragment);
    }

    public void shareToSina() {
        byte[] bitmap = helper.convertBitmapToByteArray(getChartsBitmap());

        String recordsFormatter = getString(R.string.records_formatter);
        SimpleDateFormat dateFormatter = new SimpleDateFormat(getString(R.string.time_format),
                Locale.getDefault());

        // Build string for share by microblog etc.
        String message = String.format(
                getString(R.string.share_report_formatter),
                archiveMeta.getDescription().length() > 0 ? "(" + archiveMeta.getDescription()
                        + ")" : "",
                String.format(recordsFormatter, archiveMeta.getDistance()
                        / ArchiveMeta.TO_KILOMETRE),
                dateFormatter.format(archiveMeta.getStartTime()),
                dateFormatter.format(archiveMeta.getEndTime()),
                archiveMeta.getRawCostTimeString(),
                String.format(recordsFormatter, archiveMeta.getMaxSpeed()
                        * ArchiveMeta.KM_PER_HOUR_CNT),
                String.format(recordsFormatter, archiveMeta.getAverageSpeed()
                        * ArchiveMeta.KM_PER_HOUR_CNT)
        );

        helper.shareToSina(context, message, bitmap);
    }

    private Bitmap getChartsBitmap() {
        chartsView.setDrawingCacheEnabled(true);
        chartsView.buildDrawingCache();
        chartsView.destroyDrawingCache();
        return Bitmap.createBitmap(chartsView.getDrawingCache());
    }

    public long getTime(Date start, Date end) {
        long startTimeStamp = start.getTime();
        long endTimeStamp = end.getTime();
        long between = endTimeStamp - startTimeStamp;
        return between;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        archiver.close();
    }
}
