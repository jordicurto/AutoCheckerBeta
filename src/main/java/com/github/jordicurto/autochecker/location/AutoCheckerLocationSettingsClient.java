package com.github.jordicurto.autochecker.location;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.manager.AutoCheckerNotificationManager;
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

    public final String TAG = getClass().getSimpleName();

    private final SettingsClient mSettingsClient;

    private static AutoCheckerLocationSettingsClient mInstance;

    private AutoCheckerLocationSettingsClient(Context context) {
        super(context);
        mSettingsClient = LocationServices.getSettingsClient(context);
    }

    public void checkLocationSettings() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest).build();

        Task<LocationSettingsResponse> task = mSettingsClient.checkLocationSettings(request);

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.i(TAG, "Location settings are OK");
                mContext.sendBroadcast(
                        new Intent(AutoCheckerConstants.INTENT_REQUEST_REGISTER_GEOFENCES));
                AutoCheckerNotificationManager.getInstance(mContext).cancelNotification(
                        AutoCheckerConstants.NOTIFICATION_ENABLE_LOCATION);
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Location settings are not OK");
                int statusCode = ((ApiException) e).getStatusCode();
                if (statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                    AutoCheckerNotificationManager.getInstance(mContext).
                            notifyEnableLocationRequired();
                }
            }
        });
    }

    public static AutoCheckerLocationSettingsClient getInstance(Context context) {
        if (mInstance == null)
            mInstance = new AutoCheckerLocationSettingsClient(context);
        return mInstance;
    }
}
