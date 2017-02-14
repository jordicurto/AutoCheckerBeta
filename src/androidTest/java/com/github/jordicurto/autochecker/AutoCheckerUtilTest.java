package com.github.jordicurto.autochecker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.util.Log;
import android.util.Pair;

import com.github.jordicurto.autochecker.util.DateUtils;

import junit.framework.TestCase;

public class AutoCheckerUtilTest extends TestCase {

	public void runTest() {

		DateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault());

		Calendar calS = Calendar.getInstance();
		calS.set(2015, Calendar.JANUARY, 5, 11, 15, 0);
		Calendar calE = Calendar.getInstance();
		calE.set(2015, Calendar.JANUARY, 1, 23, 52, 0);

		String TAG = "TEST";
		Log.d(TAG, "Temps : " + DateUtils.currentTimeMillis());
		Log.d(TAG, "StartOfWeek " + calS.getFirstDayOfWeek());

		Log.d(TAG, "DAY_INTERVAL");
		for (Date d : DateUtils.getDateIntervals(calS.getTime(),
				calE.getTime(), 6, DateUtils.DAY_INTERVAL_TYPE)) {
			Log.d(TAG, format.format(d));
		}

		Log.d(TAG, "WEEK_INTERVAL");
		for (Date d : DateUtils.getDateIntervals(calS.getTime(),
				calE.getTime(), 6, DateUtils.WEEK_INTERVAL_TYPE)) {
			Log.d(TAG, format.format(d));
		}

		Log.d(TAG, "MONTH_INTERVAL");
		for (Date d : DateUtils.getDateIntervals(calS.getTime(),
				calE.getTime(), 6, DateUtils.MONTH_INTERVAL_TYPE)) {
			Log.d(TAG, format.format(d));
		}

		Pair<Date, Date> dayDates = DateUtils
				.getLimitDates(DateUtils.DAY_INTERVAL_TYPE);
		Log.d(TAG, "Day Limit: " + format.format(dayDates.first) + " - "
				+ format.format(dayDates.second));

		Pair<Date, Date> weekDates = DateUtils
				.getLimitDates(DateUtils.WEEK_INTERVAL_TYPE);
		Log.d(TAG, "Week Limit: " + format.format(weekDates.first) + " - "
				+ format.format(weekDates.second));

		Pair<Date, Date> monthDates = DateUtils
				.getLimitDates(DateUtils.MONTH_INTERVAL_TYPE);
		Log.d(TAG, "Month Limit: " + format.format(monthDates.first) + " - "
				+ format.format(monthDates.second));
	}
}
