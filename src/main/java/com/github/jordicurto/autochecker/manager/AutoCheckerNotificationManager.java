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

    public void notifyTransition(WatchedLocation location, long time) {

        NotificationManager nManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (nManager != null) {

            Notification notification = new Notification.Builder(mContext)
                    .setSmallIcon(location.getStatus() == WatchedLocation.INSIDE_LOCATION ?
                            R.drawable.ic_enter_notification : R.drawable.ic_exit_notification)
                    .setContentTitle(
                            (location.getStatus() == WatchedLocation.INSIDE_LOCATION ?
                                    mContext.getString(R.string.notification_title_enter) :
                                    mContext.getString(R.string.notification_title_leave))
                                    + " " + location.getName())
                    .setContentText(mContext.getString(R.string.notification_text) + " " +
                            DateUtils.timeFormat.print(new LocalDateTime(time)))
                    .setContentIntent(PendingIntent.getActivity(getContext(), 0,
                            new Intent(getContext(), AutoCheckerMainActivity.class),
                            PendingIntent.FLAG_UPDATE_CURRENT))
                    .setAutoCancel(true)
                    .build();

            nManager.notify((location.getStatus() == WatchedLocation.INSIDE_LOCATION ?
                    AutoCheckerConstants.TRANSTION_ENTER_NOTIFICATION_ID :
                    AutoCheckerConstants.TRANSTION_LEAVE_NOTIFICATION_ID) , notification);
        }
    }
}
