package com.github.jordicurto.autochecker.adapter.erecyclerview;

import com.bignerdranch.expandablerecyclerview.model.Parent;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordi on 22/05/16.
 */
public class AutoCheckerDayRecords implements Parent<AutoCheckerDayPartRecords> {

    private DateTime weekDay;
    private Duration duration;
    private List<WatchedLocationRecord> mRecords;

    private List<Interval> dayPartIntervals;

    private List<AutoCheckerDayPartRecords> children = new ArrayList<>();

    public AutoCheckerDayRecords(Interval interval, int startHourDay,
                                 List<WatchedLocationRecord> records) {

        mRecords = records;
        weekDay = interval.getStart().plusHours(startHourDay).toDateTime();
        duration = DateUtils.calculateDuration(records, interval, startHourDay);
        dayPartIntervals = new ArrayList<>();
        initDayPartIntervals();
        createChildren();
    }

    private void initDayPartIntervals() {

        DateTime startDate = weekDay;
        DateTime endDate = startDate.plusHours(AutoCheckerConstants.HOURS_BETWEEN_DAY_PART);
        for (DateUtils.PART_OF_DAY dayPart : DateUtils.PART_OF_DAY.values()) {
            dayPartIntervals.add(dayPart.getIndex(), new Interval(startDate, endDate));
            startDate = endDate;
            endDate = endDate.plusHours(AutoCheckerConstants.HOURS_BETWEEN_DAY_PART);
        }

    }

    private void createChildren() {

        LocalDateTime now = DateUtils.getCurrentDate();
        for (DateUtils.PART_OF_DAY dayPart : DateUtils.PART_OF_DAY.values()) {
            List<WatchedLocationRecord> recordsDayPart = new ArrayList<>();
            Interval interval = dayPartIntervals.get(dayPart.getIndex());
            LocalDateTime start = interval.getStart().toLocalDateTime();
            LocalDateTime end = interval.getEnd().toLocalDateTime();
            for (WatchedLocationRecord record : mRecords) {
                LocalDateTime checkIn = record.getCheckIn();
                LocalDateTime checkOut = record.isActive() ? now : record.getCheckOut();
                if (start.isBefore(checkOut) && end.isAfter(checkIn)) {
                    recordsDayPart.add(record);
                }
            }
            if(!recordsDayPart.isEmpty())
                children.add(new AutoCheckerDayPartRecords(weekDay, dayPart, recordsDayPart));
        }
    }

    @Override
    public List<AutoCheckerDayPartRecords> getChildList() {
        return children;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    public DateTime getWeekDay() {
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
