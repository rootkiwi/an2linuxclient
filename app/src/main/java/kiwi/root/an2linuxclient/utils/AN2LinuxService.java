package kiwi.root.an2linuxclient.utils;

import android.app.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.activities.MainSettingsActivity;

import static kiwi.root.an2linuxclient.App.CHANNEL_ID_FOREGROUND_SERVICE;
import static kiwi.root.an2linuxclient.App.NOTIFICATION_ID_FOREGROUND_SERVICE;

public class AN2LinuxService extends Service {

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

    private void do_foreground() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this, CHANNEL_ID_FOREGROUND_SERVICE
        );

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

        startForeground(NOTIFICATION_ID_FOREGROUND_SERVICE, n);
    }
}

