package com.sagie.myfirstapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class NewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        ImageView timerImage = findViewById(R.id.timer_image);

        final int[] images = {
                R.drawable.img_12,
                R.drawable.img_11,
                R.drawable.img_10,
                R.drawable.img_9,
                R.drawable.img_8
        };

        new CountDownTimer(5000, 1000) {
            int i = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                timerImage.setImageResource(images[i]);
                i++;
            }

            @Override
            public void onFinish() {
                // לאחר הספירה, נפתח את NextActivity
                Intent intent = new Intent(NewActivity.this, NextActivity.class);
                startActivity(intent);
            }
        }.start();
    }
}
