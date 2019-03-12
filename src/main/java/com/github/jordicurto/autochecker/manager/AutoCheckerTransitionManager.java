package com.github.jordicurto.autochecker.manager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.receiver.AutoCheckerBroadcastReceiver;
import com.github.jordicurto.autochecker.util.ContextKeeper;

import org.joda.time.LocalDateTime;

/**
 *
 */
public class AutoCheckerTransitionManager extends ContextKeeper {

    private final String TAG = getClass().getSimpleName();

    public static final int ENTER_TRANSITION = 0;
    public static final int LEAVE_TRANSITION = 1;

    private AutoCheckerAlarmsManager mAutoCheckerAlarmsManager;

    public AutoCheckerTransitionManager(Context context) {
        super(context);
        mAutoCheckerAlarmsManager = new AutoCheckerAlarmsManager(context);
    }

    private Intent createIntent(WatchedLocation location, long time, int direction) {

        Intent intent = AutoCheckerBroadcastReceiver.createBroadcastIntent(getContext(),
                AutoCheckerConstants.GEOFENCE_TRANSITION_CONFIRM_RECEIVED);
        intent.putExtra(AutoCheckerConstants.LOCATION_ID, location.getId());
        intent.putExtra(AutoCheckerConstants.TRANSITION_TIME, time);
        intent.putExtra(AutoCheckerConstants.TRANSITION_DIRECTION, direction);

        return intent;
    }

    public void scheduleRegisterTransition(WatchedLocation location, long time, int direction, long delay) {

        mAutoCheckerAlarmsManager.setAlarm(delay, createIntent(location, time, direction));
    }

    public void cancelScheduledRegisterTransition() {

        Intent intent = AutoCheckerBroadcastReceiver.createBroadcastIntent(getContext(),
                AutoCheckerConstants.GEOFENCE_TRANSITION_CONFIRM_RECEIVED);
        mAutoCheckerAlarmsManager.cancelAlarm(intent);
    }

    public void registerTransition(WatchedLocation location, long time, int direction) {

        switch (direction) {
            case ENTER_TRANSITION:
                registerEnterTransition(location, time);
                break;
            case LEAVE_TRANSITION:
                registerLeaveTransition(location, time);
                break;
            default:
                break;
        }

        // Notify activities
        Intent intentAct = new Intent(AutoCheckerConstants.INTENT_ACTIVITY_RELOAD_REQUEST);
        intentAct.putExtra(AutoCheckerConstants.LOCATION_ID, location.getId());
        //LocalBroadcastManager.getInstance(getContext()).
        getContext().sendBroadcast(intentAct);
    }

    private void registerEnterTransition(WatchedLocation location, long time) {

        if (location != null) {

            Log.d(TAG, "Processing enter event");

            AutoCheckerBusinessManager.getManager(getContext()).createCheckInRecord(location,
                    new LocalDateTime(time), false);
        }
    }

    private void registerLeaveTransition(WatchedLocation location, long time) {

        if (location != null) {

            Log.d(TAG, "Processing leave event");

            AutoCheckerBusinessManager.getManager(getContext()).updateCheckOutRecord(location,
                    new LocalDateTime(time), false);
        }
    }
}
