package com.github.jordicurto.autochecker.constants;

import com.github.jordicurto.autochecker.BuildConfig;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;

public class AutoCheckerConstants {

	public static final String INSTALL_TIME = "INSTALL_TIME";
	public static final String CURRENT_LOCATION_ID = "CURRENT_LOCATION_ID";

	public static final String PREF_START_DAY_HOUR = "PREF_START_DAY_HOUR";
	public static final String PREF_SHOW_WEEKENDS = "PREF_SHOW_WEEKENDS";
    public static final String PREF_WEEKS_TO_SHOW = "PREF_WEEKS_TO_SHOW";
    public static final String PREF_SHOW_RELATIVE_DURATIONS = "PREF_SHOW_RELATIVE_DURATIONS";
    public static final String PREF_EXPECTED_DURATION_PER_DAY = "PREF_EXPECTED_DURATION_PER_DAY";
    public static final String PREF_EXPECTED_DURATION_PER_WEEK = "PREF_EXPECTED_DURATION_PER_WEEK";

    public static final long DEFAULT_EXPECTED_DURATION_PER_DAY = 8L * DateUtils.HOURS_PER_MILLISECOND;
    public static final long DEFAULT_EXPECTED_DURATION_PER_WEEK = 40L * DateUtils.HOURS_PER_MILLISECOND;

	public static final WatchedLocation WL_GTD = new WatchedLocation("GTD", 2.209370, 41.400649, 100);

    public static final long INVALID_DELAY = -1L;
    public static final long DELAY_FOR_SUSPECT_TRANSITION = 3L * DateUtils.MINS_PER_MILLISECOND;
    public static final long DELAY_FOR_STANDARD_TRANSITION = (BuildConfig.DEBUG ? 1L : 30L) * DateTimeConstants.MILLIS_PER_SECOND;

    public static final long DEFAULT_WEEKS_TO_SHOW = 12L;
    public static final Duration RECORDS_HOLD_DURATION = new Duration(DEFAULT_WEEKS_TO_SHOW * DateTimeConstants.MILLIS_PER_WEEK);

    public static final long DELAY_BEFORE_START_ALARMS = DateUtils.MINS_PER_MILLISECOND;

    public static final int INTERVAL_BETWEEN_LOCATION_UPDATES = 2 * DateUtils.MINS_PER_MILLISECOND;
    public static final int FASTEST_INTERVAL_BETWEEN_LOCATION_UPDATES = 5 * DateTimeConstants.MILLIS_PER_SECOND;

    public static final String GEOFENCE_TRANSITION_RECEIVED = "GEOFENCE_TRANSITION_RECEIVED";
	public static final String GEOFENCE_TRANSITION_CONFIRM_RECEIVED = "GEOFENCE_TRANSITION_CONFIRM_RECEIVED";
    public static final String INTENT_ACTIVITY_RELOAD_REQUEST = "INTENT_ACTIVITY_RELOAD_REQUEST";

    public static final String ALARM_NOTIFICATION_DURATION = "ALARM_NOTIFICATION_DURATION";
    public static final String ALARM_HOUSEKEEPING = "ALARM_HOUSEKEEPING";
    public static final String ALARM_DAY_CHANGE = "ALARM_DAY_CHANGE";
    public static final String ALARM_FORCE_LEAVE_LOCATION = "ALARM_FORCE_LEAVE_LOCATION";

    public static final String INTENT_START_RECEIVER = "INTENT_START_RECEIVER";
    public static final String INTENT_SYSTEM_SHUTDOWN = "INTENT_SYSTEM_SHUTDOWN";
    public final static String INTENT_REQUEST_REGISTER_GEOFENCES = "INTENT_REQUEST_REGISTER_GEOFENCES";
    public final static String INTENT_PERMISSION_GRANTED = "INTENT_PERMISSION_GRANTED";
    public final static String INTENT_REQUEST_CHECK_LOCATION = "INTENT_REQUEST_CHECK_LOCATION";
    public final static String INTENT_FORCE_LEAVE_LOCATION = "INTENT_FORCE_LEAVE_LOCATION";
    public final static String INTENT_CANCEL_LEAVE_LOCATION = "INTENT_CANCEL_LEAVE_LOCATION";

    public static final String INTENT_FORCE_LEAVE_LOCATION_EXTRA_DURATION = "DURATION_EXTRA";

    public static final String LOCATION_ID = "LOCATION_ID";
	public static final String TRANSITION_TIME = "TRANSITION_TIME";
	public static final String TRANSITION_DIRECTION = "TRANSITION_DIRECTION";

    public static final int NOTIFICATION_TRANSITION_ENTER_ID = 10;
	public static final int NOTIFICATION_TRANSITION_LEAVE_ID = 20;
    public static final int NOTIFICATION_REGISTER_GEOFENCE_ID = 30;
    public static final int NOTIFICATION_PERMISSION_REQUIRED = 40;
    public static final int NOTIFICATION_ENABLE_LOCATION = 50;

    public static final int DEFAULT_START_DAY_HOUR = 6;
    public static final int HOURS_BETWEEN_DAY_PART = 8;

    public static final String KEY_PERMISSIONS = "KEY_PERMISSIONS";
    public static final String KEY_GRANT_RESULTS = "KEY_GRANT_RESULTS";
    public static final String KEY_RESULT_RECEIVER = "KEY_RESULT_RECEIVER";
    public static final String KEY_REQUEST_CODE = "KEY_REQUEST_CODE";

}
