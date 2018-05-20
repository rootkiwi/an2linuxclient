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

    @Override
    public void onCreate() {
        super.onCreate();
    }

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
        String channelId = "AN2LinuxChannel";
        String channelName = "AN2Linux foreground notification channel";
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_MIN);
        chan.setLightColor(Color.GREEN);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert service != null;
        service.createNotificationChannel(chan);
        return channelId;
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
        notificationBuilder.setOngoing(true).setSmallIcon(R.mipmap.ic_launcher).setTicker("AN2Linux").setContentIntent(
                PendingIntent.getActivity(this, 0,
                        new Intent(this, MainSettingsActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );
        Notification n = notificationBuilder.build();

        startForeground(1, n);
    }
}

