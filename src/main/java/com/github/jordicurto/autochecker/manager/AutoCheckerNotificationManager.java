package com.github.jordicurto.autochecker.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.activity.AutoCheckerMainActivity;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.util.ContextKeeper;
import com.github.jordicurto.autochecker.util.DateUtils;
import com.github.jordicurto.autochecker.util.PermissionHelper;

import org.joda.time.LocalDateTime;

/**
 * Created by jordi on 12/10/16.
 */
public class AutoCheckerNotificationManager extends ContextKeeper {

    private NotificationManager nManager;
    private String IMPORTANT_CHANNEL_ID = "AUTOCHECKER_NOTIFICATION_CHANNEL_IMPORTANT";
    // TODO: Add notication channels

    public AutoCheckerNotificationManager(Context context) {
        super(context);
        nManager = (NotificationManager) getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getContext().getString(R.string.notification_channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(IMPORTANT_CHANNEL_ID, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            nManager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(int smallIcon, String title, String text) {
        return buildNotification(smallIcon, title, text, false,
                new Intent(getContext(), AutoCheckerMainActivity.class));
    }

    private Notification buildNotification(int smallIcon, String title, String text,
                                           boolean ongoing, Intent permIntent) {

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
        stackBuilder.addNextIntent(permIntent);

        PendingIntent permPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        return new NotificationCompat.Builder(getContext(), IMPORTANT_CHANNEL_ID)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(permPendingIntent)
                .setOngoing(ongoing)
                .setAutoCancel(true)
                .build();
    }

    public void notifyTransition(WatchedLocation location, long time) {

        if (nManager != null) {

            Notification notification = buildNotification(
                    location.getStatus() == WatchedLocation.INSIDE_LOCATION ?
                    R.drawable.ic_enter_notification : R.drawable.ic_exit_notification,
                    (location.getStatus() == WatchedLocation.INSIDE_LOCATION ?
                            getContext().getString(R.string.notification_title_enter, location.getName()) :
                            getContext().getString(R.string.notification_title_leave, location.getName())),
                    getContext().getString(R.string.notification_text,
                        DateUtils.timeFormat.print(new LocalDateTime(time))));

            nManager.notify((location.getStatus() == WatchedLocation.INSIDE_LOCATION ?
                    AutoCheckerConstants.NOTIFICATION_TRANSITION_ENTER_ID :
                    AutoCheckerConstants.NOTIFICATION_TRANSITION_LEAVE_ID) , notification);
        }
    }

    public void notifyRegisteredGeofence() {

        if (nManager != null) {

            Notification notification = buildNotification(
                    R.drawable.ic_notification_autochecker,
                    getContext().getString(R.string.notification_register_geofence_title,
                            getContext().getString(R.string.app_name)),
                    getContext().getString(R.string.notification_register_geofence_text));

            nManager.notify(AutoCheckerConstants.NOTIFICATION_REGISTER_GEOFENCE_ID, notification);
        }
    }

    public void notifyPermissionRequired(String[] permissions, int requestCode) {

        if (nManager != null) {

            Intent permIntent = new Intent(getContext(),
                    PermissionHelper.PermissionRequestActivity.class);

            permIntent.putExtra(AutoCheckerConstants.KEY_PERMISSIONS, permissions);
            permIntent.putExtra(AutoCheckerConstants.KEY_REQUEST_CODE, requestCode);

            Notification notification = buildNotification(
                    R.drawable.ic_notification_autochecker,
                    getContext().getString(R.string.request_permission_title),
                    getContext().getString(R.string.request_permission_text),
                    true, permIntent);

            nManager.notify(AutoCheckerConstants.NOTIFICATION_PERMISSION_REQUIRED, notification);
        }
    }

    public void notifyEnableLocationRequired() {

        if (nManager != null) {

            Intent permIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

            Notification notification = buildNotification(
                    R.drawable.ic_notification_autochecker,
                    getContext().getString(R.string.not_enable_location_title),
                    getContext().getString(R.string.not_enable_location_text),
                    true, permIntent);

            nManager.notify(AutoCheckerConstants.NOTIFICATION_ENABLE_LOCATION, notification);
        }
    }

    public void cancelNotification(int id) {
        nManager.cancel(id);
    }

}
