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


        String eventName = intent.getStringExtra("eventName");
        boolean is24hBefore = intent.getBooleanExtra("is24hBefore", false);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "mechina_events";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "מכינות", NotificationManager.IMPORTANCE_HIGH);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        String title;
        String content;

        if (is24hBefore) {
            title = "תזכורת: מחר אירוע!";
            content = "האירוע " + eventName + " יתקיים מחר!";
        } else {
            title = "תזכורת: האירוע מתחיל עכשיו";
            content = "האירוע " + eventName + " מתחיל עכשיו!";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setSmallIcon(R.drawable.ic_event);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setAutoCancel(true);

        String uniqueKey;
        if (is24hBefore) {
            uniqueKey = eventName + "24";
        } else {
            uniqueKey = eventName + "now";
        }

        int id = uniqueKey.hashCode();

        if (manager != null) {
            manager.notify(id, builder.build());
        }
    }
}