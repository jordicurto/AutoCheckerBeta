package com.github.jordicurto.autochecker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.service.AutoCheckerIntentService;

public class AutoCheckerBootBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null) {

            Log.d(TAG, "Intent received " + intent.getAction());

            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                    intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {

                AutoCheckerIntentService.enqueueWork(context,
                        AutoCheckerConstants.INTENT_START_SERVICE);
            }

        }
    }
}
