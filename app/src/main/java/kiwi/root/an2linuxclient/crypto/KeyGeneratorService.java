/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.crypto;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.activities.ClientCertificateActivity;

import static kiwi.root.an2linuxclient.App.CHANNEL_ID_GEN;
import static kiwi.root.an2linuxclient.App.NOTIFICATION_ID_GEN;

public class KeyGeneratorService extends IntentService {


    public static String BROADCAST_ACTION = "kiwi.root.an2linuxclient.crypto.KEY_GENERATOR_COMPLETED";
    public static boolean currentlyGenerating;

    public KeyGeneratorService() {
        super("KeyGeneratorService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Intent notificationIntent = new Intent(this, ClientCertificateActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_GEN)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.generate_key_working))
                .setContentTitle(getString(R.string.generate_key_working_notification))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(NOTIFICATION_ID_GEN, notification);

        RsaHelper.initialiseRsaKeyAndCert(getApplicationContext());
        currentlyGenerating = false;

        stopForeground(true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_ACTION));
    }

    public static void startGenerate(Context c) {
        currentlyGenerating = true;
        c.startService(new Intent(c, KeyGeneratorService.class));
    }

}
