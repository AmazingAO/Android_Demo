package com.example.photowalldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity {

    private GridView mphotoWall;

    private PhotoWallAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mphotoWall = findViewById(R.id.photo_wall);
        adapter = new PhotoWallAdapter(this,0, Images.imageThumbUrls,mphotoWall);
        mphotoWall.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cancleAllTasks();
    }
}
