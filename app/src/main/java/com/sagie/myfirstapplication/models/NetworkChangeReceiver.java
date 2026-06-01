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
        if (!isOnline(context)) {
            Toast.makeText(context, "אין חיבור לאינטרנט  בדקו את הרשת", Toast.LENGTH_LONG).show();
            Log.d("NetworkChangeReceiver","אין חיבור לאינטרנט  בדקו את הרשת");
        } else {
            Toast.makeText(context, "החיבור לרשת חזר", Toast.LENGTH_SHORT).show();
            Log.d("NetworkChangeReceiver","החיבור לרשת חזר");
        }
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }
}