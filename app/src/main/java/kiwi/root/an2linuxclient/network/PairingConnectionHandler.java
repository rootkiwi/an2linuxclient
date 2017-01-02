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

import java.util.Observable;
import java.util.Observer;

import kiwi.root.an2linuxclient.utils.ConnectionHelper;

import static kiwi.root.an2linuxclient.network.PairingConnectionCallbackMessage.CallbackType.*;

public class PairingConnectionHandler extends Observable implements Observer {

    private PairingConnection pairingConnection;

    public void startWifiPairing(String ipOrHostname, int port,
                                 String ssidWhitelist, Context c){
        ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean wifiConnected = false;
        boolean mobileConnected = false;

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) wifiConnected = true;
            else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) mobileConnected = true;

            boolean serverHasSsidSetting = ssidWhitelist != null;
            boolean allowedToConnect = false;

            if (wifiConnected){
                if (serverHasSsidSetting){
                    if (ConnectionHelper.checkIfSsidIsAllowed(ssidWhitelist, c)) {
                        allowedToConnect = true;
                    } else {
                        notifyObservers(new PairingConnectionCallbackMessage(DISALLOWED_SSID));
                    }
                } else {
                    allowedToConnect = true;
                }
            } else if (mobileConnected){
                notifyObservers(new PairingConnectionCallbackMessage(NOT_CONNECTED_TO_WIFI));
            }

            if (allowedToConnect){
                pairingConnection = new TcpPairingConnection(ipOrHostname, port, c);
                pairingConnection.addObserver(this);

                Thread pairingThread = new Thread(pairingConnection);
                pairingThread.start();
            }

        } else {
            notifyObservers(new PairingConnectionCallbackMessage(NOT_CONNECTED));
        }
    }

    public void startMobilePairing(String ipOrHostname, int port, boolean allowRoaming, Context c){
        ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean wifiConnected = false;
        boolean mobileConnected = false;

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) wifiConnected = true;
            else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) mobileConnected = true;

            boolean allowedToConnect = false;

            // allowing mobile pairing even when connected to wifi because why not
            if (wifiConnected){
                allowedToConnect = true;
            } else if (mobileConnected){
                if (!networkInfo.isRoaming() || allowRoaming){
                    allowedToConnect = true;
                } else {
                    notifyObservers(new PairingConnectionCallbackMessage(NOT_ALLOWED_TO_ROAM));
                }
            }

            if (allowedToConnect){
                pairingConnection = new TcpPairingConnection(ipOrHostname, port, c);
                pairingConnection.addObserver(this);

                new Thread(pairingConnection).start();
            }

        } else {
            notifyObservers(new PairingConnectionCallbackMessage(NOT_CONNECTED));
        }
    }

    public void startBluetoothPairing(String serverMacAddress, Context c){
        if (BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled()){

            pairingConnection = new BluetoothPairingConnection(serverMacAddress, c);
            pairingConnection.addObserver(this);

            new Thread(pairingConnection).start();

        } else {
            notifyObservers(new PairingConnectionCallbackMessage(BLUETOOTH_NOT_ENABLED));
        }
    }

    public void acceptPairing(){
        ThreadPoolHandler.enqueueRunnable(new Runnable() {
            @Override
            public void run() {
                pairingConnection.acceptPairing();
            }
        });
    }

    public void cancel(){
        deleteObservers();
        if (pairingConnection != null) {
            pairingConnection.deleteObservers();
            ThreadPoolHandler.enqueueRunnable(new Runnable() {
                @Override
                public void run() {
                    pairingConnection.cancel();
                }
            });
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        notifyObservers(data);
    }

    @Override
    public void notifyObservers(Object data) {
        setChanged();
        super.notifyObservers(data);
    }

}
