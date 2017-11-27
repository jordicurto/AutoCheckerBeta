package com.github.jordicurto.autochecker.constants;

import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.DateTimeConstants;

public class AutoCheckerConstants {

	public static final String INSTALL_TIME = "INSTALL_TIME";
	public static final String CURRENT_LOCATION_ID = "CURRENT_LOCATION_ID";
	public static final String PREF_START_DAY_HOUR = "PREF_START_DAY_HOUR";
	public static final String PREF_SHOW_WEEKENDS = "PREF_SHOW_WEEKENDS";

	public static final WatchedLocation WL_GTD = new WatchedLocation("GTD", 2.209370, 41.400649, 100);

    public static final long INVALID_DELAY = -1L;
    public static final long DELAY_FOR_SUSPECT_TRANSITION = 3 * DateUtils.MINS_PER_MILLISECOND;
    public static final long DELAY_FOR_STANDARD_TRANSITION = 1 * DateTimeConstants.MILLIS_PER_SECOND;

    public static final int LOITERING_DELAY = 5 * DateTimeConstants.MILLIS_PER_SECOND;

    public static final String GEOFENCE_TRANSITION_RECEIVED = "GEOFENCE_TRANSITION_RECEIVED";
	public static final String GEOFENCE_TRANSITION_CONFIRM_RECEIVED = "GEOFENCE_TRANSITION_CONFIRM_RECEIVED";
    public static final String INTENT_ACTIVITY_RELOAD_REQUEST = "INTENT_ACTIVITY_RELOAD_REQUEST";

    public static final String ALARM_NOTIFICATION_DURATION = "ALARM_NOTIFICATION_DURATION";

    public final static String INTENT_REQUEST_REGISTER_GEOFENCES = "INTENT_REQUEST_REGISTER_GEOFENCES";
    public final static String INTENT_PERMISSION_GRANTED = "INTENT_PERMISSION_GRANTED";
    public final static String INTENT_REQUEST_CHECK_LOCATION = "INTENT_REQUEST_CHECK_LOCATION";

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
