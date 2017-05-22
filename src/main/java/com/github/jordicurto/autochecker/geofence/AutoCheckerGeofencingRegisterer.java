package com.github.jordicurto.autochecker.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.jordicurto.autochecker.activity.AutoCheckerMainActivity;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.manager.AutoCheckerNotificationManager;
import com.github.jordicurto.autochecker.util.ContextKeeper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class AutoCheckerGeofencingRegisterer extends ContextKeeper
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String AUTOCHECKER_GEOFENCE_REQ_ID = "AUTOCHECKER_GEOFENCE_";

    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private List<Geofence> geofencesToAdd = new ArrayList<>();

    private AutoCheckerNotificationManager nManager;

    public final String TAG = getClass().getSimpleName();

    public  AutoCheckerGeofencingRegisterer(Context context) {
        super(context);
    }

    private Geofence createGeofence(WatchedLocation location) {
        return new Geofence.Builder()
                .setCircularRegion(location.getLatitude(), location.getLongitude(), location.getRadius())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setRequestId(AUTOCHECKER_GEOFENCE_REQ_ID + location.getId()).build();
    }

    private void updateGeofences(List<WatchedLocation> list) {
        geofencesToAdd.clear();
        for (WatchedLocation location : list)
            geofencesToAdd.add(createGeofence(location));
    }

    public void registerGeofences(List<WatchedLocation> watchedLocationList) {

        updateGeofences(watchedLocationList);
        mGoogleApiClient = new GoogleApiClient.Builder(mContext).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        try {
            mGeofencePendingIntent = createRequestPendingIntent();
            GeofencingRequest request = new GeofencingRequest.Builder().addGeofences(geofencesToAdd).build();
            final PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, request,
                    mGeofencePendingIntent);
            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        if (nManager == null)
                            nManager = new AutoCheckerNotificationManager(getContext());
                        nManager.notifyRegisteredGeofence();
                        Log.i(TAG, "Registering successful");
                    } else {
                        Log.e(TAG, "Registering failed: " + getErrorString(status.getStatusCode()));
                    }
                    mGoogleApiClient.disconnect();
                }
            });

        } catch (SecurityException ex) {
            Log.e(TAG, "Permission not granted ", ex);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorCode());
    }

    /**
     * Get a PendingIntent to send with the request to add Geofences. Location
     * Services issues the Intent inside this PendingIntent whenever a geofence
     * transition occurs for the current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence
     * transitions.
     */
    private PendingIntent createRequestPendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        } else {
            return PendingIntent.getBroadcast(mContext, 0,
                    new Intent(AutoCheckerConstants.GEOFENCE_TRANSITION_RECEIVED), PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    public int getLocationId(Geofence fence) throws NumberFormatException {
        return Integer.parseInt(fence.getRequestId().substring(AUTOCHECKER_GEOFENCE_REQ_ID.length()));
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
                return "Unknow error code: " + errorCode;
        }
    }
}
