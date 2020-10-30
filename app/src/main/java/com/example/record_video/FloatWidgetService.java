package com.example.record_video;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Calendar;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class FloatWidgetService extends Service {
    private WindowManager mWindowManager;
    private View mFloatingWidget;
    public FloatWidgetService(){}
    @Override
    public IBinder onBind(Intent intent) { return null; }
    @Override
    public void onCreate() {
        super.onCreate();
        final boolean[] play = {false};
        //아이콘 설정
        int LAYOUT_FLAG;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 100;
        params.y = 100;
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingWidget, params);
        final View collapsedView = mFloatingWidget.findViewById(R.id.collapse_view);
        final View expandedView = mFloatingWidget.findViewById(R.id.expanded_container);
        final ImageView circle = (ImageView) mFloatingWidget.findViewById(R.id.collapsed_iv);
        final ImageView excircle = (ImageView) mFloatingWidget.findViewById(R.id.back_button);
        if (((MainActivity)MainActivity.mContext).isRecord()){
            circle.setImageResource(R.drawable.red_circle);
        }else circle.setImageResource(R.drawable.black_circle);

        ImageView closeButtonCollapsed = (ImageView) mFloatingWidget.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });

        final ImageView playButton = (ImageView) mFloatingWidget.findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((MainActivity)MainActivity.mContext).isRecord()){
                    playButton.setImageResource(R.drawable.playing);
                    excircle.setImageResource(R.drawable.black_circle);
//                    ((MainActivity)MainActivity.mContext).stopRecording();
                    ((RecorderService)RecorderService.mContext).pauseRecording();
                    ((MainActivity)MainActivity.mContext).setRecorded();
                }else {
                    playButton.setImageResource(R.drawable.pausing);
                    excircle.setImageResource(R.drawable.red_circle);
//                    Intent intent = new Intent(FloatWidgetService.this, RecorderService.class);
//                    startService(intent);
                    ((RecorderService)RecorderService.mContext).resumeRecording();
                    ((MainActivity)MainActivity.mContext).setRecord();
                }
            }
        });

        ImageView expandButton = (ImageView) mFloatingWidget.findViewById(R.id.expand_button);
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //((MainActivity)MainActivity.mContext).stopRecording(); //test
                Intent intent = new Intent(FloatWidgetService.this,MainActivity.class);
                startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));
                stopSelf();
            }
        });

        ImageView closeButton = (ImageView) mFloatingWidget.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });
        //움직이는거
        mFloatingWidget.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    //처음 눌렀을때
                    case MotionEvent.ACTION_DOWN:
                        //시작위치 가져옴
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    //누른걸 땠을때
                    case MotionEvent.ACTION_UP:
                        //별로 움직이지 않았으면 이벤트 실행
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
//                                onDestroy();
                                if (((MainActivity)MainActivity.mContext).isRecord()){
                                    excircle.setImageResource(R.drawable.red_circle);
                                    playButton.setImageResource(R.drawable.pausing);
                                    setAlarmTimer();
                                }else {
                                    excircle.setImageResource(R.drawable.black_circle);
                                    playButton.setImageResource(R.drawable.playing);
                                    ((MainActivity)MainActivity.mContext).setRecorded();
                                }
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }else {
                                if (((MainActivity)MainActivity.mContext).isRecord()){
                                    circle.setImageResource(R.drawable.red_circle);
                                    ((MainActivity)MainActivity.mContext).setRecord();
                                }else {
                                    circle.setImageResource(R.drawable.black_circle);
                                    ((MainActivity)MainActivity.mContext).setRecorded();
                                }
                                collapsedView.setVisibility(View.VISIBLE);
                                expandedView.setVisibility(View.GONE);
                            }
                        }
                        return true;
                    //누르고 움직였을때
                    case MotionEvent.ACTION_MOVE:
                        //아이콘 위치 기록 및 변경
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingWidget, params);
                        return true;
                }
                return false;
            }
        });
    }
    private boolean isViewCollapsed() {
        return mFloatingWidget == null || mFloatingWidget.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingWidget != null) mWindowManager.removeView(mFloatingWidget);
    }
    public void startrecording(){
        Intent in = new Intent(this, RecorderService.class);
        startService(in);
    }
    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, AlarmRecever.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }
}