/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.network;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;

import kiwi.root.an2linuxclient.data.BluetoothServer;
import kiwi.root.an2linuxclient.data.Notification;
import kiwi.root.an2linuxclient.data.MobileServer;
import kiwi.root.an2linuxclient.data.WifiServer;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

class NotificationConnectionHandler {

    static void sendToAllEnabledWifiServers(Notification n, Context c, List<WifiServer> enabledWifiServers){
        ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            for(WifiServer wifiServer : enabledWifiServers) {
                if (ConnectionHelper.checkIfSsidIsAllowed(wifiServer.getSsidWhitelist(), c)){
                    ThreadPoolHandler.enqueueRunnable(new TcpNotificationConnection(c, n,
                            wifiServer.getCertificate(),
                            wifiServer.getIpOrHostname(),
                            wifiServer.getPortNumber()));
                }
            }
        }
    }

    static void sendToAllEnabledMobileServers(Notification n, Context c, List<MobileServer> enabledMobileServers){
        ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            for(MobileServer mobileServer : enabledMobileServers){
                if (!networkInfo.isRoaming() || mobileServer.isRoamingAllowed()){
                    ThreadPoolHandler.enqueueRunnable(new TcpNotificationConnection(c, n,
                            mobileServer.getCertificate(),
                            mobileServer.getIpOrHostname(),
                            mobileServer.getPortNumber()));
                }
            }
        }
    }

    static void sendToAllEnabledBluetoothServers(Notification n, Context c, List<BluetoothServer> enabledBluetoothServers){
        if (BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled()){
            for(BluetoothServer bluetoothServer : enabledBluetoothServers){
                ThreadPoolHandler.enqueueBtConn(new BluetoothNotificationConnection(c, n,
                        bluetoothServer.getCertificate(),
                        bluetoothServer.getBluetoothMacAddress()));
            }
        }
    }

}
