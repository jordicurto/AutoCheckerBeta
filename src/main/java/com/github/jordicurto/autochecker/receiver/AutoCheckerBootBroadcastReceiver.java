package com.github.jordicurto.autochecker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;

public class AutoCheckerBootBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action != null) {

            Log.d(TAG, "Intent received " + action);

            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                case Intent.ACTION_MY_PACKAGE_REPLACED:

                    context.sendBroadcast(AutoCheckerGeofencingReceiver.createIntent(context,
                            AutoCheckerConstants.INTENT_START_RECEIVER));
                    break;

                default:

                    Log.e(TAG, "Unmatched intent");
                    break;
            }
        }
    }
}
