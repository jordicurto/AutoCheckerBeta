package com.github.jordicurto.autochecker.manager;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import com.github.jordicurto.autochecker.data.AutoCheckerDataSource;
import com.github.jordicurto.autochecker.data.exception.NoLocationFoundException;
import com.github.jordicurto.autochecker.data.exception.NoRecordFoundException;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordi on 24/10/16.
 */

public class AutoCheckerBusinessManager {

    private final String TAG = getClass().getSimpleName();

    private AutoCheckerDataSource dataSource;

    private static AutoCheckerBusinessManager instance = null;

    public static AutoCheckerBusinessManager getManager(Context context) {
        if (instance == null)
            instance = new AutoCheckerBusinessManager(context);
        return instance;
    }

    private AutoCheckerBusinessManager(Context context) {
        dataSource = new AutoCheckerDataSource(context);
    }

    public List<WatchedLocation> getAllWatchedLocations() {

        List<WatchedLocation> list = new ArrayList<>();

        try {

            dataSource.open();
            list = dataSource.getAllWatchedLocations();
            dataSource.close();

        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }

        return list;
    }

    public WatchedLocation getWatchedLocation(int locationId) {

        WatchedLocation location = null;
        try {

            dataSource.open();
            location = dataSource.getWatchedLocation(locationId);
            dataSource.close();

        } catch (NoLocationFoundException e) {
            Log.e(TAG, "Location not found exception", e);
        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }

        return location;
    }


    public WatchedLocation getWatchedLocation(String locationName) {

        WatchedLocation location = null;
        try {

            dataSource.open();
            location = dataSource.getWatchedLocation(locationName);
            dataSource.close();

        } catch (NoLocationFoundException e) {
            Log.e(TAG, "Location not found exception", e);
        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }

        return location;
    }

    public boolean existsWatchedLocation(String locationName) {
        return (getWatchedLocation(locationName) != null);
    }

    public WatchedLocationRecord createCheckInRecord(WatchedLocation location,
                                                     LocalDateTime checkIn,
                                                     boolean forced) {

        WatchedLocationRecord record = new WatchedLocationRecord();

        try {

            dataSource.open();

            switch (location.getStatus()) {

                case WatchedLocation.OUTSIDE_LOCATION:
                case WatchedLocation.FORCED_OUTSIDE_LOCATION:

                    location.setStatus(WatchedLocation.INSIDE_LOCATION);
                    dataSource.updateWatchedLocation(location);

                    record.setCheckIn(checkIn);
                    record.setCheckOut(null);
                    record.setLocation(location);
                    dataSource.insertRecord(record);

                    if (forced) {
                        Log.i(TAG, "Forced enter event to " + location.getName());
                    } else {
                        Log.i(TAG, "User has entered to " + location.getName());
                    }

                    break;

                case WatchedLocation.INSIDE_LOCATION:

                    Log.w(TAG, "User has entered to " + location.getName() +
                            " but he/she was there yet");
                    break;
            }

            dataSource.close();

        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }

        return record;
    }

    public WatchedLocationRecord updateCheckOutRecord(WatchedLocation location,
                                                      LocalDateTime checkOut,
                                                      boolean forced) {

        WatchedLocationRecord record = new WatchedLocationRecord();

        try {

            dataSource.open();

            switch (location.getStatus()) {

                case WatchedLocation.INSIDE_LOCATION:

                    location.setStatus(forced ? WatchedLocation.FORCED_OUTSIDE_LOCATION :
                            WatchedLocation.OUTSIDE_LOCATION);
                    dataSource.updateWatchedLocation(location);

                    record = dataSource.getUnCheckedWatchedLocationRecord(location);
                    record.setCheckOut(checkOut);

                    if (record.isValid()) {
                        dataSource.updateRecord(record);
                        if (forced) {
                            Log.i(TAG, "Forced leave event from " + location.getName());
                        } else {
                            Log.i(TAG, "User has left " + location.getName());
                        }
                    } else {
                        Log.e(TAG, "Trying to update record with check out before check in. Record deleted");
                        dataSource.removeRecord(record);
                    }

                    break;

                case WatchedLocation.FORCED_OUTSIDE_LOCATION:

                    location.setStatus(WatchedLocation.OUTSIDE_LOCATION);
                    dataSource.updateWatchedLocation(location);

                    Log.i(TAG, "User has left " + location.getName());

                    break;

                case WatchedLocation.OUTSIDE_LOCATION:

                    Log.w(TAG, "User has leaving " + location.getName() +
                            " but he/she wasn't there yet");
                    break;
            }

            dataSource.close();

        } catch (NoRecordFoundException e) {
            Log.e(TAG, "Record not found exception", e);
        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }

        return record;
    }

    public List<Interval> getDateIntervals(
            WatchedLocation location, DateUtils.INTERVAL_TYPE intervalType,
            int startDayHour) {

        List<Interval> intervals = new ArrayList<>();

        try {

            dataSource.open();

            intervals = DateUtils.getDateIntervals(
                    dataSource.getLimitDates(location).getStart().toLocalDate(),
                    DateUtils.getCurrentDate().minusHours(startDayHour).toLocalDate(),
                    intervalType);

            dataSource.close();

        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }

        return intervals;
    }

    public void insertWatchedLocation(WatchedLocation location) {

        try {

            dataSource.open();
            dataSource.insertWatchedLocation(location);
            dataSource.close();

        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }

    }

    public List<WatchedLocationRecord> getIntervalWatchedLocationRecord(
            WatchedLocation location, Interval interval, int startHourDay) {

        List<WatchedLocationRecord> records = new ArrayList<>();

        try {

            dataSource.open();

            records = dataSource.getIntervalWatchedLocationRecord(location, interval, startHourDay);

            if (location.isInside()) {
                WatchedLocationRecord uncheckedRecord =
                        dataSource.getUnCheckedWatchedLocationRecord(location);
                if (uncheckedRecord.getCheckIn().toDateTime().isBefore(
                        interval.getStart().plusHours(startHourDay))) {
                    records.add(uncheckedRecord);
                }
            }

            dataSource.close();

        } catch (NoRecordFoundException e) {
            Log.e(TAG, "No unchecked record is found", e);
        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }

        return records;
    }

    public void leaveCurrentLocations(LocalDateTime checkOut, boolean forced) {

        try {

            dataSource.open();
            List<WatchedLocation> locations = dataSource.getAllWatchedLocations();
            for (WatchedLocation location : locations) {
                if (location.isInside() || location.isForcedOutside())
                    updateCheckOutRecord(location, checkOut, forced);
            }
            dataSource.close();

        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }
    }

    public void unForceLeaveLocations(LocalDateTime checkIn) {

        try {

            dataSource.open();
            List<WatchedLocation> locations = dataSource.getAllWatchedLocations();
            for (WatchedLocation location : locations) {
                if (location.isForcedOutside())
                    createCheckInRecord(location, checkIn, true);
            }
            dataSource.close();

        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }
    }

    public void cleanOldWatchedLocationRecords(Duration recordsToHold) {

        try {

            dataSource.open();
            int deletedRows = dataSource.removeRecordsToDate(
                    DateUtils.getCurrentDate().minus(recordsToHold));
            dataSource.close();

            if (deletedRows > 0)
                Log.d(TAG, "Clearing old records: Removed " + deletedRows + " rows");

        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }
    }
}
