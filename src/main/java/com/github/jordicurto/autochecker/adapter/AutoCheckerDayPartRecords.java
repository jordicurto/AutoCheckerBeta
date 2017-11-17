package com.github.jordicurto.autochecker.adapter;

import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by jordi on 15/05/17.
 */

public class AutoCheckerDayPartRecords {

    private DateTime mWeekDayStart;

    private DateUtils.PART_OF_DAY mDayPart;
    private List<WatchedLocationRecord> mRecords;

    public AutoCheckerDayPartRecords(DateTime weekDayStart, DateUtils.PART_OF_DAY dayPart,
                                     List<WatchedLocationRecord> records) {
        mWeekDayStart = weekDayStart;
        mDayPart = dayPart;
        mRecords = records;
    }

    public DateTime getWeekDayStart() {
        return mWeekDayStart;
    }

    public DateUtils.PART_OF_DAY getDayPart() {
        return mDayPart;
    }

    public List<WatchedLocationRecord> getRecords() {
        return mRecords;
    }
}
