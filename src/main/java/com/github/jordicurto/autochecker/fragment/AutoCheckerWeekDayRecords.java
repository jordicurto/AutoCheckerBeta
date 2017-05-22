package com.github.jordicurto.autochecker.fragment;

import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by jordi on 22/05/16.
 */
public class AutoCheckerWeekDayRecords {

    private LocalDate weekDay;
    private Duration duration;
    private List<WatchedLocationRecord> mRecords;

    public AutoCheckerWeekDayRecords(Interval interval, int startHourDay,
                                     List<WatchedLocationRecord> records) {

        mRecords = records;
        weekDay = interval.getStart().toLocalDate();
        duration = DateUtils.calculateDuration(records, interval, startHourDay);
    }

    public LocalDate getWeekDay() {
        return weekDay;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getWeekDayString() {
        return DateUtils.dayFormat.print(weekDay);
    }

    public String getDurationString() {
        return DateUtils.getDurationString(duration);
    }

    public List<WatchedLocationRecord> getRecords() {
        return mRecords;
    }
}
