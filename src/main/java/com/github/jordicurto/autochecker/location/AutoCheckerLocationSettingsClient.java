package com.github.jordicurto.autochecker.location;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.manager.AutoCheckerNotificationManager;
import com.github.jordicurto.autochecker.receiver.AutoCheckerGeofencingReceiver;
import com.github.jordicurto.autochecker.util.ContextKeeper;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by jordi on 20/11/17.
 */

public class AutoCheckerLocationSettingsClient extends ContextKeeper {

    private final String TAG = getClass().getSimpleName();

    private final SettingsClient mSettingsClient;

    private final LocationRequest mLocationRequest = new LocationRequest();

    private AutoCheckerNotificationManager mAutoCheckerNotificationManager;

    public AutoCheckerLocationSettingsClient(Context context) {
        super(context);
        mSettingsClient = LocationServices.getSettingsClient(context);
        mAutoCheckerNotificationManager = new AutoCheckerNotificationManager(context);
    }

    public void checkLocationSettings() {
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(AutoCheckerConstants.INTERVAL_BETWEEN_LOCATION_UPDATES);
        mLocationRequest.setFastestInterval(
                AutoCheckerConstants.FASTEST_INTERVAL_BETWEEN_LOCATION_UPDATES);

        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest).build();

        Task<LocationSettingsResponse> task = mSettingsClient.checkLocationSettings(request);

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.i(TAG, "Location settings are OK");
                getContext().sendBroadcast(AutoCheckerGeofencingReceiver.createIntent(getContext(),
                        AutoCheckerConstants.INTENT_REQUEST_REGISTER_GEOFENCES));
                mAutoCheckerNotificationManager.cancelNotification(
                        AutoCheckerConstants.NOTIFICATION_ENABLE_LOCATION);
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Location settings are not OK");
                int statusCode = ((ApiException) e).getStatusCode();
                if (statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                    mAutoCheckerNotificationManager.notifyEnableLocationRequired();
                }
            }
        });
    }
}
