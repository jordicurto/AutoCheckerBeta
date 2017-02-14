package com.github.jordicurto.autochecker.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.util.Pair;

public class DateUtils {

    public static final SimpleDateFormat timeFormat = new SimpleDateFormat(
            "HH:mm", Locale.getDefault());
    public static final SimpleDateFormat hourFormat = new SimpleDateFormat(
            "H", Locale.getDefault());
    public static final SimpleDateFormat dayFormat = new SimpleDateFormat(
            "cccc, d", Locale.getDefault());
    public static final SimpleDateFormat weekFormat = new SimpleDateFormat(
            "d MMMM", Locale.getDefault());
    public static final SimpleDateFormat weekDayFormat = new SimpleDateFormat(
            "d", Locale.getDefault());

    public static final int DAY_INTERVAL_TYPE = 0;
    public static final int WEEK_INTERVAL_TYPE = 1;
    public static final int MONTH_INTERVAL_TYPE = 2;

    public static List<Date> getDateIntervals(Date start, Date end, int startDayHour,
                                              int intervalType) {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);

        startCalendar.add(Calendar.HOUR_OF_DAY, startDayHour);
        startCalendar.set(Calendar.MINUTE,
                startCalendar.getMinimum(Calendar.MINUTE));
        startCalendar.set(Calendar.HOUR_OF_DAY,
                startCalendar.getMinimum(Calendar.HOUR_OF_DAY));

        endCalendar.add(Calendar.HOUR_OF_DAY, startDayHour);
        endCalendar.set(Calendar.MINUTE,
                endCalendar.getMaximum(Calendar.MINUTE));
        endCalendar.set(Calendar.HOUR_OF_DAY,
                endCalendar.getMaximum(Calendar.HOUR_OF_DAY));

        if (intervalType == WEEK_INTERVAL_TYPE) {
            startCalendar.set(Calendar.DAY_OF_WEEK,
                    startCalendar.getFirstDayOfWeek());

        } else if (intervalType == MONTH_INTERVAL_TYPE) {
            startCalendar.set(Calendar.DAY_OF_MONTH,
                    startCalendar.getMinimum(Calendar.DAY_OF_MONTH));
        }

        List<Date> dates = new ArrayList<Date>();

        while (startCalendar.before(endCalendar)) {
            dates.add(startCalendar.getTime());
            switch (intervalType) {
                case DAY_INTERVAL_TYPE:
                    startCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                case WEEK_INTERVAL_TYPE:
                    startCalendar.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case MONTH_INTERVAL_TYPE:
                    startCalendar.add(Calendar.MONTH, 1);
                    break;
            }
        }

        dates.add(startCalendar.getTime());

        return dates;
    }

    public static Pair<Date, Date> getInterval(List<Date> dates,
                                               int intervalPosition) {

        if ((intervalPosition - 1) < dates.size()) {

            return new Pair<Date, Date>(dates.get(intervalPosition), new Date(
                    dates.get(intervalPosition + 1).getTime() - 10000));
        }

        return null;
    }

    public static boolean sameMonth(Pair<Date, Date> interval) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(interval.first);
        int monthStart = cal.get(Calendar.MONTH);
        cal.setTime(interval.second);
        int monthEnd = cal.get(Calendar.MONTH);

        return (monthStart == monthEnd);
    }

    public static String getDateIntervalString(List<Date> dates,
                                               int intervalPosition) {

        Pair<Date, Date> interval = getInterval(dates, intervalPosition);

        boolean sameMonth = sameMonth(interval);

        return (sameMonth ? weekDayFormat.format(interval.first)
                : weekFormat.format(interval.first))
                + (sameMonth ? " - " : "\n")
                + weekFormat.format(interval.second);
    }

    public static Pair<Date, Date> getLimitDates(int intervalType) {

        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();

        if (intervalType == MONTH_INTERVAL_TYPE) {

            startCalendar.set(Calendar.DAY_OF_MONTH,
                    startCalendar.getActualMinimum(Calendar.DAY_OF_MONTH));

        } else if (intervalType == WEEK_INTERVAL_TYPE) {

            startCalendar.set(Calendar.DAY_OF_WEEK,
                    startCalendar.getFirstDayOfWeek());
        }

        startCalendar.set(Calendar.HOUR_OF_DAY,
                startCalendar.getMinimum(Calendar.HOUR_OF_DAY));
        startCalendar.set(Calendar.MINUTE,
                startCalendar.getMinimum(Calendar.MINUTE));
        startCalendar.set(Calendar.SECOND,
                startCalendar.getMinimum(Calendar.SECOND));
        startCalendar.set(Calendar.MILLISECOND,
                startCalendar.getMinimum(Calendar.MILLISECOND));

        if (intervalType == MONTH_INTERVAL_TYPE) {

            endCalendar.set(Calendar.DAY_OF_MONTH,
                    endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        } else if (intervalType == WEEK_INTERVAL_TYPE) {

            endCalendar.set(Calendar.DAY_OF_WEEK,
                    (endCalendar.getMaximum(Calendar.DAY_OF_WEEK)));
            endCalendar.add(
                    Calendar.DAY_OF_MONTH,
                    (endCalendar.getMaximum(Calendar.DAY_OF_WEEK) + endCalendar
                            .getFirstDayOfWeek() - 1)
                            % endCalendar.getMaximum(Calendar.DAY_OF_WEEK));
        }

        endCalendar.set(Calendar.HOUR_OF_DAY,
                endCalendar.getMaximum(Calendar.HOUR_OF_DAY));
        endCalendar.set(Calendar.MINUTE,
                endCalendar.getMaximum(Calendar.MINUTE));
        endCalendar.set(Calendar.SECOND,
                endCalendar.getMaximum(Calendar.SECOND));
        endCalendar.set(Calendar.MILLISECOND,
                endCalendar.getMaximum(Calendar.MILLISECOND));

        return new Pair<Date, Date>(startCalendar.getTime(),
                endCalendar.getTime());
    }

    public static long currentTimeMillis() {
        return (((System.currentTimeMillis() + (Duration.MINS_PER_MILLISECOND / 2))
                / Duration.MINS_PER_MILLISECOND) * Duration.MINS_PER_MILLISECOND);
    }

    public static Date getCurrentDate() {
        return new Date(currentTimeMillis());
    }

    public static Date getDateStartDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getCurrentDate());
        calendar.set(Calendar.HOUR, calendar.getMinimum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
        return calendar.getTime();
    }

    public static Date getEndDate(Date startDate, boolean weekend) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() - 1);
        //cal.add(Calendar.DAY_OF_WEEK, 1);
        if (!weekend)
            cal.add(Calendar.DAY_OF_WEEK, -2);
        return cal.getTime();
    }
}
