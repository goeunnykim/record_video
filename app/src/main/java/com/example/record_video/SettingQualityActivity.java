package com.example.record_video;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingQualityActivity extends Activity {

    public static int quality=1;
    public static int videoFrameHeight=1080, videoFrameWidth=1920;
    public static String toastMessage;

    private ArrayList<String> arrayList;
    private ListView mListView;
    private SettingAdapter mSettingAdapter;

    SharedPreferences SettingQ;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setContentView(R.layout.activity_quality_setting);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();

        int width = (int) (dm.widthPixels * 0.9);

        int height = (int) (dm.heightPixels * 0.5);

        getWindow().getAttributes().width = width;

        getWindow().getAttributes().height = height;

        mListView = (ListView)findViewById(R.id.list_setting);

        arrayList = new ArrayList<>();
        arrayList.add("고화질");
        arrayList.add("저화질");
        arrayList.add("480p");
        arrayList.add("720p");
        arrayList.add("1080p");

        //mSettingAdapter = new SettingAdapter(getApplicationContext(), arrayList);
        //mListView.setAdapter(mSettingAdapter);

        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //mListView.setAdapter(new SettingAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,arrayList));

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, arrayList) ;

        mListView.setAdapter(adapter);

        /*저장된 quality 값 불러오기*/
        SettingQ = getSharedPreferences("quality",Activity.MODE_PRIVATE);
        editor = SettingQ.edit();

        int position = SettingQ.getInt("position",0);

        mListView.setItemChecked(position,true);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("position,id", String.valueOf(position+"d"+id));

                editor.putInt("position",position);
                editor.commit();

                switch (position){
                    case(0) : {
                        quality = 1;
                        videoFrameHeight = 1080;
                        videoFrameWidth = 1920;
                        toastMessage = "고화질";
                        break;
                    }
                    case(1) : {
                        quality = 0;
                        videoFrameHeight = 480;
                        videoFrameWidth = 640;
                        toastMessage = "저화질";
                        break;
                    }
                    case(2) : {
                        quality = 4;
                        videoFrameHeight = 480;
                        videoFrameWidth = 640;
                        toastMessage = "480p";
                        break;
                    }
                    case(3) : {
                        quality = 5;
                        videoFrameHeight = 720;
                        videoFrameWidth = 960;
                        toastMessage = "720p";
                        break;
                    }
                    case(4) : {
                        quality = 6;
                        videoFrameHeight = 1080;
                        videoFrameWidth = 1920;
                        toastMessage = "1080p";
                        break;
                    }
                }
                //finish();
                Toast.makeText(SettingQualityActivity.this,toastMessage,Toast.LENGTH_SHORT).show();
            }
        });
    }
}