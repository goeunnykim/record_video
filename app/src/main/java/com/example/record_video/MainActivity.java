package com.example.record_video;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "Recorder";
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    public static Camera mCamera ;
    public static boolean mPreviewRunning;
    public static Context mContext;
    boolean perm = false;
    boolean record = false;
    static private Intent floating, recording;
    public static int camFacing = 0; // 0=후면, 1=전면

    //service 통신
    private IMyRecoderService binder;
    TextView timeTxt;
    private boolean running = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //서비스가 가진 binder를 리턴 받음
            binder = IMyRecoderService.Stub.asInterface(service);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private class GetTimeThread implements Runnable{
        private Handler handler = new Handler();
        String minute,sec;
        int crrTime, minuteT, secT;

        @Override
        public void run(){

            while(running){
                if(binder == null){
                    continue;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            crrTime = binder.getTime();
                            minuteT = (crrTime/60);
                            secT = (crrTime/10);

                            if( minuteT < 1){
                                minute = "00";
                            }else if(minuteT >= 1){
                                minute = "0"+ minuteT;
                            }
                            if(secT < 1){
                                sec = "0"+crrTime;
                                Log.d("result==sec if1",sec);
                            }else if(secT >= 1){
                                if(crrTime <= 60) {
                                    if(crrTime == 60){
                                        sec = "00";
                                    }else {
                                        sec = String.valueOf(crrTime);
                                    }
                                }else{
                                    if(crrTime - (minuteT * 60) < 10) {
                                        sec = "0"+(crrTime - (secT * 10));
                                        Log.d("result==sec else1",sec);
                                    }else{
                                        if(crrTime <= 300) {
                                            sec = String.valueOf(crrTime - (minuteT * 60));
                                        }else{
                                            stopRecording();
                                        }
                                        Log.d("result==sec else2",sec);
                                    }
                                }
                            }
                            timeTxt.setText(minute+":"+sec);
                            Log.d("result==printTime","!!");

                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

   // private static Camera mServiceCamera;
    //private MediaRecorder mMediaRecorder;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        stopService(new Intent(MainActivity.this, FloatWidgetService.class));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 102);
        }

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            //권한 물어보는 라이브러리
            TedPermission.with(this)
                    .setPermissionListener(permission)
                    .setRationaleMessage("녹화를 위하여 권한을 허용해주세요.")
                    .setDeniedMessage("권한이 거부되었습니다. 설정 > 권한에서 허용해주세요.")
                    .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                    .check();
        }else {
            perm = true;
        }

        getSaveFolder();
        Log.d("result==",String.valueOf("foldername"+getSaveFolder().getAbsolutePath()));
        Toast.makeText(getApplicationContext(),"foldername"+getSaveFolder().getAbsolutePath(), Toast.LENGTH_LONG).show();

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        timeTxt = (TextView)findViewById(R.id.timeTxt);

        Button btnStart = (Button) findViewById(R.id.StartService);
        btnStart.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startRecording();
                //recording = new Intent(MainActivity.this, RecorderService.class);
                //startService(recording);
               // setRecord();
//                finish();
            }
        });

        Button btnStop = (Button) findViewById(R.id.StopService);
        btnStop.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                stopRecording();
                //mCamera.stopPreview();
                //recording = new Intent(MainActivity.this, RecorderService.class);
                //test
                //stopService(recording);
                //setRecorded();
                //stopRecording_main();
            }
        });
        if (isLaunchingService(mContext)){
            Toast.makeText(MainActivity.this, "비디오 실행중", Toast.LENGTH_SHORT).show();
            //stopService(recording); //test
            //startService(recording);
//                mCamera = Camera.open();
//                mCamera.setDisplayOrientation(90);
        }
        //설정
        Button Setting_button = findViewById(R.id.SettingBtn);
        Setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(RecorderService.mRecordingStatus == false)
                {stopRecording();}
                startActivity(new Intent(getApplicationContext(), SettingActivity.class));

            }
        });

        final Button camFacingBtn = findViewById(R.id.cameraFacingBtn);
        camFacingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //카메라 새로 세팅(종료)
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;

                if(camFacing == 0) {
                    camFacing = 1;
                    camFacingBtn.setText("후면");

                }else if(camFacing == 1){
                    camFacing = 0;
                    camFacingBtn.setText("전면");
                }
                cameraFacing();
            }
        });

        Button galleryBtn = (Button)findViewById(R.id.galleryBtn);
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("content://media/internal/images/media"));
                startActivity(intent);
            }
        });
    }

    private File getSaveFolder() {
        String folderName = "DesignApp";
        //File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.pathSeparator + folderName);
        File dir = new File("/sdcard/DCIM/"+folderName);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }
    /*public void stopRecording_main() {
        Toast.makeText(getBaseContext(), "Recording Stopped_service", Toast.LENGTH_SHORT).show();
        try {
            RecorderService.mServiceCamera.reconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RecorderService.mMediaRecorder.stop();
        RecorderService.mMediaRecorder.reset();

        RecorderService.mServiceCamera.stopPreview();
        RecorderService.mMediaRecorder.release();

        RecorderService.mServiceCamera.release();
        RecorderService.mServiceCamera = null;
    }
*/
    public void cameraFacing(){

        mCamera  = Camera.open(camFacing);
        // 카메라 설정
        Camera.Parameters parameters = mCamera.getParameters();

        // 카메라의 회전이 가로/세로일때 화면을 설정한다.
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            parameters.set("orientation", "portrait");
            mCamera.setDisplayOrientation(90);
            parameters.setRotation(90);
        } else {
            parameters.set("orientation", "landscape");
            mCamera.setDisplayOrientation(0);
            parameters.setRotation(0);
        }
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 카메라 미리보기를 시작한다.
        mCamera.startPreview();

        // 자동포커스 설정
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {

                }
            }
        });
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("result==","created");
        /*try {
            if (mCamera == null) {
                mCamera = Camera.open();
               mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                mServiceCamera.unlock();
            }
        } catch (IOException e) { }*/
        //try {
            // 카메라 객체를 사용할 수 있게 연결한다.
            if (mCamera == null) {
                mCamera = Camera.open(camFacing);
                Log.d("result==isitnull", String.valueOf(mCamera));
            }
            else if(RecorderService.mRecordingStatus == true){

                mCamera  = Camera.open(camFacing);
            }
                // 카메라 설정
                Camera.Parameters parameters = mCamera.getParameters();

                // 카메라의 회전이 가로/세로일때 화면을 설정한다.
                if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "portrait");
                    mCamera.setDisplayOrientation(90);
                    parameters.setRotation(90);
                } else {
                    parameters.set("orientation", "landscape");
                    mCamera.setDisplayOrientation(0);
                    parameters.setRotation(0);
                }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("result==","changed");

            Camera.Parameters parameters = mCamera.getParameters();
            mCamera.setParameters(parameters);
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 카메라 미리보기를 시작한다.
            mCamera.startPreview();

            // 자동포커스 설정
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {

                    }
                }
            });
