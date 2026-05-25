package com.sagie.myfirstapplication.models;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // בדיקה האם יש חיבור לרשת
        if (!isOnline(context)) {
            // כאן את שמה את הקוד שאת רוצה שיקרה כשאין אינטרנט
            Toast.makeText(context, "אין חיבור לאינטרנט! אנא בדקי את הרשת.", Toast.LENGTH_LONG).show();
            Log.d("NetworkChangeReceiver","אין חיבור לאינטרנט! אנא בדקי את הרשת.");
        } else {
            // אופציונלי: קוד שיקרה כשהאינטרנט חוזר
            Toast.makeText(context, "החיבור לרשת חזר!", Toast.LENGTH_SHORT).show();
            Log.d("NetworkChangeReceiver","החיבור לרשת חזר!");
        }
    }

    // פונקציית עזר לבדיקת מצב החיבור
    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }
}