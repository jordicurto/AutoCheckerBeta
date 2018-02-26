package com.github.jordicurto.autochecker.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.Duration;

/**
 *
 */
public class AutoCheckerPreferencesManager {

    private String mCurrentLocationName;
    private boolean mShowWeekends;
    private int mStartDayHour;
    private int mWeeksToShow;
    private boolean mRelativeDurations;

    private Duration[] mDurations = null;

    public AutoCheckerPreferencesManager() {
    }

    public void updatePreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        mCurrentLocationName = prefs.getString(AutoCheckerConstants.CURRENT_LOCATION_ID,
                AutoCheckerConstants.WL_GTD.getName());
        mShowWeekends = prefs.getBoolean(AutoCheckerConstants.PREF_SHOW_WEEKENDS, false);
        mStartDayHour = prefs.getInt(AutoCheckerConstants.PREF_START_DAY_HOUR,
                AutoCheckerConstants.DEFAULT_START_DAY_HOUR);
        mWeeksToShow = prefs.getInt(AutoCheckerConstants.PREF_WEEKS_TO_SHOW,
                (int) AutoCheckerConstants.DEFAULT_WEEKS_TO_SHOW);
        mRelativeDurations = prefs.getBoolean(AutoCheckerConstants.PREF_SHOW_RELATIVE_DURATIONS,
                false);
        mDurations = new Duration[3];
        mDurations[DateUtils.INTERVAL_TYPE.DAYS.getIndex()] =
                new Duration(prefs.getLong(AutoCheckerConstants.PREF_EXPECTED_DURATION_PER_DAY,
                        AutoCheckerConstants.DEFAULT_EXPECTED_DURATION_PER_DAY));
        mDurations[DateUtils.INTERVAL_TYPE.WEEKS.getIndex()] =
                new Duration(prefs.getLong(AutoCheckerConstants.PREF_EXPECTED_DURATION_PER_WEEK,
                        AutoCheckerConstants.DEFAULT_EXPECTED_DURATION_PER_WEEK));
        mDurations[DateUtils.INTERVAL_TYPE.MONTHS.getIndex()] = Duration.ZERO;
    }

    public Duration getDuration(DateUtils.INTERVAL_TYPE intervalType) {
        return mDurations[intervalType.getIndex()];
    }

    public String getCurrentLocationName() {
        return mCurrentLocationName;
    }

    public boolean isShowWeekends() {
        return mShowWeekends;
    }

    public int getStartDayHour() {
        return mStartDayHour;
    }

    public int getWeeksToShow() {
        return mWeeksToShow;
    }

    public boolean isRelativeDurations() {
        return mRelativeDurations;
    }
}
