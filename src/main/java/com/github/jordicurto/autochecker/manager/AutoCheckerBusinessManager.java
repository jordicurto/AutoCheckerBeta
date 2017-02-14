package com.github.jordicurto.autochecker.manager;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;
import android.util.Pair;

import com.github.jordicurto.autochecker.data.AutoCheckerDataSource;
import com.github.jordicurto.autochecker.data.exception.NoLocationFoundException;
import com.github.jordicurto.autochecker.data.exception.NoRecordFoundException;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jordi on 24/10/16.
 */

public class AutoCheckerBusinessManager {

    private final String TAG = getClass().getSimpleName();

    private AutoCheckerDataSource dataSource = null;

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
            Log.e(TAG, "Location not found execption", e);
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
            Log.e(TAG, "Location not found execption", e);
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

    public WatchedLocationRecord createCheckInRecord(WatchedLocation location, Date checkIn) {

        WatchedLocationRecord record = new WatchedLocationRecord();

        try {

            dataSource.open();

            switch (location.getStatus()) {

                case WatchedLocation.OUTSIDE_LOCATION:

                    location.setStatus(WatchedLocation.INSIDE_LOCATION);
                    dataSource.updateWatchedLocation(location);

                    record.setCheckIn(checkIn);
                    record.setCheckOut(null);
                    record.setLocation(location);
                    dataSource.insertRecord(record);

                    Log.i(TAG, "User has entered to " + location.getName());

                    break;

                case WatchedLocation.INSIDE_LOCATION:

                    Log.w(TAG, "User has entered to " + location.getName() + " but he/she was there yet");
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

    public WatchedLocationRecord updateCheckOutRecord(WatchedLocation location, Date checkOut) {

        WatchedLocationRecord record = new WatchedLocationRecord();

        try {

            dataSource.open();

            switch (location.getStatus()) {

                case WatchedLocation.INSIDE_LOCATION:

                    location.setStatus(WatchedLocation.OUTSIDE_LOCATION);
                    dataSource.updateWatchedLocation(location);

                    record = dataSource.getUnCheckedWatchedLocationRecord(location);
                    record.setCheckOut(checkOut);
                    dataSource.updateRecord(record);

                    Log.i(TAG, "User has left " + location.getName());

                    break;

                case WatchedLocation.OUTSIDE_LOCATION:

                    Log.w(TAG, "User has leaving " + location.getName() + " but he/she wasn't there yet");
                    break;
            }

            dataSource.close();

        } catch (NoRecordFoundException e) {
            Log.e(TAG, "Record not found execption", e);
        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }

        return record;
    }

    public List<Date> getDateIntervals(WatchedLocation location, int startDayHour, int intervalType) {

        List<Date> intervals = new ArrayList<>();

        try {

            dataSource.open();

            Pair<Date, Date> limits = dataSource.getLimitDates(location);

            if (limits.first != null) {

                intervals = DateUtils.getDateIntervals(limits.first, DateUtils.getCurrentDate(),
                        -startDayHour, intervalType);

            }

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

    public List<WatchedLocationRecord> getIntervalWatchedLocationRecord(WatchedLocation location,
                                                                        Date start, Date end) {

        List<WatchedLocationRecord> records = new ArrayList<WatchedLocationRecord>();

        try {

            dataSource.open();
            records = dataSource.getIntervalWatchedLocationRecord(location, start, end);
            dataSource.close();

        } catch (SQLException e) {
            Log.e(TAG, "DataSource exception", e);
        } finally {
            dataSource.close();
        }

        return records;
    }
}
