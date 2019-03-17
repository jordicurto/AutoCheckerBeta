package com.github.jordicurto.autochecker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.service.AutoCheckerIntentService;

public class AutoCheckerGeofencingReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    @NonNull
    public static Intent createIntent(Context context, String action) {
        return new Intent(context, AutoCheckerGeofencingReceiver.class).setAction(action);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action != null) {

            Log.d(TAG, "Intent received " + action);

            switch (action) {
                case AutoCheckerConstants.GEOFENCE_TRANSITION_RECEIVED:
                case AutoCheckerConstants.GEOFENCE_TRANSITION_CONFIRM_RECEIVED:
                case AutoCheckerConstants.ALARM_FORCE_LEAVE_LOCATION:
                    AutoCheckerIntentService.enqueueWork(context, intent);
                    break;
                default:
                    Log.e(TAG, "Unmatched intent");
                    break;
            }
        }
    }
}
