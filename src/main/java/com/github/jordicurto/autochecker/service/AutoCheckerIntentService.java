package com.github.jordicurto.autochecker.service;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.geofence.AutoCheckerGeofencingClient;
import com.github.jordicurto.autochecker.location.AutoCheckerLocationSettingsClient;
import com.github.jordicurto.autochecker.manager.AutoCheckerAlarmsManager;
import com.github.jordicurto.autochecker.manager.AutoCheckerBusinessManager;
import com.github.jordicurto.autochecker.manager.AutoCheckerNotificationManager;
import com.github.jordicurto.autochecker.manager.AutoCheckerTransitionManager;
import com.github.jordicurto.autochecker.receiver.AutoCheckerBroadcastReceiver;
import com.github.jordicurto.autochecker.util.DateUtils;
import com.github.jordicurto.autochecker.util.LocationUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;


public class AutoCheckerIntentService extends JobIntentService {

    private static final int JOB_ID = 1;
    private final String TAG = getClass().getSimpleName();

    // managers
    private AutoCheckerTransitionManager mAutoCheckerTransitionManager = null;
    private AutoCheckerAlarmsManager mAutoCheckerAlarmsManager = null;
    private AutoCheckerNotificationManager mAutoCheckerNotificationManager = null;
    private AutoCheckerGeofencingClient mAutoCheckerGeofencingClient = null;
    private AutoCheckerLocationSettingsClient mAutoCheckerLocationSettingsClient = null;

    @NonNull
    private static Intent createIntent(Context context, String action) {
        return new Intent(context, AutoCheckerIntentService.class).setAction(action);
    }

    public static void enqueueWork(Context context, String action) {
        enqueueWork(context, createIntent(context, action));
    }

    public static void enqueueWork(Context context, String action, Bundle bundle) {
        Intent intent = createIntent(context, action);
        intent.putExtras(bundle);
        enqueueWork(context, intent);
    }

