package com.gracecode.tracker.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.gracecode.tracker.R;

/**
 * Created by zhouh on 16-2-1.
 */
public class Myff extends View implements Runnable{
    /*声明bitmap*/
    private Bitmap bitmap = null;
    private int bitmapWidth;
    private int bitmapHeight;
    private Paint mPaint = null;
    /*Bitmap渲染*/
    private Shader mBitmapShader = null;
    /*线性渐变渲染*/
    private Shader mLinearGradient = null;
    /*环形渐变渲染*/
    private Shader mRaialGradient = null;
    /*混合渲染*/
    private Shader mCompostGradient = null;
    /*梯度渲染*/
    private Shader mSweepGradient = null;
    private ShapeDrawable mShapeDrawable = null;
    public Myff(Context context) {
        super(context);
        init();
    }

    public Myff(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Myff(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Myff(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abc_btn_check_to_on_mtrl_000);
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
        mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.MIRROR);
        mLinearGradient = new LinearGradient(0,0,100,100,
                new int[]{Color.RED,Color.GREEN,Color.BLUE,Color.WHITE},
                null,
                Shader.TileMode.REPEAT);
        mCompostGradient = new ComposeShader(mBitmapShader, mLinearGradient,
                PorterDuff.Mode.DARKEN);
        mRaialGradient = new RadialGradient(50,200,50,
                new int[]{Color.GREEN,Color.RED,Color.BLUE,Color.WHITE},
                null,
                Shader.TileMode.REPEAT);
        mSweepGradient = new SweepGradient(30,30,
                new int[]{Color.GREEN,Color.RED,Color.BLUE,Color.WHITE},null);
        mPaint = new Paint();
        new Thread(this).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //将图片裁剪为椭圆形
        /*构建ShapeDrawable对象并定义形状为椭圆*/
        mShapeDrawable = new ShapeDrawable(new OvalShape());
        /*设置要绘制的椭圆形的东西为ShapeDrawable*/
        mShapeDrawable.getPaint().setShader(mBitmapShader);
        /*设置显示区域*/
        mShapeDrawable.setBounds(0,0,bitmapWidth,bitmapHeight);
        /*绘制shapeDrawable*/
        mShapeDrawable.draw(canvas);
        //绘制渐变的矩形
        mPaint.setShader(mLinearGradient);
        canvas.drawRect(bitmapWidth,0,320,156,mPaint);
        //绘制混合渲染效果
        mPaint.setShader(mCompostGradient);
        canvas.drawRect(0,300,bitmapWidth,300+bitmapHeight,mPaint);
        //绘制环形渐变
        mPaint.setShader(mRaialGradient);
        canvas.drawCircle(50,200,50,mPaint);
        //绘制梯度渐变
        mPaint.setShader(mSweepGradient);
        canvas.drawRect(150,160,300,300,mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return true;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            postInvalidate();
        }
    }
}
