package com.github.jordicurto.autochecker.constants;

import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.DateTimeConstants;

public class AutoCheckerConstants {

	public static final String PREF_TEST = "test";

	public static final String FIRST_RUN = "FIRST_RUN";
	public static final String CURRENT_LOCATION_ID = "CURRENT_LOCATION_ID";
	public static final String PREF_START_DAY_HOUR = "PREF_START_DAY_HOUR";
	public static final String PREF_SHOW_WEEKENDS = "PREF_SHOW_WEEKENDS";

	public static final WatchedLocation WL_GTD = new WatchedLocation("GTD", 2.209514, 41.400665, 100);

    public static final long INVALID_DELAY = -1L;
    public static final long DELAY_FOR_SUSPECT_TRANSITION = 3 * DateUtils.MINS_PER_MILLISECOND;
    public static final long DELAY_FOR_STANDARD_TRANSITION = 1 * DateTimeConstants.MILLIS_PER_SECOND;

    public static final String GEOFENCE_TRANSITION_RECEIVED = "GEOFENCE_TRANSITION_RECEIVED";
	public static final String GEOFENCE_TRANSITION_CONFIRM_RECEIVED = "GEOFENCE_TRANSITION_CONFIRM_RECEIVED";
    public static final String ALARM_NOTIFICATION_DURATION = "ALARM_NOTIFICATION_DURATION";

    public final static String LOCATIONS_ACTIVITY_FIRST_RUN = "LOCATIONS_ACTIVITY_FIRST_RUN";

    public final static String TRANSITION_RECEIVED = "TRANSITION_RECEIVED";

    public static final String LOCATION_ID = "LOCATION_ID";
	public static final String TRANSITION_TIME = "TRANSITION_TIME";
	public static final String TRANSITION_DIRECTION = "TRANSITION_DIRECTION";

    public static final int TRANSTION_ENTER_NOTIFICATION_ID = 100;
	public static final int TRANSTION_LEAVE_NOTIFICATION_ID = 200;

    public static final int DEFAULT_START_DAY_HOUR = 6;

}
