package com.gracecode.tracker.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.location.AMapLocation;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.ui.view.MyMarkerView;
import com.gracecode.tracker.util.DateAxisLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.filter.Approximator;
import com.github.mikephil.charting.data.filter.Approximator.ApproximatorType;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * <p/>
 * User: mingcheng
 * Date: 12-9-20
 */
@SuppressLint("ValidFragment")
public class SpeedNewChartsFragment extends Fragment {
    private Context context;
    private Archiver archiver;
    private ArrayList<AMapLocation> locations;
    private LineChart mChart;
    private static int HORIZONTAL_LABELS_ITEM_SIZE = 9;
    private LineGraphSeries<DataPoint> speedSeries;
    private SimpleDateFormat dateFormatter;


    public SpeedNewChartsFragment(Context context, Archiver archiver) {
        this.context = context;
        this.archiver = archiver;

        this.locations = archiver.fetchAll();
        speedSeries = new LineGraphSeries<DataPoint>();
        dateFormatter = new SimpleDateFormat(context.getString(R.string.sort_time_format), Locale.getDefault());

       // graphView = GraphView(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.speedchart_new_fragment, null, false);
        mChart = (LineChart) view.findViewById(R.id.chart);
        setGraphDataAndStyle();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private void setGraphDataAndStyle() {
        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);
        mChart.setMarkerView(mv);
        mChart.setDescription("");
// x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        XAxis xAxis = mChart.getXAxis();
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.setStartAtZero(false);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        mChart.getAxisRight().setEnabled(false);
        setData();
        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
    }
    private void setData(int count, float range) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add((i) + "");
        }

        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {

            float mult = (range + 1);
            float val = (float) (Math.random() * mult) + 3;// + (float)
// ((mult *
// 0.1) / 10);
            yVals.add(new Entry(val, i));
        }

// create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "速度");
        set1.setDrawCircles(false);
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.6f);
        set1.setDrawFilled(true);
        set1.setFillColor(Color.RED);
// set1.setAxisDependency(YAxis.AxisDependency.LEFT);
// set1.setFillAlpha(110);

// set the line to be drawn like this "- - - - - -"
// set1.enableDashedLine(10f, 5f, 0f);
// set1.enableDashedHighlightLine(10f, 5f, 0f);
// set1.setColor(Color.BLUE);
// set1.setCircleColor(Color.GRAY);
// set1.setLineWidth(1f);
// set1.setCircleRadius(1f);
// set1.setDrawCircleHole(false);
// set1.setValueTextSize(9f);
// Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.fade_red);
// set1.setFillDrawable(drawable);
// set1.setDrawFilled(true);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

// create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

// set data
        mChart.setData(data);
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

// create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "速度");
        set1.setDrawCircles(false);
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        set1.setDrawFilled(true);
        set1.setFillColor(Color.RED);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

// create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

// set data
        mChart.setData(data);
    }
    private String[] getHorizontalLabels() {
        int size = locations.size();
        ArrayList<String> labels = new ArrayList<String>();

        for (int i = 0; i < size; i += (size / HORIZONTAL_LABELS_ITEM_SIZE)) {
            Location location = locations.get(i);
            if (location != null) {
                labels.add(dateFormatter.format(location.getTime()));
            }
        }

        return labels.toArray(new String[labels.size()]);
    }


    private DataPoint[] getSeriesData() {
       // speedSeries..clear();
      List<DataPoint>  a=new ArrayList<>();
        Iterator<AMapLocation> locationIterator = locations.iterator();
        while (locationIterator.hasNext()) {
            AMapLocation location = locationIterator.next();
            DataPoint graphViewData = new DataPoint(location.getTime(),
                location.getSpeed() * ArchiveMeta.KM_PER_HOUR_CNT);
           a.add(graphViewData);
           // speedSeries.add(graphViewData);
        }

        return  (DataPoint[])a.toArray(new DataPoint[a.size()]);
      //  return speedSeries.toArray(new GraphView.GraphViewData[speedSeries.size()]);
    }
}
