package com.github.jordicurto.autochecker.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.receiver.AutoCheckerGeofencingReceiver;
import com.github.jordicurto.autochecker.util.ContextKeeper;
import com.github.jordicurto.autochecker.util.DateUtils;

/**
 *
 */
public class AutoCheckerAlarmsManager extends ContextKeeper {

    static int ENTER_LOCATION_ALARM_ID = 0;
    static int LEAVE_LOCATION_ALARM_ID = 1;

    private AlarmManager mManager;

    public AutoCheckerAlarmsManager(Context context) {
        super(context);
        mManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
    }

    void setAlarm(long delay, int id, Intent intent) {
        if (Build.VERSION.SDK_INT >= 23) {
            mManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    (System.currentTimeMillis() + delay),
                    createPendingIntent(intent, id));
        } else {
            mManager.setExact(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() + delay),
                    createPendingIntent(intent, id));
        }
    }

    void cancelAlarm(Intent intent, int id) {
        mManager.cancel(createPendingIntent(intent, id));
    }

    private void setRepeatingAlarm(long startOffset, long interval, Intent intent) {
        int REPEATING_ALARM_ID = 2;
        mManager.setRepeating(AlarmManager.RTC_WAKEUP,
                (System.currentTimeMillis() + startOffset), interval,
                createPendingIntent(intent, REPEATING_ALARM_ID));
    }

    public void setAlarmForceLeaveLocation(long duration) {
        Intent intent = AutoCheckerGeofencingReceiver.createIntent(getContext(),
                AutoCheckerConstants.ALARM_FORCE_LEAVE_LOCATION);
        setAlarm(duration, ENTER_LOCATION_ALARM_ID, intent);
    }

    public void configureNotificationDurationAlarm() {
        Intent intent = AutoCheckerGeofencingReceiver.createIntent(getContext(),
                AutoCheckerConstants.ALARM_NOTIFICATION_DURATION);
        setRepeatingAlarm(AutoCheckerConstants.DELAY_BEFORE_START_ALARMS,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, intent);
    }

    public void configureHousekeepingAlarm() {
        Intent intent = AutoCheckerGeofencingReceiver.createIntent(getContext(),
                AutoCheckerConstants.ALARM_HOUSEKEEPING);
        setRepeatingAlarm(AutoCheckerConstants.DELAY_BEFORE_START_ALARMS,
                AlarmManager.INTERVAL_HOUR, intent);
    }

    public void configureDayChangeAlarm() {
        Intent intent = AutoCheckerGeofencingReceiver.createIntent(getContext(),
                AutoCheckerConstants.ALARM_DAY_CHANGE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        long timeToStartDay = DateUtils.getMillisUtilDayChange(
                prefs.getInt(AutoCheckerConstants.PREF_START_DAY_HOUR,
                        AutoCheckerConstants.DEFAULT_START_DAY_HOUR));
        setRepeatingAlarm(timeToStartDay, AlarmManager.INTERVAL_DAY, intent);
    }

    public void cancelAlarmForceLeaveLocation() {
        Intent intent = AutoCheckerGeofencingReceiver.createIntent(getContext(),
                AutoCheckerConstants.ALARM_FORCE_LEAVE_LOCATION);
        cancelAlarm(intent, ENTER_LOCATION_ALARM_ID);
    }

    private PendingIntent createPendingIntent(Intent intent, int id) {
        return PendingIntent.getBroadcast(getContext(), id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
