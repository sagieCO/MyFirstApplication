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

        String eventName = intent.getStringExtra("eventName");
        boolean is24hBefore = intent.getBooleanExtra("is24hBefore", false);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "mechina_events";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "מכינות",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        String title = is24hBefore ?
                "תזכורת: מחר אירוע!" :
                "תזכורת: האירוע מתחיל עכשיו";

        String content = is24hBefore ?
                "האירוע " + eventName + " מחר!" :
                "האירוע " + eventName + " מתחיל עכשיו!";

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_event)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        int id = (eventName + (is24hBefore ? "24" : "now")).hashCode();

        manager.notify(id, builder.build());
    }
}
