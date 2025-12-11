package com.sagie.myfirstapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {

    private static final String PREF_NAME = "AppPrefs";
    private static final String LOGIN_COUNT = "login_count";

    public static void incrementLoginCount(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int count = sp.getInt(LOGIN_COUNT, 0);
        sp.edit().putInt(LOGIN_COUNT, count + 1).apply();
    }

    public static int getLoginCount(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(LOGIN_COUNT, 0);
    }
}
