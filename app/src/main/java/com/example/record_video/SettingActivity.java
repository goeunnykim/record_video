package com.example.record_video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingActivity extends Activity {
    private String toastMessage;

    private ArrayList<String> arrayList;
    private ListView mListView;
    private SettingAdapter mSettingAdapter;
    private String qualityMessage = SettingQualityActivity.toastMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mListView = (ListView)findViewById(R.id.list_setting);

        arrayList = new ArrayList<>();

        arrayList.add("화질");

        mSettingAdapter = new SettingAdapter(getApplicationContext(), arrayList);
        mListView.setAdapter(mSettingAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("position,id", String.valueOf(position+"d"+id));
                switch (position){
                    case(0) : {
                        toastMessage = "화질선택";
                        startActivity(new Intent(SettingActivity.this,SettingQualityActivity.class));
                        break;
                    }
                }
                Toast.makeText(SettingActivity.this,toastMessage,Toast.LENGTH_SHORT).show();
            }
        });
    }
}