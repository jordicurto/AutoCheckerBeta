/*
 * Copyright © 2013–2016 Michael von Glasow.
 *
 * This file is part of LSRN Tools.
 *
 * LSRN Tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSRN Tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSRN Tools.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.jordicurto.autochecker.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;

import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.receiver.AutoCheckerBroadcastReceiver;

/**
 * Provides helper methods to request permissions from components other than Activities.
 */
public class PermissionHelper {

    public static void requestPermissions(final Context context, String[] permissions,
                                          int requestCode, String notificationTitle,
                                          String notificationText, int notificationIcon) {

        Intent permIntent = new Intent(context, PermissionRequestActivity.class);

        permIntent.putExtra(AutoCheckerConstants.KEY_PERMISSIONS, permissions);
        permIntent.putExtra(AutoCheckerConstants.KEY_REQUEST_CODE, requestCode);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(permIntent);

        PendingIntent permPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(notificationIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setOngoing(true)
                .setAutoCancel(true)
                .setWhen(0)
                .setContentIntent(permPendingIntent)
                .setStyle(null);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(requestCode, builder.build());
    }


    /**
     * A blank {@link Activity} on top of which permission request dialogs can be displayed
     */
    public static class PermissionRequestActivity extends AppCompatActivity {

        String[] permissions;
        int requestCode;

        /**
         * Called when the user has made a choice in the permission dialog.
         * <p>
         * This method wraps the responses in a {@link Bundle} and passes it to the {@link ResultReceiver}
         * specified in the {@link Intent} that started the activity, then closes the activity.
         */
        @Override
        public void onRequestPermissionsResult(int requestCode, String permissions[],
                                               int[] grantResults) {
            Bundle resultData = new Bundle();
            resultData.putStringArray(AutoCheckerConstants.KEY_PERMISSIONS, permissions);
            resultData.putIntArray(AutoCheckerConstants.KEY_GRANT_RESULTS, grantResults);
            Intent intent = new Intent(AutoCheckerConstants.INTENT_PERMISSION_GRANTED);
            intent.putExtras(resultData);
            sendBroadcast(intent);
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
