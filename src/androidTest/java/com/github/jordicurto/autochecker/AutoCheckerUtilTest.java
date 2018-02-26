package com.github.jordicurto.autochecker;

import android.util.Log;

import com.github.jordicurto.autochecker.util.DateUtils;

import junit.framework.TestCase;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Calendar;

public class AutoCheckerUtilTest extends TestCase {

	public void runTest() {

		Calendar calS = Calendar.getInstance();
		calS.set(2015, Calendar.JANUARY, 5, 11, 15, 0);
		Calendar calE = Calendar.getInstance();
		calE.set(2015, Calendar.JANUARY, 1, 23, 52, 0);

		LocalDate dateS = LocalDate.fromCalendarFields(calS);
		LocalDate dateE = LocalDate.fromCalendarFields(calE);

		String TAG = "TEST JODA";
		Log.d(TAG, "Temps : " + DateUtils.getCurrentDateMillis());
		Log.d(TAG, "StartOfWeek " + calS.getFirstDayOfWeek());

		Log.d(TAG, "DAY_INTERVAL");
		for (Interval i : DateUtils.getDateIntervals(dateS, dateE,
				DateUtils.INTERVAL_TYPE.DAYS)) {
			Log.d(TAG, i.toString());
		}

		Log.d(TAG, "WEEK_INTERVAL");
		for (Interval i : DateUtils.getDateIntervals(dateS, dateE,
				DateUtils.INTERVAL_TYPE.WEEKS)) {
			Log.d(TAG, i.toString());
		}

		Log.d(TAG, "MONTH_INTERVAL");
		for (Interval i : DateUtils.getDateIntervals(dateS, dateE,
				DateUtils.INTERVAL_TYPE.MONTHS)) {
			Log.d(TAG, i.toString());
		}

        Period p = new Period(-1 * (2 * DateUtils.HOURS_PER_MILLISECOND) + (30 * DateUtils.MINS_PER_MILLISECOND));
        PeriodFormatter formatter = new PeriodFormatterBuilder().
                printZeroAlways().minimumPrintedDigits(2)
                .appendHours().appendLiteral(":").rejectSignedValues(true).appendMinutes().toFormatter();

        Log.d(TAG, formatter.print(p));
        Log.d(TAG, formatter.print(p.negated()));
    }

	public AutoCheckerUtilTest () {

	}
}