//        if (mSurfaceHolder.getSurface() == null) { return; }
//        try { mCamera.stopPreview(); } catch (Exception e) {}
//        Camera.Parameters parameters = mCamera.getParameters();
//        List<String> focusModes = parameters.getSupportedFocusModes();
//        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); }
//        mCamera.setParameters(parameters);
//        try { mCamera.setPreviewDisplay(mSurfaceHolder); mCamera.startPreview(); } catch (Exception e) { }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("result==","destroy");
        // TODO Auto-generated method stub
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    PermissionListener permission = new PermissionListener() {
        @Override
        public void onPermissionGranted() {

        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, "권한 거부", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
            if (perm) {
                Log.e(TAG, "home");
                Log.d("result==", String.valueOf(record));
                if(isRecord()){
                    startService(new Intent(MainActivity.this, FloatWidgetService.class));
                    finish();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                }
            }
//                finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        finish();
        startActivity(intent);
        if (requestCode == 102 && resultCode == RESULT_OK) {
        } else {
            Toast.makeText(this, "Draw over other app permission not enable.", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (recording!=null) {
//            stopService(recording);
//            recording = null;
//        }
//    }

    public boolean isRecord(){
        return record;
    }
    public void setRecord(){
        record = true;
        RecorderService.mRecordingStatus = false;
    }
    public void setRecorded(){
        record = false;
        RecorderService.mRecordingStatus = true;
    }
    public void startRecording(){

        Toast.makeText(getBaseContext(), "Recording Start_Main", Toast.LENGTH_SHORT).show();
        Intent Record = new Intent(MainActivity.this, RecorderService.class);
        //Record.putExtra("mCamera", (Parcelable) mCamera);
        setRecord();
        startService(Record);

        bindService(Record, connection, BIND_AUTO_CREATE);
        running = true;
        new Thread(new GetTimeThread()).start();
    }
    public void stopRecording(){
        if(running == true) {
            Toast.makeText(getBaseContext(), "Recording Stopped_Main", Toast.LENGTH_SHORT).show();
            Intent Record = new Intent(MainActivity.this, RecorderService.class);
            setRecorded();
            stopService(Record);

            unbindService(connection);
            running = false;
            timeTxt.setText("00:00");
        }
    }
    public Boolean isLaunchingService(Context mContext){

        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RecorderService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return  false;
    }

}
