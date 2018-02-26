package com.github.jordicurto.autochecker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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

public class AutoCheckerDataSource {

	private final String TAG = getClass().getSimpleName();

	private SQLiteDatabase database;

	private AutoCheckerSQLiteOpenHelper dbHelper;

	public AutoCheckerDataSource(Context context) {
		dbHelper = new AutoCheckerSQLiteOpenHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	private void setWatchedLocationContents(Cursor cursor, WatchedLocation location) {

		location.setId(cursor.getInt(AutoCheckerSQLiteOpenHelper.COLUMN_ID_INDEX));
		location.setName(cursor.getString(AutoCheckerSQLiteOpenHelper.COLUMN_NAME_INDEX));
		location.setLatitude(cursor.getDouble(AutoCheckerSQLiteOpenHelper.COLUMN_LATITUDE_INDEX));
		location.setLongitude(cursor.getDouble(AutoCheckerSQLiteOpenHelper.COLUMN_LONGITUDE_INDEX));
		location.setRadius(cursor.getFloat(AutoCheckerSQLiteOpenHelper.COLUMN_RADIUS_INDEX));
		location.setStatus(cursor.getInt(AutoCheckerSQLiteOpenHelper.COLUMN_STATUS_INDEX));

	}

	private void setWatchedLocationRecordContents(Cursor cursor, WatchedLocationRecord record,
			WatchedLocation location) {

		record.setId(cursor.getInt(AutoCheckerSQLiteOpenHelper.COLUMN_ID_INDEX));
		record.setCheckIn(new LocalDateTime(cursor.getLong(AutoCheckerSQLiteOpenHelper.COLUMN_CHECKIN_INDEX)));
		if (cursor.isNull(AutoCheckerSQLiteOpenHelper.COLUMN_CHECKOUT_INDEX)) {
			record.setCheckOut(null);
		} else {
			record.setCheckOut(new LocalDateTime(cursor.getLong(AutoCheckerSQLiteOpenHelper.COLUMN_CHECKOUT_INDEX)));
		}
		record.setLocation(location);

	}

	private ContentValues watchedLocationContentValues(WatchedLocation location) {

		ContentValues values = new ContentValues();

		values.put(AutoCheckerSQLiteOpenHelper.COLUMN_NAME, location.getName());
		values.put(AutoCheckerSQLiteOpenHelper.COLUMN_LATITUDE, location.getLatitude());
		values.put(AutoCheckerSQLiteOpenHelper.COLUMN_LONGITUDE, location.getLongitude());
		values.put(AutoCheckerSQLiteOpenHelper.COLUMN_RADIUS, location.getRadius());
		values.put(AutoCheckerSQLiteOpenHelper.COLUMN_STATUS, location.getStatus());

		return values;
	}

	private ContentValues watchedLocationRecordContentValues(WatchedLocationRecord record) {

		ContentValues values = new ContentValues();

		values.put(AutoCheckerSQLiteOpenHelper.COLUMN_LOCATION_ID, record.getLocation().getId());
		values.put(AutoCheckerSQLiteOpenHelper.COLUMN_CHECKIN, record.getCheckIn().toDateTime().getMillis());
		if (record.getCheckOut() == null) {
			values.putNull(AutoCheckerSQLiteOpenHelper.COLUMN_CHECKOUT);
		} else {
			values.put(AutoCheckerSQLiteOpenHelper.COLUMN_CHECKOUT, record.getCheckOut().toDateTime().getMillis());
		}

		return values;
	}

	public void insertWatchedLocation(WatchedLocation location) {

		Cursor cursor = database.query(AutoCheckerSQLiteOpenHelper.TABLE_LOCATIONS_NAME,
				AutoCheckerSQLiteOpenHelper.COLUMNS_TABLE_LOCATIONS,
                AutoCheckerSQLiteOpenHelper.COLUMN_NAME + " = \"" + location.getName() + "\"",
                null, null, null, null);

		if (!cursor.moveToFirst()) {
			database.insert(AutoCheckerSQLiteOpenHelper.TABLE_LOCATIONS_NAME, null,
					watchedLocationContentValues(location));
		} else {
			Log.d(TAG, "Location exists " + location.toString());
		}

		cursor.close();
	}

	public WatchedLocation getWatchedLocation(int locationId) throws NoLocationFoundException {

		Cursor cursor = database.query(AutoCheckerSQLiteOpenHelper.TABLE_LOCATIONS_NAME,
				AutoCheckerSQLiteOpenHelper.COLUMNS_TABLE_LOCATIONS,
                AutoCheckerSQLiteOpenHelper.COLUMN_ID + " = " + locationId,
                null, null, null, null);

		WatchedLocation location = new WatchedLocation();

		if (cursor.moveToFirst()) {
			setWatchedLocationContents(cursor, location);
		} else {
			Log.w(TAG, "No watched location found " + locationId);
			cursor.close();
			throw new NoLocationFoundException(locationId);
		}

		cursor.close();

		return location;
	}

	public WatchedLocation getWatchedLocation(String locationName) throws NoLocationFoundException {

		Cursor cursor = database.query(AutoCheckerSQLiteOpenHelper.TABLE_LOCATIONS_NAME,
				AutoCheckerSQLiteOpenHelper.COLUMNS_TABLE_LOCATIONS,
                AutoCheckerSQLiteOpenHelper.COLUMN_NAME + " like '" + locationName + "'",
                null, null, null, null);

		WatchedLocation location = new WatchedLocation();

		if (cursor.moveToFirst()) {
			setWatchedLocationContents(cursor, location);
		} else {
			Log.w(TAG, "No watched location found " + locationName);
			cursor.close();
			throw new NoLocationFoundException(locationName);
		}

		cursor.close();

		return location;
	}

	public List<WatchedLocation> getAllWatchedLocations() {

		Cursor cursor = database.query(AutoCheckerSQLiteOpenHelper.TABLE_LOCATIONS_NAME,
				AutoCheckerSQLiteOpenHelper.COLUMNS_TABLE_LOCATIONS, null, null, null, null,
				AutoCheckerSQLiteOpenHelper.COLUMN_ID + " asc");

        List<WatchedLocation> locations = new ArrayList<>();

		while (cursor.moveToNext()) {
			WatchedLocation location = new WatchedLocation();
			setWatchedLocationContents(cursor, location);
			locations.add(location);
		}

		cursor.close();

		return locations;
	}

	public void updateWatchedLocation(WatchedLocation location) {

		database.update(AutoCheckerSQLiteOpenHelper.TABLE_LOCATIONS_NAME, watchedLocationContentValues(location),
				AutoCheckerSQLiteOpenHelper.COLUMN_ID + " = " + location.getId(), null);
	}

	public void insertRecord(WatchedLocationRecord record) {

		database.insert(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME, null,
				watchedLocationRecordContentValues(record));
	}

	public void updateRecord(WatchedLocationRecord record) {

		database.update(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME, watchedLocationRecordContentValues(record),
				"id = " + record.getId(), null);
	}

	public WatchedLocationRecord getUnCheckedWatchedLocationRecord(WatchedLocation location)
			throws NoRecordFoundException {

		Cursor cursor = database.query(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME,
				AutoCheckerSQLiteOpenHelper.COLUMNS_TABLE_RECORDS, AutoCheckerSQLiteOpenHelper.COLUMN_LOCATION_ID
						+ " = " + location.getId() + " and " + AutoCheckerSQLiteOpenHelper.COLUMN_CHECKOUT + " is null",
				null, null, null, null);

		WatchedLocationRecord record = new WatchedLocationRecord();

		if (cursor.moveToFirst()) {
			setWatchedLocationRecordContents(cursor, record, location);
		} else {
			Log.w(TAG, "No opened check in found for " + location.toString());
			cursor.close();
			throw new NoRecordFoundException(location.getName());
		}

		cursor.close();

		return record;
	}

	public List<WatchedLocationRecord> getAllWatchedLocationRecord(WatchedLocation location) {

		Cursor cursor = database.query(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME,
				AutoCheckerSQLiteOpenHelper.COLUMNS_TABLE_RECORDS,
                AutoCheckerSQLiteOpenHelper.COLUMN_LOCATION_ID + " = " + location.getId(),
                null, null, null, null);

        List<WatchedLocationRecord> records = new ArrayList<>();

		while (cursor.moveToNext()) {
			WatchedLocationRecord record = new WatchedLocationRecord();
			setWatchedLocationRecordContents(cursor, record, location);
			records.add(record);
		}

		cursor.close();

		return records;
	}

	public List<WatchedLocationRecord> getIntervalWatchedLocationRecord(WatchedLocation location,
																		Interval interval,
                                                                        int startHourDay) {

        long start = interval.getStart().plusHours(startHourDay).getMillis();
        long end = interval.getEnd().plusHours(startHourDay).getMillis();

		Cursor cursor = database.query(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME,
				AutoCheckerSQLiteOpenHelper.COLUMNS_TABLE_RECORDS,
				AutoCheckerSQLiteOpenHelper.COLUMN_LOCATION_ID + " = " + location.getId() + " and "
						+ AutoCheckerSQLiteOpenHelper.COLUMN_CHECKIN + " between "
                        + start + " and " + end + " or "
                        + AutoCheckerSQLiteOpenHelper.COLUMN_CHECKOUT + " between "
                        + start + " and " + end + " or "
                        + AutoCheckerSQLiteOpenHelper.COLUMN_CHECKIN + " <= " + start + " and "
                        + AutoCheckerSQLiteOpenHelper.COLUMN_CHECKOUT + " >= " + end,
                null, null, null, AutoCheckerSQLiteOpenHelper.COLUMN_CHECKIN + " asc");

        List<WatchedLocationRecord> records = new ArrayList<>();

		while (cursor.moveToNext()) {
			WatchedLocationRecord record = new WatchedLocationRecord();
			setWatchedLocationRecordContents(cursor, record, location);
			if (record.isValid())
				records.add(record);
		}

		cursor.close();

		return records;
	}

	public Interval getLimitDates(WatchedLocation location) {

		Interval interval = new Interval(DateUtils.getCurrentDate().toDateTime(),
				Duration.millis(0));

		Cursor cursor = database.query(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME,
				AutoCheckerSQLiteOpenHelper.COLUMNS_TABLE_RECORDS,
                AutoCheckerSQLiteOpenHelper.COLUMN_LOCATION_ID + " = " + location.getId(),
                null, null, null,
                AutoCheckerSQLiteOpenHelper.COLUMN_CHECKIN + " asc");

		WatchedLocationRecord firstRecord = new WatchedLocationRecord();
		WatchedLocationRecord lastRecord = new WatchedLocationRecord();

		if (cursor.moveToFirst()) {
			setWatchedLocationRecordContents(cursor, firstRecord, location);
			cursor.moveToLast();
			setWatchedLocationRecordContents(cursor, lastRecord, location);
			interval = new Interval(firstRecord.getCheckIn().toDateTime(),
					lastRecord.getCheckOut() != null ? lastRecord.getCheckOut().toDateTime()
							: DateUtils.getCurrentDate().toDateTime()
			);
		} else {
			Log.d(TAG, "No records found for " + location.toString());
		}

		cursor.close();

		return interval;
	}

	public boolean isUserInWatchedLocation(WatchedLocation location) {

		Cursor cursor = database.query(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME,
				AutoCheckerSQLiteOpenHelper.COLUMNS_TABLE_RECORDS, AutoCheckerSQLiteOpenHelper.COLUMN_LOCATION_ID
						+ " = " + location.getId() + " and " + AutoCheckerSQLiteOpenHelper.COLUMN_CHECKOUT + " is null",
				null, null, null, null);

		return (cursor.getCount() > 0);
	}

	public WatchedLocationRecord getLastWatchedLocationRecord(WatchedLocation location) throws NoRecordFoundException {

		Cursor cursor = database.query(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME,
				AutoCheckerSQLiteOpenHelper.COLUMNS_TABLE_RECORDS,
                AutoCheckerSQLiteOpenHelper.COLUMN_LOCATION_ID + " = " + location.getId(),
                null, null, null,
                AutoCheckerSQLiteOpenHelper.COLUMN_CHECKOUT + " desc");

		WatchedLocationRecord record = new WatchedLocationRecord();

		if (cursor.moveToFirst()) {
			setWatchedLocationRecordContents(cursor, record, location);
		} else {
			Log.w(TAG, "No checks found for " + location.toString());
			cursor.close();
			throw new NoRecordFoundException(location.getName());
		}

		cursor.close();

		return record;
	}

    public void removeLastWatchedLocationRecord(WatchedLocation location) throws NoRecordFoundException {

        WatchedLocationRecord record = getUnCheckedWatchedLocationRecord(location);
        database.delete(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME,
                AutoCheckerSQLiteOpenHelper.COLUMN_ID + " = " + record.getId(), null);
    }

    public int removeRecordsToDate(LocalDateTime limitDate) {

        return database.delete(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME,
                AutoCheckerSQLiteOpenHelper.COLUMN_CHECKOUT + " <= "
                        + limitDate.toDateTime().getMillis(), null);
    }

    public int removeFutureRecords() {

        return database.delete(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME,
                AutoCheckerSQLiteOpenHelper.COLUMN_CHECKIN + " > " +
                        DateUtils.getCurrentDate().toDateTime().getMillis(), null);
    }

    public void removeRecord(WatchedLocationRecord record) {

        database.delete(AutoCheckerSQLiteOpenHelper.TABLE_RECORDS_NAME,
                AutoCheckerSQLiteOpenHelper.COLUMN_ID + " = " + record.getId(), null);
    }
}
