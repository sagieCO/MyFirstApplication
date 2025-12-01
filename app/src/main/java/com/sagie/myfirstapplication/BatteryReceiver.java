package com.sagie.myfirstapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.TextView;

public class BatteryReceiver extends BroadcastReceiver {
    private TextView tvBattery; // נוסיף שדה עבור TextView

    // נוכל להעביר את ה-TextView מה-Activity דרך Constructor או Setter
    public BatteryReceiver(TextView tvBattery) {
        this.tvBattery = tvBattery;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // קבלת פרטי הסוללה
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); // אחוז הסוללה
        if (tvBattery != null) {
            tvBattery.setText("Battery Level: " + level + "%"); // עדכון ה-TextView
        }
    }
}
