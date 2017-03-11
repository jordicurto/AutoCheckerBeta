package com.github.jordicurto.autochecker.fragment;

import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.Duration;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by jordi on 22/05/16.
 */
public class AutoCheckerWeekDayRecords {

    private LocalDate weekDay;
    private Duration duration;
    private List<WatchedLocationRecord> records;

    public AutoCheckerWeekDayRecords(LocalDate weekDay, List<WatchedLocationRecord> records) {

        this.weekDay = weekDay;
        this.records = records;
        duration = DateUtils.calculateDuration(records);
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
        return records;
    }
}
