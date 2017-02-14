package com.github.jordicurto.autochecker.constants;

import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.Duration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class AutoCheckerConstants {

	public static final String PREF_TEST = "test";

	public static final String FIRST_RUN = "FIRST_RUN";
	public static final String CURRENT_LOCATION_ID = "CURRENT_LOCATION_ID";
	public static final String PREF_START_DAY_HOUR = "PREF_START_DAY_HOUR";
	public static final String PREF_SHOW_WEEKENDS = "PREF_SHOW_WEEKENDS";

	public static final WatchedLocation WL_GTD = new WatchedLocation("GTD", 2.209514, 41.400665, 100);

    public static final long INVALID_DELAY = -1L;
    public static final long DELAY_FOR_SUSPECT_TRANSITION = 3 * Duration.MINS_PER_MILLISECOND;
    public static final long DELAY_FOR_STANDARD_TRANSITION = 1 * Duration.SECS_PER_MILLISECOND;

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


    public static List<WatchedLocationRecord> getDummyRecords(WatchedLocation location) {

		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		List<WatchedLocationRecord> records = new ArrayList<>();
		WatchedLocationRecord rec = new WatchedLocationRecord();
		rec.setLocation(location);

		cal.set(2016, Calendar.MAY, 9, 9, 0, 0);
		rec.setCheckIn(cal.getTime());
		cal.set(2016, Calendar.MAY, 9, 11, 0, 0);
		rec.setCheckOut(cal.getTime());
		records.add(rec);

		rec = new WatchedLocationRecord();
		rec.setLocation(location);

		cal.set(2016, Calendar.MAY, 9, 12, 0, 0);
		rec.setCheckIn(cal.getTime());
		cal.set(2016, Calendar.MAY, 9, 12, 30, 0);
		rec.setCheckOut(cal.getTime());
		records.add(rec);

		rec = new WatchedLocationRecord();
		rec.setLocation(location);

		cal.set(2016, Calendar.MAY, 9, 13, 0, 0);
		rec.setCheckIn(cal.getTime());
		cal.set(2016, Calendar.MAY, 9, 13, 30, 0);
		rec.setCheckOut(cal.getTime());
		records.add(rec);

		rec = new WatchedLocationRecord();
		rec.setLocation(location);

		cal.set(2016, Calendar.MAY, 9, 14, 30, 0);
		rec.setCheckIn(cal.getTime());
		cal.set(2016, Calendar.MAY, 9, 16, 30, 0);
		rec.setCheckOut(cal.getTime());
		records.add(rec);

		rec = new WatchedLocationRecord();
		rec.setLocation(location);

		cal.set(2016, Calendar.MAY, 9, 17, 0, 0);
		rec.setCheckIn(cal.getTime());
		cal.set(2016, Calendar.MAY, 9, 22, 0, 0);
		rec.setCheckOut(cal.getTime());
		records.add(rec);

		rec = new WatchedLocationRecord();
		rec.setLocation(location);

		cal.set(2016, Calendar.MAY, 9, 22, 30, 0);
		rec.setCheckIn(cal.getTime());
		cal.set(2016, Calendar.MAY, 9, 23, 30, 0);
		rec.setCheckOut(cal.getTime());
		records.add(rec);

		rec = new WatchedLocationRecord();
		rec.setLocation(location);

		cal.set(2016, Calendar.MAY, 10, 0, 30, 0);
		rec.setCheckIn(cal.getTime());
		cal.set(2016, Calendar.MAY, 10, 2, 30, 0);
		rec.setCheckOut(cal.getTime());
		records.add(rec);

		return records;
	}
}
