
package com.github.jordicurto.autochecker.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.manager.AutoCheckerNotificationManager;
import com.github.jordicurto.autochecker.service.AutoCheckerIntentService;

/**
 * Provides helper methods to request permissions from components other than Activities.
 */
public class PermissionHelper {

    /**
     * A blank {@link Activity} on top of which permission request dialogs can be displayed
     */
    public static class PermissionRequestActivity extends AppCompatActivity {

        String[] permissions;
        int requestCode;

        private AutoCheckerNotificationManager mAutoCheckerNotificationManager;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mAutoCheckerNotificationManager = new AutoCheckerNotificationManager(this);
        }

        /**
         * Called when the user has made a choice in the permission dialog.
         * <p>
         * This method wraps the responses in a {@link Bundle} and passes it to the {@link ResultReceiver}
         * specified in the {@link Intent} that started the activity, then closes the activity.
         */
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                               @NonNull int[] grantResults) {
            Bundle resultData = new Bundle();
            resultData.putStringArray(AutoCheckerConstants.KEY_PERMISSIONS, permissions);
            resultData.putIntArray(AutoCheckerConstants.KEY_GRANT_RESULTS, grantResults);
            AutoCheckerIntentService.enqueueWork(
                    this, AutoCheckerConstants.INTENT_PERMISSION_GRANTED, resultData);
            mAutoCheckerNotificationManager.cancelNotification(
                    AutoCheckerConstants.NOTIFICATION_PERMISSION_REQUIRED);
            finish();
        }


        /**
         * Called when the activity is started.
         * <p>
         * This method obtains several extras from the {@link Intent} that started the activity: the request
         * code, the requested permissions and the {@link ResultReceiver} which will receive the results.
         * After that, it issues the permission request.
         */
        @Override
        protected void onStart() {
            super.onStart();

            permissions = getIntent().getStringArrayExtra(AutoCheckerConstants.KEY_PERMISSIONS);
            requestCode = getIntent().getIntExtra(AutoCheckerConstants.KEY_REQUEST_CODE, 0);

            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }
}
