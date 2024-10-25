package com.lelei.b_r_gas.Helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import com.lelei.b_r_gas.R;

public class NotificationHelper extends ContextWrapper {

    private static final String RJSWEETS_CHANNEL_ID = "com.lelei.b_r_gas.FloGas";
    private static final String RJSWEETS_CHANNEL_NAME = "flo gas Final";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        createChannel();
    }

    private void createChannel() {

        NotificationChannel rjChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rjChannel = new NotificationChannel(RJSWEETS_CHANNEL_ID, RJSWEETS_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rjChannel.enableLights(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rjChannel.enableVibration(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rjChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        }

        getManager().createNotificationChannel(rjChannel);
    }

    public NotificationManager getManager() {
        if (manager == null){
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public Notification.Builder rjSweetsChannelNotification(String title, String body){
        return  new Notification.Builder(getApplicationContext(), RJSWEETS_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.sweet_icon_notification)
                .setAutoCancel(true);
    }
}
