package com.github.jordicurto.autochecker.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.util.ContextKeeper;
import com.github.jordicurto.autochecker.util.DateUtils;

/**
 *
 */
public class AutoCheckerAlarmsManager extends ContextKeeper {

    private AlarmManager mManager;

    private static AutoCheckerAlarmsManager mInstance = null;

    public static AutoCheckerAlarmsManager getManager(Context context) {
        if (mInstance == null)
            mInstance = new AutoCheckerAlarmsManager(context);
        return mInstance;
    }

    private AutoCheckerAlarmsManager(Context context) {
        super(context);
        mManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
    }

    public void setAlarm(long delay, PendingIntent intent) {
        mManager.set(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() + delay), intent);
    }

    public void cancelAlarm(PendingIntent intent) {
        mManager.cancel(intent);
    }

    public void setRepeatingAlarm(long startOffset, long interval, PendingIntent intent) {
        mManager.setRepeating(AlarmManager.RTC_WAKEUP,
                (System.currentTimeMillis() + startOffset), interval, intent);
    }

    public void configureNotificationDurationAlarm() {
        setRepeatingAlarm(AutoCheckerConstants.DELAY_BEFORE_START_ALARMS, AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                PendingIntent.getBroadcast(getContext(), 0,
                        new Intent(AutoCheckerConstants.ALARM_NOTIFICATION_DURATION),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void configureHousekeepingAlarm() {
        setRepeatingAlarm(AutoCheckerConstants.DELAY_BEFORE_START_ALARMS, AlarmManager.INTERVAL_HOUR,
                PendingIntent.getBroadcast(getContext(), 0,
                        new Intent(AutoCheckerConstants.ALARM_HOUSEKEEPING),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void configureDayChangeAlarm() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        long timeToStartDay = DateUtils.getMillisUtilDayChange(
                prefs.getInt(AutoCheckerConstants.PREF_START_DAY_HOUR,
                        AutoCheckerConstants.DEFAULT_START_DAY_HOUR));
        setRepeatingAlarm(timeToStartDay, AlarmManager.INTERVAL_DAY,
                PendingIntent.getBroadcast(getContext(), 0,
                        new Intent(AutoCheckerConstants.ALARM_DAY_CHANGE),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

}
