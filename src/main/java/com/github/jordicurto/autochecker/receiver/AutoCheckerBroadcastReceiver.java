package com.github.jordicurto.autochecker.receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.geofence.AutoCheckerGeofencingRegisterer;
import com.github.jordicurto.autochecker.manager.AutoCheckerBusinessManager;
import com.github.jordicurto.autochecker.manager.AutoCheckerNotificationManager;
import com.github.jordicurto.autochecker.manager.AutoCheckerTransitionManager;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.util.DateUtils;
import com.github.jordicurto.autochecker.util.LocationUtils;
import com.github.jordicurto.autochecker.util.PermissionHelper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class AutoCheckerBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    private AutoCheckerGeofencingRegisterer geofencingRegisterer = null;
    private AutoCheckerTransitionManager transitionManager = null;
    private AutoCheckerNotificationManager notificationManager = null;

    public AutoCheckerBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Log.d(TAG, "Intent received " + action);

        if (geofencingRegisterer == null)
            geofencingRegisterer = new AutoCheckerGeofencingRegisterer(context);

        if (transitionManager == null)
            transitionManager = new AutoCheckerTransitionManager(context);

        if (notificationManager == null)
            notificationManager = new AutoCheckerNotificationManager(context);

        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals(AutoCheckerConstants.INTENT_REQUEST_REGISTER_GEOFENCES) ||
                action.equals(AutoCheckerConstants.INTENT_PERMISSION_GRANTED)) {

            registerGeofences(context);

        } else if (action.equals(AutoCheckerConstants.GEOFENCE_TRANSITION_RECEIVED)) {

            handleTransition(context, intent);

        } else if (action.equals(AutoCheckerConstants.GEOFENCE_TRANSITION_CONFIRM_RECEIVED)) {

            handleConfirmTransition(context, intent);

        } else {

            Log.e(TAG, "Unmatched intent");
        }
    }

    private void registerGeofences(Context context) {

        if (ContextCompat.checkSelfPermission
                (context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            PermissionHelper.requestPermissions(context,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0,
                    context.getString(R.string.request_permission_title),
                    context.getString(R.string.request_permission_text),
                    R.drawable.ic_request_permssion);

        } else {

            Log.d(TAG, "Creating and registering geofences ");

            List<WatchedLocation> list = AutoCheckerBusinessManager.getManager(context).getAllWatchedLocations();

            geofencingRegisterer.registerGeofences(list);

        }
    }

    private void handleTransition(Context context, Intent intent) {

        Log.d(TAG, "Proximity alert received by geofencing ");

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event != null) {

            if (event.hasError()) {

                Log.e(TAG, "GeoFence Error: " + AutoCheckerGeofencingRegisterer.getErrorString(event.getErrorCode()));

            } else {

                long time = DateUtils.getCurrentDateMillis();

                Location triggerLocation = event.getTriggeringLocation();

                for (Geofence fence : event.getTriggeringGeofences()) {

                    try {

                        int locationId = geofencingRegisterer.getLocationId(fence);

                        WatchedLocation location = AutoCheckerBusinessManager.getManager(context).getWatchedLocation(locationId);

                        if (location != null && triggerLocation != null) {

                            long delay = calculateDelayForRegisterTransition(triggerLocation, location,
                                    event.getGeofenceTransition());

                            switch (event.getGeofenceTransition()) {
                                case Geofence.GEOFENCE_TRANSITION_ENTER:
                                    if (location.getStatus() == WatchedLocation.OUTSIDE_LOCATION) {
                                        transitionManager.scheduleRegisterTransition(location, time,
                                                AutoCheckerTransitionManager.ENTER_TRANSITION, delay);
                                    } else {
                                        transitionManager.cancelScheduledRegisterTransition();
                                    }
                                    break;
                                case Geofence.GEOFENCE_TRANSITION_EXIT:
                                    if (location.getStatus() == WatchedLocation.INSIDE_LOCATION) {
                                        transitionManager.scheduleRegisterTransition(location, time,
                                                AutoCheckerTransitionManager.LEAVE_TRANSITION, delay);
                                    } else {
                                        transitionManager.cancelScheduledRegisterTransition();
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }

                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Can't get location id from : " + fence.getRequestId());
                    }
                }
            }
        }
    }

    private void handleConfirmTransition(Context context, Intent intent) {

        Log.d(TAG, "Transition confirmed ");

        WatchedLocation location = AutoCheckerBusinessManager.getManager(context).
                getWatchedLocation(intent.getIntExtra(AutoCheckerConstants.LOCATION_ID, 0));
        long time = intent.getLongExtra(AutoCheckerConstants.TRANSITION_TIME, 0);
        int direction = intent.getIntExtra(AutoCheckerConstants.TRANSITION_DIRECTION, AutoCheckerTransitionManager.LEAVE_TRANSITION);

        transitionManager.registerTransition(location, time, direction);

        notificationManager.notifyTransition(location, time);
    }

    private long calculateDelayForRegisterTransition(Location triggerLocation, WatchedLocation wLocation,
                                                     int transition) {

        Location location = LocationUtils.getLocationFromWatchedLocation(wLocation);

        float distance = location.distanceTo(triggerLocation);

        boolean intersect = distance < (wLocation.getRadius() + triggerLocation.getAccuracy());

        boolean locInsideTriggerLoc = (distance + wLocation.getRadius()) < triggerLocation.getAccuracy();

        switch (transition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:

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
