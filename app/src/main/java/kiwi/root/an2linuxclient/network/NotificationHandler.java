/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.network;

import android.content.Context;
import android.service.notification.StatusBarNotification;

import java.util.List;

import kiwi.root.an2linuxclient.data.BluetoothServer;
import kiwi.root.an2linuxclient.data.Notification;
import kiwi.root.an2linuxclient.data.ServerDatabaseHandler;
import kiwi.root.an2linuxclient.data.MobileServer;
import kiwi.root.an2linuxclient.data.WifiServer;

public class NotificationHandler {

    public static void handleStatusBarNotification(StatusBarNotification sbn, Context c){
        Notification n = new Notification(sbn, c);
        sendNotificationToAllEnabledServers(n, c);
    }

    private static void sendNotificationToAllEnabledServers(Notification n, Context c){
        ServerDatabaseHandler dbHandler = ServerDatabaseHandler.getInstance(c);

        List<WifiServer> allEnabledWifiServers = dbHandler.getAllEnabledWifiServers();
        if (allEnabledWifiServers.size() > 0) {
            NotificationConnectionHandler.sendToAllEnabledWifiServers(n, c, allEnabledWifiServers);
        }

        List<MobileServer> allEnabledMobileServers = dbHandler.getAllEnabledMobileServers();
        if (allEnabledMobileServers.size() > 0) {
            NotificationConnectionHandler.sendToAllEnabledMobileServers(n, c, allEnabledMobileServers);
        }

        List<BluetoothServer> allEnabledBluetoothServers = dbHandler.getAllEnabledBluetoothServers();
        if (allEnabledBluetoothServers.size() > 0) {
            NotificationConnectionHandler.sendToAllEnabledBluetoothServers(n, c, allEnabledBluetoothServers);
        }
    }

}
