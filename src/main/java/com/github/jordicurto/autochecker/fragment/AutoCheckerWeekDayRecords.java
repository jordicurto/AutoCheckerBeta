package com.github.jordicurto.autochecker.fragment;

import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.DateUtils;
import com.github.jordicurto.autochecker.util.Duration;

import java.util.Date;
import java.util.List;

/**
 * Created by jordi on 22/05/16.
 */
public class AutoCheckerWeekDayRecords {

    private Date weekDay;
    private Duration duration;
    private List<WatchedLocationRecord> records;

    public AutoCheckerWeekDayRecords(Date weekDay, List<WatchedLocationRecord> records) {

        this.weekDay = weekDay;
        this.records = records;
        duration = Duration.calculateDuration(records);
    }

    public Date getWeekDay() {
        return weekDay;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getWeekDayString() {
        return DateUtils.dayFormat.format(weekDay);
    }

    public String getDurationString() {
        return duration.toString();
    }

    public List<WatchedLocationRecord> getRecords() {
        return records;
    }
}
