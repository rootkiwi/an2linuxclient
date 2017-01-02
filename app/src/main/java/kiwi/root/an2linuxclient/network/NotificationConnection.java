/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.network;

import android.content.Context;

import java.security.cert.Certificate;

import kiwi.root.an2linuxclient.data.Notification;

abstract class NotificationConnection implements Runnable {

    Context c;
    Notification n;
    Certificate serverCert;

    final byte NOTIF_CONN = 1;

    NotificationConnection(Context c, Notification n, Certificate serverCert) {
        this.c = c;
        this.n = n;
        this.serverCert = serverCert;
    }

}
