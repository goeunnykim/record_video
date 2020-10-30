package com.example.record_video;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class RecorderService extends Service {
    private static final String TAG = "RecorderService";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    //private static Camera mServiceCamera;
    //private boolean mRecordingStatus;
    //private MediaRecorder mMediaRecorder;
    public static Context mContext;

    public static Camera mServiceCamera;
    public static MediaRecorder mMediaRecorder;
    public static boolean mRecordingStatus = false;

    public Size mPreviewSize;

    private String videoName;
    private String Str_Path;

    private int camFacing = MainActivity.camFacing;
    private int quality = SettingQualityActivity.quality;

    Thread thread;
    int sec = 0;

    @Override
    public void onCreate() {
        mContext = this;
        //mRecordingStatus = false;
        //mServiceCamera = CameraRecorder.mCamera;
        //액티비티 카메라 가져옴

        //Intent intent_camera = null;
        //mServiceCamera = intent_camera.getParcelableExtra("mCamera");

        //mServiceCamera = MainActivity.mCamera;
        mServiceCamera = Camera.open(camFacing);//카메라 개수 따라 달라짐 //전면 후면 선택
        mServiceCamera.setDisplayOrientation(90);
        mSurfaceView = MainActivity.mSurfaceView;
        mSurfaceHolder = MainActivity.mSurfaceHolder;

        super.onCreate();
        Log.d("result==mRecord", String.valueOf(mRecordingStatus));
        if (mRecordingStatus == false) //test
        { startRecording();}

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub

        //return null;
        return binder;
    }

    @Override
    public void onDestroy() {
        //stopRecording(); //test
        if(mRecordingStatus)
        { stopRecording();}
        //mRecordingStatus = false;

        super.onDestroy();
    }

    public boolean startRecording(){
        try {
            Toast.makeText(getBaseContext(), "Recording Started", Toast.LENGTH_SHORT).show();

            //mServiceCamera = Camera.open();
            Camera.Parameters params = mServiceCamera.getParameters();
            mServiceCamera.setParameters(params);
            Camera.Parameters p = mServiceCamera.getParameters();

            final List<Size> listSize = p.getSupportedPreviewSizes();
            mPreviewSize = listSize.get(camFacing); //전면 후면 선택
            Log.v(TAG, "use: width = " + mPreviewSize.width
                    + " height = " + mPreviewSize.height);
            p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            p.setPreviewFormat(PixelFormat.YCbCr_420_SP);

            mServiceCamera.setParameters(p);
            //test
            //mServiceCamera.setPreviewDisplay(mSurfaceHolder);
            try {
                mServiceCamera.setPreviewDisplay(mSurfaceHolder);
                mServiceCamera.startPreview();
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String dateStr = sdf.format(cal.getTime());
            videoName = dateStr+".mp4";
            Str_Path = "/sdcard/DCIM/";//DesignApp/";

            mServiceCamera.unlock();

             /*   boolean result = CamcorderProfile.hasProfile(0,CamcorderProfile.QUALITY_480P);
                Log.d("result==p",String.valueOf(result));*/
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mServiceCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFile(Str_Path+videoName);
            if(camFacing == 0) {
                mMediaRecorder.setOrientationHint(90);
            }else if(camFacing == 1){
                mMediaRecorder.setOrientationHint(270);
            }
           /* mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            mMediaRecorder.setOrientationHint(90);
            mMediaRecorder.setOutputFile("/sdcard/DCIM/video1022.mp4");
            mMediaRecorder.setVideoFrameRate(20);
            */
           /*if(SettingActivity.quality == 1){
               int quality480p = CamcorderProfile.QUALITY_480P;
           }*/

            Log.d("result==quality", String.valueOf(quality));
            /*Camera.CameraInfo.CAMERA_FACING_FRONT//Camera.CameraInfo.CAMERA_FACING_BACK*/
            //전면 후면 선택
            CamcorderProfile profile = CamcorderProfile.get(camFacing,quality);
            profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
            profile.videoCodec = MediaRecorder.VideoEncoder.MPEG_4_SP;
            profile.audioCodec = MediaRecorder.AudioEncoder.DEFAULT;
            profile.videoFrameRate = 20;
            profile.videoFrameHeight = SettingQualityActivity.videoFrameHeight;//480;//mPreviewSize.height;
            profile.videoFrameWidth = SettingQualityActivity.videoFrameWidth;//640;//mPreviewSize.width;
            Log.d("result==height",String.valueOf(SettingQualityActivity.videoFrameHeight));
            Log.d("result==width",String.valueOf(SettingQualityActivity.videoFrameWidth));
            profile.videoBitRate = 15;

/*
            if (p != null) {


                List<Camera.Size> pictureSizeList = p.getSupportedPictureSizes();
                for (Camera.Size size : pictureSizeList) {        //지원하는 사진 크기

                    Log.e("==PictureSize==", "width : " + size.width + "  height : " + size.height);
                }

                List<Camera.Size> previewSizeList = p.getSupportedPreviewSizes();
                for (Camera.Size size : previewSizeList) {        //지원하는 프리뷰 크기

                    Log.e("==PreviewSize==", "width : " + size.width + "  height : " + size.height);
                }
            }
*/
            /*mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            mMediaRecorder.setOrientationHint(90);
            mMediaRecorder.setOutputFile("/sdcard/DCIM/video1022.mp4");
            mMediaRecorder.setVideoFrameRate(20);
            //mPreviewSize.width = 1280; mPreviewSize.height = 720;
            mMediaRecorder.setVideoSize(mPreviewSize.width, mPreviewSize.height);
*/
            mMediaRecorder.setProfile(profile);

            Log.d("result==m", String.valueOf(mPreviewSize.width)+"dd"+mPreviewSize.height);

            mMediaRecorder.prepare();
            mMediaRecorder.start();

            thread = new Thread();
            thread.start();

            mRecordingStatus = true;

            return true;
        } catch (IllegalStateException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public void pauseRecording(){
        mMediaRecorder.pause();
    }
    public void resumeRecording(){
        mMediaRecorder.resume();
    }
    public void stopRecording() {
        Toast.makeText(getBaseContext(), "Recording Stopped_service", Toast.LENGTH_SHORT).show();
        try {
            mServiceCamera.reconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        thread.stopThread();

        mMediaRecorder.stop();
        mMediaRecorder.reset();

        //mServiceCamera.stopPreview();
        mMediaRecorder.release();

        mServiceCamera.release();
        mServiceCamera = null;

        MediaScanner media_scanner = MediaScanner .newInstance(getApplicationContext());
        try {

            media_scanner.mediaScanning(Str_Path + videoName); // 경로 + 제목
        } catch (Exception e) {
            e.printStackTrace();

            System.out.println(":::: Media Scan ERROR:::: = " + e);
        }
    }
    public boolean check(){
        return mRecordingStatus;
    }


    class Thread extends java.lang.Thread{
        boolean stopped;


        private Handler handler = new Handler();

        public Thread(){
            stopped = false;
        }
        public void stopThread(){
            stopped = true;
        }

        @Override
        public void run(){
            super.run();
            while(stopped == false){
                sec++;
                Log.d("result==", String.valueOf(sec));
                /*handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getApplicationContext(),sec+"초",Toast.LENGTH_SHORT).show();
                    }
                });*/
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    IMyRecoderService.Stub binder = new IMyRecoderService.Stub() {
        @Override
        public int getTime() throws RemoteException {
            return sec;
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }
    };
    @Override
    public boolean onUnbind(Intent intent){
        //isStop = true;
        return super.onUnbind(intent);
    }
}