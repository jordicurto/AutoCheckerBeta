package com.github.jordicurto.autochecker.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;

import org.joda.time.*;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordi on 21/02/17.
 */

public class DateUtils {

    public static final int MINS_PER_MILLISECOND =
            DateTimeConstants.SECONDS_PER_MINUTE * DateTimeConstants.MILLIS_PER_SECOND;
    public static final int HOURS_PER_MILLISECOND =
            DateTimeConstants.MINUTES_PER_HOUR * MINS_PER_MILLISECOND;
    public static final int DAYS_PER_MILLISECOND =
            DateTimeConstants.HOURS_PER_DAY * HOURS_PER_MILLISECOND;

    public static final DateTimeFormatter timeFormat =
            new DateTimeFormatterBuilder().appendHourOfDay(2).appendLiteral(":")
                    .appendMinuteOfHour(2).toFormatter();
    public static final PeriodFormatter durationFormat =
            new PeriodFormatterBuilder().printZeroAlways().minimumPrintedDigits(2)
                    .appendHours().appendLiteral(":").appendMinutes().toFormatter();
    public static final DateTimeFormatter dayFormat =
            new DateTimeFormatterBuilder().appendDayOfWeekText()
                    .appendLiteral(", ").appendDayOfMonth(1).toFormatter();
    public static final DateTimeFormatter weekFormat =
            new DateTimeFormatterBuilder().appendDayOfMonth(1).appendLiteral(" ").
                    appendMonthOfYearText().toFormatter();
    public static final DateTimeFormatter weekDayFormat =
            new DateTimeFormatterBuilder().appendDayOfMonth(1).toFormatter();
    public static final DateTimeFormatter hourFormat =
            new DateTimeFormatterBuilder().appendHourOfDay(1).toFormatter();

    public enum INTERVAL_TYPE {
        DAYS(0),
        WEEKS(1),
        MONTHS(2);

        private int index;

        INTERVAL_TYPE(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public enum PART_OF_DAY {
        MORNING(0),
        AFTERNOON(1),
        NIGHT(2);

        private int index;

        PART_OF_DAY(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public static List<Interval> getDateIntervals(LocalDate start, LocalDate end,
                                                  INTERVAL_TYPE intervalType) {

        List<Interval> intervals = new ArrayList<>();

        LocalDate startDate = start;
        LocalDate endDate = end;

        switch (intervalType) {
            case DAYS:
                endDate = end.plusDays(1);
                break;
            case WEEKS:
                startDate = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
                endDate = endDate.plusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY);
                break;
            case MONTHS:
                startDate = startDate.withDayOfMonth(1);
                endDate = endDate.plusMonths(1).withDayOfMonth(1);
                break;
        }

        while (startDate.isBefore(endDate)) {

            Interval interval;

            if (intervalType == INTERVAL_TYPE.WEEKS) {
                interval = startDate.toInterval().withPeriodAfterStart(
                        Period.weeks(1).minusMinutes(1));
                startDate = startDate.plusWeeks(1);
            } else if (intervalType == INTERVAL_TYPE.MONTHS) {
                interval = startDate.toInterval().withPeriodAfterStart(
                        Period.months(1).minusMinutes(1));
                startDate = startDate.plusMonths(1);
            } else {
                interval = startDate.toInterval().withPeriodAfterStart(
                        Period.days(1).minusMinutes(1));
                startDate = startDate.plusDays(1);
            }

            intervals.add(interval);
        }

        return intervals;
    }

    public static String getDateIntervalString(Interval interval) {

        boolean sameMonth =
                interval.getStart().getMonthOfYear() == interval.getEnd().getMonthOfYear();

        return (sameMonth ? weekDayFormat.print(interval.getStart())
                : weekFormat.print(interval.getStart()))
                + (sameMonth ? " - " : "\n")
                + weekFormat.print(interval.getEnd());
    }

    public static LocalDate getEndDate(LocalDate startDate, boolean weekend) {
        return startDate.withDayOfWeek(weekend ?
                DateTimeConstants.SUNDAY : DateTimeConstants.FRIDAY);
    }

    public static Duration calculateDuration(WatchedLocationRecord record, Interval weekDay,
                                             int startHourDay) {
        Duration checkDuration = Duration.ZERO;
        LocalDateTime start = weekDay.getStart().plusHours(startHourDay).toLocalDateTime();
        LocalDateTime end = weekDay.getEnd().plusHours(startHourDay).toLocalDateTime();
        if (record.getCheckIn() != null) {
            checkDuration = Duration.standardSeconds(
                Seconds.secondsBetween(
                        (start.isAfter(record.getCheckIn()) ? start : record.getCheckIn()),
                        record.isActive() ? getCurrentDate() :
                            (end.isBefore(record.getCheckOut()) ? end : record.getCheckOut()))
                        .getSeconds());
        }
        return checkDuration;
    }

    public static Duration calculateDuration(List<WatchedLocationRecord> records,
                                             Interval weekDay, int startHourDay) {
        Duration checksDuration = Duration.ZERO;
        for(WatchedLocationRecord record : records) {
            checksDuration = checksDuration.plus(calculateDuration(record, weekDay, startHourDay));
        }
        return checksDuration;
    }

    public static String getDurationString (Duration duration) {
        return durationFormat.print(duration.toPeriod());
    }

    public static LocalDateTime getCurrentDate() {
        return LocalDateTime.now().minuteOfHour().roundFloorCopy();
    }

    public static long getCurrentDateMillis() {
        return getCurrentDate().toDateTime().getMillis();
    }

    public static long getApplicationInstallTime(Context context, String appName) {
        long installTime = System.currentTimeMillis();
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(appName, 0);
            String appFile = appInfo.sourceDir;
            installTime = new File(appFile).lastModified();
        } catch (PackageManager.NameNotFoundException e) {
        }
        return installTime;
    }
}
