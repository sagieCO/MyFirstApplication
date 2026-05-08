package com.sagie.myfirstapplication.models;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.sagie.myfirstapplication.R;

public class AlarmReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("ALARM_DEBUG", "Receiver triggered!");

        // שליפת נתונים מה-Intent
        String eventName = intent.getStringExtra("eventName");
        boolean is24hBefore = intent.getBooleanExtra("is24hBefore", false);

        // קבלת שירות ההתראות עם Casting מפורש
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "mechina_events";

        // יצירת ערוץ התראות עבור אנדרואיד 8 ומעלה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "מכינות",
                    NotificationManager.IMPORTANCE_HIGH
            );
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // קביעת כותרת ותוכן ההודעה באמצעות if/else במקום ? :
        String title;
        String content;

        if (is24hBefore) {
            title = "תזכורת: מחר אירוע!";
            content = "האירוע " + eventName + " יתקיים מחר!";
        } else {
            title = "תזכורת: האירוע מתחיל עכשיו";
            content = "האירוע " + eventName + " מתחיל עכשיו!";
        }

        // בניית ההתראה
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setSmallIcon(R.drawable.ic_event); // וודא שהאייקון קיים ב-drawable
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setAutoCancel(true);

        // יצירת ID ייחודי להתראה
        String uniqueKey;
        if (is24hBefore) {
            uniqueKey = eventName + "24";
        } else {
            uniqueKey = eventName + "now";
        }

        int id = uniqueKey.hashCode();

        // שליחת ההתראה
        if (manager != null) {
            manager.notify(id, builder.build());
        }
    }
}