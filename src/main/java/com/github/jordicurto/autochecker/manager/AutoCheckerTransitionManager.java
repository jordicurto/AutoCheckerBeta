package com.github.jordicurto.autochecker.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.util.Log;

import com.github.jordicurto.autochecker.data.AutoCheckerDataSource;
import com.github.jordicurto.autochecker.data.exception.NoLocationFoundException;
import com.github.jordicurto.autochecker.data.exception.NoRecordFoundException;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.util.ContextKeeper;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jordi on 12/10/16.
 */
public class AutoCheckerTransitionManager extends ContextKeeper {

    private final String TAG = getClass().getSimpleName();

    public static final int ENTER_TRANSITION = 0;
    public static final int LEAVE_TRANSITION = 1;

    public AutoCheckerTransitionManager(Context context) {
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

        AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        if (manager != null)
            manager.set(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() + delay),
                    PendingIntent.getBroadcast(mContext, 0, createIntent(location, time, direction), PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void cancelScheduledRegisterTransition() {

        AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        if (manager != null)
            manager.cancel(PendingIntent.getBroadcast(mContext, 0,
                    new Intent(AutoCheckerConstants.GEOFENCE_TRANSITION_CONFIRM_RECEIVED), PendingIntent.FLAG_UPDATE_CURRENT));
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
        mContext.sendBroadcast(intentAct);
    }

    private void registerEnterTransition(WatchedLocation location, long time) {

        if (location != null) {

            Log.d(TAG, "Processing enter event");

            AutoCheckerBusinessManager.getManager(mContext).createCheckInRecord(location,
                    new LocalDateTime(time));
        }
    }

    private void registerLeaveTransition(WatchedLocation location, long time) {

        if (location != null) {

            Log.d(TAG, "Processing leave event");

            AutoCheckerBusinessManager.getManager(mContext).updateCheckOutRecord(location,
                    new LocalDateTime(time));
        }
    }
}
