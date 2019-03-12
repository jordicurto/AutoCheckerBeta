package com.github.jordicurto.autochecker.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.receiver.AutoCheckerBroadcastReceiver;
import com.github.jordicurto.autochecker.util.ContextKeeper;
import com.github.jordicurto.autochecker.util.DateUtils;

/**
 *
 */
public class AutoCheckerAlarmsManager extends ContextKeeper {

    private AlarmManager mManager;

    public AutoCheckerAlarmsManager(Context context) {
        super(context);
        mManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
    }

    public void setAlarm(long delay, Intent intent) {
        mManager.set(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() + delay),
                PendingIntent.getBroadcast(getContext(), 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void cancelAlarm(Intent intent) {
        mManager.cancel(PendingIntent.getBroadcast(getContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void setRepeatingAlarm(long startOffset, long interval, Intent intent) {
        mManager.setRepeating(AlarmManager.RTC_WAKEUP,
                (System.currentTimeMillis() + startOffset), interval,
                PendingIntent.getBroadcast(getContext(), 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void setAlarmForceLeaveLocation(long duration) {
        Intent intent = AutoCheckerBroadcastReceiver.createBroadcastIntent(getContext(),
                AutoCheckerConstants.ALARM_FORCE_LEAVE_LOCATION);
        setAlarm(duration, intent);
    }

    public void configureNotificationDurationAlarm() {
        Intent intent = AutoCheckerBroadcastReceiver.createBroadcastIntent(getContext(),
                AutoCheckerConstants.ALARM_NOTIFICATION_DURATION);
        setRepeatingAlarm(AutoCheckerConstants.DELAY_BEFORE_START_ALARMS,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, intent);
    }

    public void configureHousekeepingAlarm() {
        Intent intent = AutoCheckerBroadcastReceiver.createBroadcastIntent(getContext(),
                AutoCheckerConstants.ALARM_HOUSEKEEPING);
        setRepeatingAlarm(AutoCheckerConstants.DELAY_BEFORE_START_ALARMS,
                AlarmManager.INTERVAL_HOUR, intent);
    }

    public void configureDayChangeAlarm() {
        Intent intent = AutoCheckerBroadcastReceiver.createBroadcastIntent(getContext(),
                AutoCheckerConstants.ALARM_DAY_CHANGE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        long timeToStartDay = DateUtils.getMillisUtilDayChange(
                prefs.getInt(AutoCheckerConstants.PREF_START_DAY_HOUR,
                        AutoCheckerConstants.DEFAULT_START_DAY_HOUR));
        setRepeatingAlarm(timeToStartDay, AlarmManager.INTERVAL_DAY, intent);
    }

    public void cancelAlarmForceLeaveLocation() {
        Intent intent = AutoCheckerBroadcastReceiver.createBroadcastIntent(getContext(),
                AutoCheckerConstants.ALARM_FORCE_LEAVE_LOCATION);
        cancelAlarm(intent);
    }
}
