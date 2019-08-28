/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.RequiresApi;

public class App extends Application {

    public final static String CHANNEL_ID_FOREGROUND_SERVICE = "AN2LinuxForegroundServiceChannel";
    public final static int NOTIFICATION_ID_FOREGROUND_SERVICE = 1;

    public final static String CHANNEL_ID_INFORMATION = "AN2LinuxInformationChannel";
    public final static int NOTIFICATION_ID_HIDE_FOREGROUND_NOTIF = 2;
    public static final int NOTIFICATION_ID_TEST_NOTIF = 3;

    public static final String CHANNEL_ID_GEN = "AN2LinuxKeyGenChannel";
    public static final int NOTIFICATION_ID_GEN = 4;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels();
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannels() {
        NotificationChannel foregroundServiceChan = new NotificationChannel(
                CHANNEL_ID_FOREGROUND_SERVICE,
                getString(R.string.main_enable_service_notification_channel_name),
                NotificationManager.IMPORTANCE_MIN
        );
        foregroundServiceChan.setLightColor(Color.GREEN);
        foregroundServiceChan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationChannel infoChan = new NotificationChannel(CHANNEL_ID_INFORMATION,
                getString(R.string.main_enable_service_information_notification_channel_name), NotificationManager.IMPORTANCE_LOW);
        infoChan.setLightColor(Color.GREEN);
        infoChan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationChannel keyGenChannel = new NotificationChannel(
                CHANNEL_ID_GEN,
                getString(R.string.generate_key_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(foregroundServiceChan);
        notificationManager.createNotificationChannel(infoChan);
        notificationManager.createNotificationChannel(keyGenChannel);
    }

}
