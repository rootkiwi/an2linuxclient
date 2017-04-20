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
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.activities.ClientCertificateActivity;

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
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.generate_key_working))
                .setContentText(getString(R.string.generate_key_working_notification))
                .setSmallIcon(R.drawable.ic_stat_tux)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .build();
        final int NOTIFICATION_ID = 2;
        startForeground(NOTIFICATION_ID, notification);

        RsaHelper.initialiseRsaKeyAndCert(getApplicationContext());
        currentlyGenerating = false;

        stopForeground(true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_ACTION));
    }

}
