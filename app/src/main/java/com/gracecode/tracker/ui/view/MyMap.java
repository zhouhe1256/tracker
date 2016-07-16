
package com.gracecode.tracker.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.gracecode.tracker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouh on 16-2-1.
 */
public class MyMap extends View {
    public static List<Point> list = new ArrayList<Point>();
    protected ArrayList<AMapLocation> locations;
    protected ArrayList<Integer> stops;
    private Paint linePaint = null;
    private Bitmap bitmap = null;
    public MyMap(Context context) {
        super(context);

        linePaint = new Paint();

    }

    public void setDate(Context context,
                        ArrayList<AMapLocation> locations,
                        List<Point> list,
                        Handler handler,
                        ArrayList<Integer> stops) {
        this.stops = stops;
        this.locations = locations;
        this.list = list;
        linePaint = new Paint();
        invalidate();
        Message msg = handler.obtainMessage();
        msg.what = 1;
        handler.sendMessage(msg);
    }

    public MyMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        linePaint = new Paint();
    }

    public MyMap(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        linePaint = new Paint();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyMap(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        linePaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackgroundColor(Color.TRANSPARENT);
        linePaint.setStrokeWidth(dp2px(2));
        linePaint.setAntiAlias(true);
        LatLng startLatlng  = null;

        LatLng endLatlng = null;
        float dis = 0f;
        int count = 1;
        boolean st = true;
        if (locations != null && locations.size() >= 2) {
            double cha = 8.0 - 0.1;
            for (int i = 0; i < locations.size() - 1; i++) {
                float x = list.get(i).x;
                float y = list.get(i).y;
                float x1 = list.get(i + 1).x;
                float y1 = list.get(i + 1).y;
                int colors[] = new int[2];
                float positions[] = new float[2];
                int red = (int) (locations.get(i).getSpeed() * 255 / cha);
                int red1 = (int) (locations.get(i + 1).getSpeed() * 255 / cha);
                int green = 255 - red;
                int green1 = 255 - red1;
                colors[0] = Color.rgb(red, green, 00);
                positions[0] = 0;
                colors[1] = Color.rgb(red, green1, 00);
                positions[1] = 0.5f;
                LinearGradient shader = new LinearGradient(
                        x, y,
                        x1, y1,
                        colors,
                        positions,
                        Shader.TileMode.CLAMP);
                linePaint.setShader(shader);
                if(stops.get(i)==0){

                }else {
                    canvas.drawLine(x, y, x1, y1, linePaint);

                startLatlng = new LatLng(locations.get(i).getLatitude(),
                        locations.get(i).getLongitude());
                endLatlng = new LatLng(locations.get(i+1).getLatitude(),
                        locations.get(i+1).getLongitude());
                dis =dis + AMapUtils.calculateLineDistance(startLatlng, endLatlng);
                Log.i("dis", dis + "ff");
                if(dis>=1000f){
                    bitmap =
                            BitmapFactory.decodeResource
                                    (getResources(), R.drawable.to);
                   // canvas.drawBitmap(bitmap,x,y,linePaint);
                    linePaint.setShader(null);
                    linePaint.setColor(Color.BLUE);
                    linePaint.setTextSize(30);
                    canvas.drawText(count+"",x,y,linePaint);
                    dis = 0f;
                    count++;
                }
                }
            }
            linePaint.setShader(null);
            float xs = list.get(0).x-19f;
            float ys = list.get(0).y-30f;
            Bitmap bitmap =
                    BitmapFactory.decodeResource
                            (getResources(), R.drawable.point_start);
            canvas.drawBitmap(bitmap,xs,ys,linePaint);
            bitmap =
                    BitmapFactory.decodeResource
                            (getResources(), R.drawable.point_end);
            xs = list.get(list.size()-1).x-19f;
            ys = list.get(list.size()-1).y-30f;
            canvas.drawBitmap(bitmap, xs, ys, linePaint);
        }

    }

    private float dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (float) (dp * scale + 0.5f);
    }





}
