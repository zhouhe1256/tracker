
package com.gracecode.tracker.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.gracecode.tracker.R;
import com.gracecode.tracker.TrackerApplication;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.model.UserModel;
import com.gracecode.tracker.service.Recorder;
import com.gracecode.tracker.ui.activity.base.Activity;
import com.gracecode.tracker.ui.fragment.ArchiveMetaFragment;
import com.gracecode.tracker.util.Helper;
import com.gracecode.tracker.util.PreferencesUtils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.markupartist.android.widget.ActionBar;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;
import com.umeng.update.UmengUpdateAgent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.listener.SaveListener;

public class Tracker extends Activity implements View.OnClickListener, View.OnLongClickListener {
    private Button mStartButton;
    private Button mEndButton;
    private TextView speedTextView;
    private TextView distanceTextView;
    private ArchiveMetaFragment archiveMetaFragment;
    private String formatter;
    protected ArchiveMeta archiveMeta;

    private static final int FLAG_RECORDING = 0x001;
    private static final int FLAG_ENDED = 0x002;
    private static final long MINI_RECORDS = 2;

    private boolean isRecording = false;
    public static Boolean pedometerstatus = false;
    public static final int MESSAGE_UPDATE_VIEW = 0x011;
    private Timer updateViewTimer;
    private static final long TIMER_PERIOD = 1000;
    private TextView mCoseTime;
    private Button mDisabledButton;
    private AMapLocationClient mLocationClient;
    public TrackerApplication tracker = TrackerApplication.getInstance();
    private UserModel userModel;
    public static long tm = 0;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private SimpleDateFormat dateFormat;
    private float calorie;
    private float totalDistance;
    private long totalDuration;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.tracker);
        formatter = getString(R.string.records_formatter);
        mStartButton = (Button) findViewById(R.id.btn_start);
        mEndButton = (Button) findViewById(R.id.btn_end);
        mDisabledButton = (Button) findViewById(R.id.btn_disabled);
        speedTextView = (TextView) findViewById(R.id.speed);
        mCoseTime = (TextView) findViewById(R.id.item_cost_time);
        distanceTextView = (TextView) findViewById(R.id.distance);
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
        mStartButton.setOnClickListener(this);
        mEndButton.setOnClickListener(this);
        mDisabledButton.setOnClickListener(this);
        mEndButton.setOnLongClickListener(this);
        mEngineType = SpeechConstant.TYPE_LOCAL;
        // UmengUpdateAgent.update(context);
        // UMFeedbackService.enableNewReplyNotification(context,
        // NotificationType.AlertDialog);

    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            // Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                // showTip("初始化失败,错误码："+code);
                Toast.makeText(Tracker.this, "初始化失败,错误码：" + code, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 参数设置
     * 
     * @param param
     * @return
     */
    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 设置合成
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            // 设置使用云端引擎
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        } else {
            // 设置使用本地引擎
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置发音人资源路径
            mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
            // 设置发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        }

        // 设置语速
        mTts.setParameter(SpeechConstant.SPEED, "50");

        // 设置音调
        mTts.setParameter(SpeechConstant.PITCH, "50");

        // 设置音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");

        // 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
    }

    // 获取发音人资源路径
    private String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        // 合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets,
                "tts/common.jet"));
        tempBuffer.append(";");
        // 发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets,
                "tts/xiaoyan.jet"));
        return tempBuffer.toString();
    }

    private void notifyUpdateView() {
        Message message = new Message();
        message.what = MESSAGE_UPDATE_VIEW;
        uiHandler.sendMessage(message);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (helper.isGPSProvided()) {
            updateViewTimer = new Timer();
            updateViewTimer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            notifyUpdateView();
                        }
                    }, 0, TIMER_PERIOD);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        if (!helper.isGPSProvided()) {
            mStartButton.setVisibility(View.GONE);
            mEndButton.setVisibility(View.GONE);
            // mDisabledButton.setVisibility(View.VISIBLE);

            helper.showLongToast("当前没有开启GPS定位");
        } else {
            mDisabledButton.setVisibility(View.GONE);
        }

        // 设置 ActionBar 样式
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.removeAllActions();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.clearHomeAction();
        actionBar.addAction(
                new ActionBar.Action() {
                    @Override
                    public int getDrawable() {
                        return R.drawable.ic_menu_friendslist;
                    }

                    @Override
                    public void performAction(View view) {
                        gotoActivity(Records.class);
                    }
                });

    }

    private void gotoActivity(Class cls) {
        Intent intent = new Intent(context, cls);
        startActivity(intent);
    }
    Date date_t = null;
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                if (serviceBinder != null && !isRecording) {
                    TrackerApplication.ff = true;
                    TrackerApplication.fff = true;
                    TrackerApplication.tixing = true;
                    tm = 0;
                    TrackerApplication.count_2 = 0;

                    setParam();
                    int code = mTts.startSpeaking("开始跑步", null);
                    if (code != ErrorCode.SUCCESS) {
                        // showTip("语音合成失败,错误码: " + code);
                        Toast.makeText(Tracker.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG)
                                .show();
                    }
                    serviceBinder.startRecord();

                    notifyUpdateView();
                }
                break;
            case R.id.btn_end:
               // helper.showShortToast(getString(R.string.long_press_to_stop));

                if(TrackerApplication.fff){
                    TrackerApplication.fff = false;
                    date_t = new Date(System.currentTimeMillis());
                    int code = mTts.startSpeaking("跑步已暂停", null);
                    if (code != ErrorCode.SUCCESS) {
                        // showTip("语音合成失败,错误码: " + code);
                        Toast.makeText(Tracker.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG)
                                .show();
                    }
                }else{
                    TrackerApplication.fff = true;
                    Date d = null;
                    d = new Date(System.currentTimeMillis());
                    long f1 = d.getTime();
                    long f2 = date_t.getTime();
                    tm = (f1 - f2) + tm;
                    setParam();
                    int code = mTts.startSpeaking("恢复跑步", null);
                    if (code != ErrorCode.SUCCESS) {
                        // showTip("语音合成失败,错误码: " + code);
                        Toast.makeText(Tracker.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG)
                                .show();
                    }
                }
                break;

            case R.id.btn_disabled:
                Intent intent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (isRecording && serviceBinder != null) {
            TrackerApplication.d1 = 0;
            TrackerApplication.TIME = 0;
            TrackerApplication.DISTANCE = 0;
            int code = mTts.startSpeaking("跑步结束", null);
            if (code != ErrorCode.SUCCESS) {
                // showTip("语音合成失败,错误码: " + code);
                Toast.makeText(Tracker.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG)
                        .show();
            }
            serviceBinder.stopRecord();
            this.dateFormat = new SimpleDateFormat(context.getString(R.string.time_format), Locale.CHINA);

            notifyUpdateView();
            if (archiveMeta != null) {
                long count = archiveMeta.getCount();
                if (count > MINI_RECORDS) {
                    TrackerApplication.getInstance().setDistance(0);
                    Intent intent = new Intent(context, Detail.class);
                    intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveMeta.getName());
                    startActivity(intent);

                }

            }
        }

        setViewStatus(FLAG_ENDED);
        return true;
    }

    private float ditance;
    private float ditance_t = 0f;
    private Date date;
    private boolean b = true;
    private boolean s = true;

    private void setViewStatus(int status) {
        // FragmentTransaction fragmentTransaction =
        // fragmentManager.beginTransaction();

        switch (status) {
            case FLAG_RECORDING:
                mStartButton.setVisibility(View.GONE);
                mEndButton.setVisibility(View.VISIBLE);
                if (archiveMeta != null&&TrackerApplication.fff) {
                    // archiveMetaFragment = new ArchiveMetaFragment(context,
                    // archiveMeta);
                    // fragmentTransaction.replace(R.id.status_layout,
                    // archiveMetaFragment);
                    // fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    TrackerApplication.count++;
                    if (TrackerApplication.count == 20) {
                        if (ditance != 0f
                                && ditance == tracker.getDistance() / ArchiveMeta.TO_KILOMETRE) {
                            TrackerApplication.ff = false;
                            if (b) {
                                date = new Date(System.currentTimeMillis());
                                int code = mTts.startSpeaking("跑步已暂停", null);
                                if (code != ErrorCode.SUCCESS) {
                                    // showTip("语音合成失败,错误码: " + code);
                                    Toast.makeText(Tracker.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                            b = false;
                            s = true;
                        } else {
                            if (s) {
                                if (ditance == 0f) {
                                    tm = 0;
                                } else {

                                    TrackerApplication.ff = true;
                                    Date d = null;
                                    d = new Date(System.currentTimeMillis());
                                    long f1 = d.getTime();
                                    long f2 = date.getTime();
                                    tm = (f1 - f2) + tm;
                                }
                                b = true;
                                s = false;
                            }

                        }
                        ditance = tracker.getDistance() / ArchiveMeta.TO_KILOMETRE;
                        TrackerApplication.count = 0;
                    }
                    if (tracker.getDistance() / ArchiveMeta.TO_KILOMETRE - ditance_t >= 1) {
                        TrackerApplication.count_2++;
                        ditance_t = tracker.getDistance() / ArchiveMeta.TO_KILOMETRE;
                        // 提示跑满一公里
                        // 设置参数
                        setParam();
                        int code = mTts.startSpeaking("你已经跑了" + TrackerApplication.count_2 + "公里",
                                null);
                        if (code != ErrorCode.SUCCESS) {
                            // showTip("语音合成失败,错误码: " + code);
                            Toast.makeText(Tracker.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                    if (tracker.getDistance()
                            / ArchiveMeta.TO_KILOMETRE >= TrackerApplication.DISTANCE
                            && TrackerApplication.DISTANCE != 0) {

                        // 提示跑满自己已经设定的距离
                        // 设置参数
                        setParam();
                        int code = mTts.startSpeaking(
                                "运动目标已完成", null);
                        if (code != ErrorCode.SUCCESS) {
                            // showTip("语音合成失败,错误码: " + code);
                            Toast.makeText(Tracker.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG)
                                    .show();
                        }
                        TrackerApplication.DISTANCE = 0;
                    }
                    String time = archiveMeta.getCostTimeStringByNow();
                    String[] times = time.split(":");
                    int tmh = Integer.parseInt(times[0]);
                    int tms = Integer.parseInt(times[1]);
                    int ttms = tmh * 60 + tms;
                    if (ttms >= TrackerApplication.TIME && TrackerApplication.TIME != 0) {
                        // 提示跑满自己已经设定的时间
                        int code = mTts.startSpeaking(
                                "设定的" + TrackerApplication.TIME + "分钟跑步时间已经达到", null);
                        if (code != ErrorCode.SUCCESS) {
                            // showTip("语音合成失败,错误码: " + code);
                            Toast.makeText(Tracker.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG)
                                    .show();
                        }
                        TrackerApplication.TIME = 0;
                    }
                    if (TrackerApplication.ff) {
                        mCoseTime.setText(archiveMeta.getCostTimeStringByNow());
                    }

                    // mCoseTime.setText(archiveMeta.getCostTimeStringByNow());
                    try {
                        if (TrackerApplication.ff) {
                        if (tracker.getPace() != null) {
                            if (Float.parseFloat(tracker.getPace()) > 10.43) {
                                if(TrackerApplication.tixing){
                                setParam();
                                int code = mTts.startSpeaking(
                                        "当前数据异常，非跑步状态", null);
                                if (code != ErrorCode.SUCCESS) {
                                    // showTip("语音合成失败,错误码: " + code);
                                    Toast.makeText(Tracker.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG)
                                            .show();
                                }
                                Toast.makeText(context, "当前数据异常，非跑步状态", Toast.LENGTH_LONG).show();
                                TrackerApplication.tixing = false;
                            }
                            }
                            if (tracker.getDistance() != 0) {
                                long bwn = new Date().getTime()
                                        - archiveMeta.getStartTime().getTime();
                                speedTextView.setText(String.format(getPace((long) ((bwn)
                                        / (tracker.getDistance() / ArchiveMeta.TO_KILOMETRE)))));
                            } else {
                                /*speedTextView
                                        .setText(
                                                getPace((long) (3600
                                                        / (Float.valueOf(tracker.getPace())
                                                        * ArchiveMeta.KM_PER_HOUR_CNT)
                                                        * 1000)));*/
                                speedTextView.setText("- -");
                            }

                        } else {
                            speedTextView.setText("0'0\"");
                        }}
                    } catch (Exception e) {
                        Helper.Logger.e(e.getMessage());
                    }

                    // distanceTextView.setText(String.format(formatter,
                    // archiveMeta.getDistance() / ArchiveMeta.TO_KILOMETRE));
                    distanceTextView.setText(String.format(formatter,
                            tracker.getDistance() / ArchiveMeta.TO_KILOMETRE));
                    totalDistance = tracker.getDistance() / ArchiveMeta.TO_KILOMETRE;
                    totalDuration = archiveMeta.getCostTimeLongByNow();
                }
                break;
            case FLAG_ENDED:
                mStartButton.setVisibility(View.VISIBLE);
                mEndButton.setVisibility(View.GONE);
                // if (archiveMetaFragment != null) {
                // fragmentTransaction.remove(archiveMetaFragment);
                // }
                mCoseTime.setText(R.string.none_cost_time);
                speedTextView.setText("0'0\"");
                distanceTextView.setText("00.00");
                break;
        }

        // fragmentTransaction.commit();
    }

    // 控制界面显示 UI
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_VIEW:

                    if (serviceBinder == null) {
                        Helper.Logger.i(getString(R.string.not_available));
                        return;
                    }

                    archiveMeta = serviceBinder.getMeta();

                    switch (serviceBinder.getStatus()) {
                        case Recorder.ServiceBinder.STATUS_RECORDING:
                            setViewStatus(FLAG_RECORDING);
                            isRecording = true;
                            pedometerstatus = true;
                            break;
                        case Recorder.ServiceBinder.STATUS_STOPPED:
                            setViewStatus(FLAG_ENDED);
                            isRecording = false;
                            pedometerstatus = false;
                    }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tracker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_records:
                gotoActivity(Records.class);
                break;

            case R.id.menu_configure:
                gotoActivity(Preference.class);
                break;

            case R.id.menu_feedback:
                UMFeedbackService.openUmengFeedbackSDK(context);
                break;

            case R.id.menu_help:
                gotoActivity(Info.class);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updateViewTimer != null) {
            updateViewTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            helper.showLongToast(getString(R.string.still_running));
        }
    }

    public String getPace(long between) {

        long day = between / (24 * 60 * 60 * 1000);
        long hour = (between / (60 * 60 * 1000) - day * 24);
        long minute = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long second = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
        return minute + "'" + second + "\"";
    }
}
