package kiwi.root.an2linuxclient.utils;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.activities.MainSettingsActivity;

public class AN2LinuxService extends Service {
    private final static int SERVICE_NOTIFICATION_ID = 1;
    private final static String SERVICE_CHANNEL_ID = "AN2LinuxServiceChannel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        do_foreground();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopSelf();
        stopForeground(true);

        super.onDestroy();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        NotificationChannel chan = new NotificationChannel(SERVICE_CHANNEL_ID,
                getString(R.string.main_enable_service_notification_channel_name), NotificationManager.IMPORTANCE_MIN);
        chan.setLightColor(Color.GREEN);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(chan);
        return SERVICE_CHANNEL_ID;
    }

    private void do_foreground() {
        String channelID = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            channelID = createNotificationChannel();
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        } else{
            notificationBuilder.setPriority(Notification.PRIORITY_MIN);
        }
        notificationBuilder.setOngoing(true);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setTicker(getString(R.string.main_enable_service_notification_channel_name));
        notificationBuilder.setContentIntent(
                PendingIntent.getActivity(this, 0,
                        new Intent(this, MainSettingsActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );
        Notification n = notificationBuilder.build();

        startForeground(SERVICE_NOTIFICATION_ID, n);
    }
}

