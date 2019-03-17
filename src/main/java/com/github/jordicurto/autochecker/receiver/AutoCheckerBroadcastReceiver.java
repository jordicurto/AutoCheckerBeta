package com.github.jordicurto.autochecker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.service.AutoCheckerIntentService;

public class AutoCheckerBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    public static final String ACTION_QUICKBOOT_POWEROFF =
            "android.intent.action.QUICKBOOT_POWEROFF";

    public AutoCheckerBroadcastReceiver() {
    }

    public static void registerReceiver(@NonNull Context context) {

        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(AutoCheckerBroadcastReceiver.ACTION_QUICKBOOT_POWEROFF);

        AutoCheckerBroadcastReceiver receiver = new AutoCheckerBroadcastReceiver();
        context.registerReceiver(receiver, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action != null) {

            Log.d(TAG, "Intent received " + action);

            switch (action) {
                case Intent.ACTION_MY_PACKAGE_REPLACED:
                    AutoCheckerIntentService.enqueueWork(context,
                            AutoCheckerConstants.INTENT_START_SERVICE);
                    break;
                case LocationManager.MODE_CHANGED_ACTION:
                    AutoCheckerIntentService.enqueueWork(context,
                            AutoCheckerConstants.INTENT_REQUEST_CHECK_LOCATION);
                    break;
                case Intent.ACTION_SHUTDOWN:
                case ACTION_QUICKBOOT_POWEROFF:
                    AutoCheckerIntentService.enqueueWork(context,
                            AutoCheckerConstants.INTENT_SYSTEM_SHUTDOWN);
                    break;
                default:
                    Log.e(TAG, "Unmatched intent");
                    break;
            }
        }
    }
}
