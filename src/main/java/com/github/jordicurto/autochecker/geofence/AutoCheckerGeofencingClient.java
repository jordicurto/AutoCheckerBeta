package com.github.jordicurto.autochecker.geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.manager.AutoCheckerNotificationManager;
import com.github.jordicurto.autochecker.util.ContextKeeper;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordi on 20/11/17.
 */

public class AutoCheckerGeofencingClient extends ContextKeeper {

    private static final String AUTOCHECKER_GEOFENCE_REQ_ID = "AUTOCHECKER_GEOFENCE_";

    public final String TAG = getClass().getSimpleName();

    private GeofencingClient mGeofencingClient = null;
    private PendingIntent mGeofencePendingIntent = null;
    private List<Geofence> geofencesToAdd = new ArrayList<>();

    private static AutoCheckerGeofencingClient mInstance;

    private AutoCheckerGeofencingClient(Context context) {
        super(context);
        mGeofencingClient = LocationServices.getGeofencingClient(getContext());
    }

    @NonNull
    private Geofence createGeofence(WatchedLocation location) {
        return new Geofence.Builder()
                .setCircularRegion(location.getLatitude(), location.getLongitude(),
                        location.getRadius())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setRequestId(AUTOCHECKER_GEOFENCE_REQ_ID + location.getName()).build();
    }

    private PendingIntent createRequestPendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        } else {
            return PendingIntent.getBroadcast(getContext(), 0,
                    new Intent(AutoCheckerConstants.GEOFENCE_TRANSITION_RECEIVED),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private void updateGeofences(List<WatchedLocation> list) {
        geofencesToAdd.clear();
        for (WatchedLocation location : list)
            geofencesToAdd.add(createGeofence(location));
    }

    public void registerGeofences(List<WatchedLocation> watchedLocationList) {

        updateGeofences(watchedLocationList);

        mGeofencePendingIntent = createRequestPendingIntent();

        GeofencingRequest request = new GeofencingRequest.Builder().
                addGeofences(geofencesToAdd).
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).build();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Task<Void> task = mGeofencingClient.addGeofences(request, mGeofencePendingIntent);

            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    AutoCheckerNotificationManager.getInstance(getContext()).notifyRegisteredGeofence();
                    Log.i(TAG, "Registering successful");
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int errorCode = ((ApiException)e).getStatusCode();

                    Log.e(TAG, "Registering failed: " + getErrorString(errorCode));

                    if (errorCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                        AutoCheckerNotificationManager.getInstance(getContext()).
                                notifyEnableLocationRequired();
                    }
                }
            });
        }
    }

    public void unregisterGeofences() {
        mGeofencePendingIntent = createRequestPendingIntent();
        mGeofencingClient.removeGeofences(mGeofencePendingIntent);
    }

    public static String getLocationName(Geofence fence) {
        String reqId = fence.getRequestId();
        if (reqId.length() <= AUTOCHECKER_GEOFENCE_REQ_ID.length())
            return null;
        else
            return reqId.substring(AUTOCHECKER_GEOFENCE_REQ_ID.length());
    }

    /**
     * Returns the error string for a geofencing error code.
     */
    public static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence is not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error code: " + errorCode;
        }
    }

    public static AutoCheckerGeofencingClient getInstance(Context context) {
        if (mInstance == null)
            mInstance = new AutoCheckerGeofencingClient(context);
        return mInstance;
    }

}
