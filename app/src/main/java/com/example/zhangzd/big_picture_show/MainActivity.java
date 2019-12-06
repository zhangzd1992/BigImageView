package com.example.zhangzd.big_picture_show;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BigPictureView bigPictureView = findViewById(R.id.bigPicView);
        InputStream is = null;
        try {
            is = getAssets().open("pic.jpg");
            bigPictureView.setImage(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
