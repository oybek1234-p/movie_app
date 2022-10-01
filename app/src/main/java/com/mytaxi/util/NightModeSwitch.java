package com.mytaxi.util;

import android.content.Context;
import android.content.SharedPreferences;

public class NightModeSwitch {

    SharedPreferences prefs;

    SharedPreferences.Editor editor;

    Context context;

    int PRIVATE_MODE = 0;
    final String NIGHT_MODE_SWITCH = "NightModeSwitch";
    final String NIGHT_MODE = "NightMode";

    public NightModeSwitch(Context context) {

        this.context = context;
        prefs = context.getSharedPreferences(NIGHT_MODE_SWITCH, PRIVATE_MODE);
        editor = prefs.edit();
    }

    public void setNightMode(String nightMode) {
        editor.putString(NIGHT_MODE, nightMode);
        editor.commit();
    }

    public String getNightMode() {
        return prefs.getString(NIGHT_MODE, "ON");
    }
}

