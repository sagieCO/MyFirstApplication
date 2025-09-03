package com.sagie.myfirstapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer == null) {
            // הכנס קובץ שיר ל-res/raw בשם mysong.mp3
            mediaPlayer = MediaPlayer.create(this, R.raw.brazil);
            mediaPlayer.setLooping(true);
        }

        String action = intent.getStringExtra("action");
        if ("start".equals(action)) {
            mediaPlayer.start();
        } else if ("stop".equals(action)) {
            mediaPlayer.pause();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // לא משתמשים ב-Bound Service
    }
}
