package com.github.jordicurto.autochecker.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.activity.AutoCheckerMainActivity;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.util.ContextKeeper;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.LocalDateTime;

/**
 * Created by jordi on 12/10/16.
 */
public class AutoCheckerNotificationManager extends ContextKeeper {

    public AutoCheckerNotificationManager(Context context) {
        super(context);
    }

    private Notification buildNotification(int smallIcon, String title, String text) {

        return new Notification.Builder(mContext)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(PendingIntent.getActivity(mContext, 0,
                        new Intent(mContext, AutoCheckerMainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .build();
    }

    public void notifyTransition(WatchedLocation location, long time) {

        NotificationManager nManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (nManager != null) {

            Notification notification = buildNotification(
                    location.getStatus() == WatchedLocation.INSIDE_LOCATION ?
                    R.drawable.ic_enter_notification : R.drawable.ic_exit_notification,
                    (location.getStatus() == WatchedLocation.INSIDE_LOCATION ?
                        mContext.getString(R.string.notification_title_enter, location.getName()) :
                        mContext.getString(R.string.notification_title_leave, location.getName())),
                    mContext.getString(R.string.notification_text,
                        DateUtils.timeFormat.print(new LocalDateTime(time))));

            nManager.notify((location.getStatus() == WatchedLocation.INSIDE_LOCATION ?
                    AutoCheckerConstants.NOTIFICATION_TRANSITION_ENTER_ID :
                    AutoCheckerConstants.NOTIFICATION_TRANSITION_LEAVE_ID) , notification);
        }
    }

    public void notifyRegisteredGeofence() {

        NotificationManager nManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (nManager != null) {

            Notification notification = buildNotification(
                    R.drawable.ic_notification_register_geofence,
                    mContext.getString(R.string.notification_register_geofence_title,
                            mContext.getString(R.string.app_name)),
                    mContext.getString(R.string.notification_register_geofence_text));

            nManager.notify(AutoCheckerConstants.NOTIFICATION_REGISTER_GEOFENCE_ID, notification);
        }
    }

}
