package com.github.jordicurto.autochecker;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by jordi on 26/02/17.
 */

public class AutoCheckerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }

}
