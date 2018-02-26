package com.github.jordicurto.autochecker.manager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.util.ContextKeeper;

import org.joda.time.LocalDateTime;

/**
 *
 */
public class AutoCheckerTransitionManager extends ContextKeeper {

    private final String TAG = getClass().getSimpleName();

    public static final int ENTER_TRANSITION = 0;
    public static final int LEAVE_TRANSITION = 1;

    private static AutoCheckerTransitionManager mInstance;

    private AutoCheckerTransitionManager(Context context) {
        super(context);
    }

    private Intent createIntent(WatchedLocation location, long time, int direction) {

        Intent intent = new Intent(AutoCheckerConstants.GEOFENCE_TRANSITION_CONFIRM_RECEIVED);
        intent.putExtra(AutoCheckerConstants.LOCATION_ID, location.getId());
        intent.putExtra(AutoCheckerConstants.TRANSITION_TIME, time);
        intent.putExtra(AutoCheckerConstants.TRANSITION_DIRECTION, direction);

        return intent;
    }

    public void scheduleRegisterTransition(WatchedLocation location, long time, int direction, long delay) {

        AutoCheckerAlarmsManager.getManager(getContext()).setAlarm(delay,
                PendingIntent.getBroadcast(getContext(), 0, createIntent(location, time, direction),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void cancelScheduledRegisterTransition() {

        AutoCheckerAlarmsManager.getManager(getContext()).cancelAlarm(
                PendingIntent.getBroadcast(getContext(), 0,
                        new Intent(AutoCheckerConstants.GEOFENCE_TRANSITION_CONFIRM_RECEIVED),
                        PendingIntent.FLAG_UPDATE_CURRENT));
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

    public static AutoCheckerTransitionManager getInstance(Context context) {
        if (mInstance == null)
            mInstance = new AutoCheckerTransitionManager(context);
        return mInstance;
    }
}
