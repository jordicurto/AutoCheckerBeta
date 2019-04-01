package com.github.jordicurto.autochecker.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.github.jordicurto.autochecker.receiver.AutoCheckerBroadcastReceiver;


public class AutoCheckerIntentService extends JobIntentService {

    private static final int JOB_ID = 1;
    private final String TAG = getClass().getSimpleName();


    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AutoCheckerIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        AutoCheckerBroadcastReceiver.registerReceiver(getApplicationContext());
        Log.d(TAG, "Receiver registered");
    }

}