    public static void enqueueWork(Context context, String action, String param, long value) {
        Intent intent = createIntent(context, action);
        intent.putExtra(param, value);
        enqueueWork(context, intent);
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AutoCheckerIntentService.class, JOB_ID, work);
    }

    private void initializeManagers(Context context) {
        if (mAutoCheckerTransitionManager == null)
            mAutoCheckerTransitionManager = new AutoCheckerTransitionManager(context);
        if (mAutoCheckerAlarmsManager == null)
            mAutoCheckerAlarmsManager = new AutoCheckerAlarmsManager(context);
        if (mAutoCheckerNotificationManager == null)
            mAutoCheckerNotificationManager = new AutoCheckerNotificationManager(context);
        if (mAutoCheckerGeofencingClient == null)
            mAutoCheckerGeofencingClient = new AutoCheckerGeofencingClient(context);
        if (mAutoCheckerLocationSettingsClient == null)
            mAutoCheckerLocationSettingsClient = new AutoCheckerLocationSettingsClient(context);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        initializeManagers(this);

        String action = intent.getAction();

        if (action != null) {

            Log.d(TAG, "Intent received " + action);

            switch (action) {
                case AutoCheckerConstants.INTENT_START_SERVICE:

                    housekeeping();
                    checkPermission();
                    registerReceiver();

                    break;
                case AutoCheckerConstants.INTENT_PERMISSION_GRANTED:
                case AutoCheckerConstants.INTENT_REQUEST_CHECK_LOCATION:

                    mAutoCheckerLocationSettingsClient.checkLocationSettings();

                    break;
                case AutoCheckerConstants.INTENT_REQUEST_REGISTER_GEOFENCES:

                    registerGeofences();

                    break;
                case AutoCheckerConstants.INTENT_SYSTEM_SHUTDOWN:

                    leaveAndUnregisterGeofences();

                    break;
                case AutoCheckerConstants.GEOFENCE_TRANSITION_RECEIVED:

                    handleTransition(intent);

                    break;
                case AutoCheckerConstants.GEOFENCE_TRANSITION_CONFIRM_RECEIVED:

                    handleConfirmTransition(intent);

                    break;
                case AutoCheckerConstants.INTENT_FORCE_LEAVE_LOCATION:

                    handleForceLeaveLocation(intent);
                    break;

                case AutoCheckerConstants.ALARM_FORCE_LEAVE_LOCATION:
                case AutoCheckerConstants.INTENT_CANCEL_LEAVE_LOCATION:

                    handleCancelLeaveLocation();
                    break;

                case AutoCheckerConstants.ALARM_NOTIFICATION_DURATION:

                    Log.e(TAG, "Unimplemented");

                    break;
                default:

                    Log.e(TAG, "Unmatched intent");
                    break;
            }
        }
    }

    private void registerReceiver() {

        AutoCheckerBroadcastReceiver.registerReceiver(getApplicationContext());
        Log.d(TAG, "Broadcast Receiver registered");
    }

    private void handleCancelLeaveLocation() {

        Log.d(TAG, "Forced to enter location");

        AutoCheckerBusinessManager.getManager(this).
                unForceLeaveLocations(DateUtils.getCurrentDate());

        mAutoCheckerAlarmsManager.
                cancelAlarmForceLeaveLocation();

        sendBroadcast(
                new Intent(AutoCheckerConstants.INTENT_ACTIVITY_RELOAD_REQUEST));
    }

    private void handleForceLeaveLocation(Intent intent) {

        long duration = intent.getLongExtra(
                AutoCheckerConstants.INTENT_FORCE_LEAVE_LOCATION_EXTRA_DURATION, 0L);

        if (duration > 0L) {

            Log.d(TAG, "Forced to leave location for " + duration + " ms");

            AutoCheckerBusinessManager.getManager(this).
                    leaveCurrentLocations(DateUtils.getCurrentDate(), true);

            mAutoCheckerAlarmsManager.
                    setAlarmForceLeaveLocation(duration);

            sendBroadcast(
                    new Intent(AutoCheckerConstants.INTENT_ACTIVITY_RELOAD_REQUEST));
        }
    }

    private void housekeeping() {

        AutoCheckerBusinessManager.getManager(this).cleanOldWatchedLocationRecords(
                AutoCheckerConstants.RECORDS_HOLD_DURATION);
    }

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            mAutoCheckerNotificationManager.notifyPermissionRequired(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        } else {
            mAutoCheckerLocationSettingsClient.checkLocationSettings();
        }
    }

    private void registerGeofences() {

        Log.d(TAG, "Creating and registering geofences ");

        List<WatchedLocation> list = AutoCheckerBusinessManager.getManager(this).getAllWatchedLocations();

        mAutoCheckerGeofencingClient.registerGeofences(list);
    }

    private void leaveAndUnregisterGeofences() {
        AutoCheckerBusinessManager.getManager(this).
                leaveCurrentLocations(DateUtils.getCurrentDate(), false);
        mAutoCheckerGeofencingClient.unregisterGeofences();
    }

    private void handleTransition(Intent intent) {

        Log.d(TAG, "Proximity alert received by geofencing ");

        long time = DateUtils.getCurrentDateMillis();

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event != null) {

            if (event.hasError()) {

                Log.e(TAG, "GeoFence Error: " +
                        AutoCheckerGeofencingClient.getErrorString(event.getErrorCode()));

                if (event.getErrorCode() == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                    // Location disabled
                    mAutoCheckerNotificationManager.notifyEnableLocationRequired();
                    leaveAndUnregisterGeofences();
                }

            } else {

                Location triggerLocation = event.getTriggeringLocation();

                for (Geofence fence : event.getTriggeringGeofences()) {

                    String locationName = AutoCheckerGeofencingClient.getLocationName(fence);

                    if (locationName != null) {
                        WatchedLocation location = AutoCheckerBusinessManager.getManager(this).
                                getWatchedLocation(locationName);

                        if (location != null && triggerLocation != null) {

                            long delay = calculateDelayForRegisterTransition(triggerLocation, location,
                                    event.getGeofenceTransition());

                            switch (event.getGeofenceTransition()) {
                                case Geofence.GEOFENCE_TRANSITION_ENTER:
                                    //case Geofence.GEOFENCE_TRANSITION_DWELL:
                                    if (location.getStatus() == WatchedLocation.OUTSIDE_LOCATION) {
                                        mAutoCheckerTransitionManager.
                                                scheduleRegisterTransition(location, time,
                                                        AutoCheckerTransitionManager.ENTER_TRANSITION, delay);
                                    } else {
                                        mAutoCheckerTransitionManager.
                                                cancelScheduledRegisterTransition();
                                    }
                                    break;
                                case Geofence.GEOFENCE_TRANSITION_EXIT:
                                    if (location.getStatus() == WatchedLocation.INSIDE_LOCATION) {
                                        mAutoCheckerTransitionManager.
                                                scheduleRegisterTransition(location, time,
                                                        AutoCheckerTransitionManager.LEAVE_TRANSITION, delay);
                                    } else {
                                        mAutoCheckerTransitionManager.
                                                cancelScheduledRegisterTransition();
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleConfirmTransition(Intent intent) {

        Log.d(TAG, "Transition confirmed ");

        WatchedLocation location = AutoCheckerBusinessManager.getManager(this).
                getWatchedLocation(intent.getIntExtra(AutoCheckerConstants.LOCATION_ID, 0));
        long time = intent.getLongExtra(AutoCheckerConstants.TRANSITION_TIME, 0);
        int direction = intent.getIntExtra(AutoCheckerConstants.TRANSITION_DIRECTION,
                AutoCheckerTransitionManager.LEAVE_TRANSITION);

        mAutoCheckerTransitionManager.
                registerTransition(location, time, direction);

        mAutoCheckerNotificationManager.notifyTransition(location, time);
    }

    private long calculateDelayForRegisterTransition(Location triggerLocation, WatchedLocation wLocation,
                                                     int transition) {

        Location location = LocationUtils.getLocationFromWatchedLocation(wLocation);

        float distance = location.distanceTo(triggerLocation);

        boolean intersect = distance < (wLocation.getRadius() + triggerLocation.getAccuracy());

        boolean locInsideTriggerLoc = (distance + wLocation.getRadius()) < triggerLocation.getAccuracy();

        switch (transition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                //case Geofence.GEOFENCE_TRANSITION_DWELL:

                return AutoCheckerConstants.DELAY_FOR_STANDARD_TRANSITION;

            case Geofence.GEOFENCE_TRANSITION_EXIT:

                if (intersect && locInsideTriggerLoc) {

                    Log.d(TAG,
                            "Location is inside trigger location. Maybe accuracy has changed because location source has changed");

                    return AutoCheckerConstants.DELAY_FOR_SUSPECT_TRANSITION;

                } else if (intersect) {

                    Log.d(TAG,
                            "Location and trigger location intersects. Maybe accuracy has changed because location source has changed");

                    return AutoCheckerConstants.DELAY_FOR_SUSPECT_TRANSITION;

                } else {

                    return AutoCheckerConstants.DELAY_FOR_STANDARD_TRANSITION;
                }

            default:
                return AutoCheckerConstants.INVALID_DELAY;
        }
    }

}
