/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import java.util.Observable;
import java.util.Observer;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.data.Server;
import kiwi.root.an2linuxclient.data.WifiServer;
import kiwi.root.an2linuxclient.network.PairingConnectionCallbackMessage;
import kiwi.root.an2linuxclient.network.PairingConnectionHandler;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

abstract class WifiDialog extends TcpServerDialog implements Observer {

    EditText ssidWhitelistEditText;
    String ssidWhitelist;

    @Override
    void initViews(View v){
        super.initViews(v);
        ssidWhitelistEditText = (EditText) v.findViewById(R.id.ssidWhitelist);
        initiatePairingButton.setOnClickListener(new InitiatePairingOnClickListener());
    }

    @Override
    void initViewsDialogEdit(View v, Server wifiServer, int serverListPosition) {
        super.initViewsDialogEdit(v, wifiServer, serverListPosition);
        ssidWhitelistEditText.setText(((WifiServer)wifiServer).getSsidWhitelist());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_dialog_wifi, container);
        initViews(v);
        initViewsRetainedInstance(savedInstanceState);
        return v;
    }

    abstract void saveWifiServerToDatabase(boolean newCertificate);

    @Override
    public void update(Observable observable, Object data) {
        PairingConnectionCallbackMessage callback = (PairingConnectionCallbackMessage) data;

        final String infoText;
        switch (callback.getType()) {
            case NOT_CONNECTED: // from handler
                infoText = getString(R.string.not_connected);
                resetAfterFailedPairingConnection();
                break;
            case DISALLOWED_SSID: // from handler
                infoText = getString(R.string.disallowed_ssid);
                resetAfterFailedPairingConnection();
                break;
            case NOT_CONNECTED_TO_WIFI: // from handler
                infoText = getString(R.string.not_connected_to_wifi);
                resetAfterFailedPairingConnection();
                break;
            case UNKNOWN_HOST: // from tcp connection
                infoText = getString(R.string.unknown_host);
                resetAfterFailedPairingConnection();
                break;
            case TIMED_OUT: // from tcp connection
                infoText = getString(R.string.connected_timed_out);
                resetAfterFailedPairingConnection();
                break;
            case FAILED_TO_CONNECT: // from tcp connection
                infoText = getString(R.string.failed_to_connect);
                resetAfterFailedPairingConnection();
                break;
            case TLS_HANDSHAKE_COMPLETED:
                infoText = getString(R.string.verify_hash) + callback.getVerifyHash();
                serverCert = callback.getServerCert();
                activePairingConnection = true;
                break;
            case SERVER_ACCEPTED_PAIR:
                if (clientAcceptedPair){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            saveWifiServerToDatabase(true);
                        }
                    });
                    return;
                } else {
                    infoText = getString(R.string.server_accepted_pairing) + pairingInfoTextView.getText().toString();
                    serverAcceptedPair = true;
                }
                break;
            case SERVER_DENIED_PAIR:
                infoText = getString(R.string.server_denied_pairing);
                activePairingConnection = false;
                clientAcceptedPair = false;
                resetAfterFailedPairingConnection();
                break;
            case SOCKET_CLOSED:
                infoText = getString(R.string.connection_closed);
                activePairingConnection = false;
                clientAcceptedPair = false;
                serverAcceptedPair = false;
                resetAfterFailedPairingConnection();
                break;
            default:
                return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pairingInfoTextView.setText(infoText);
            }
        });
    }

    void setSsidWhitelist(){
        if (ssidWhitelistEditText.getText().length() > 0 && !ssidWhitelistEditText.getText().toString().trim().equals("")) {
            ssidWhitelist = ssidWhitelistEditText.getText().toString().trim();
        } else {
            ssidWhitelist = null;
        }
    }

    private class InitiatePairingOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            if (ConnectionHelper.checkIfValidAddressAndPortInput(ipOrHostnameEditText,
                    portNumberEditText, getActivity().getApplicationContext())){

                ipOrHostname = ipOrHostnameEditText.getText().toString().trim();
                portNumber = Integer.parseInt(portNumberEditText.getText().toString());
                setSsidWhitelist();

                ipOrHostnameEditText.setEnabled(false);
                portNumberEditText.setEnabled(false);
                initiatePairingButton.setVisibility(View.GONE);
                if (certificateSpinner != null) {
                    certificateSpinner.setVisibility(View.GONE);
                }

                pairingInfoTextView.setVisibility(View.VISIBLE);
                pairingInfoTextView.setText(R.string.pairing_connecting);

                if (connectionHandler == null){
                    connectionHandler = new PairingConnectionHandler();
                }
                connectionHandler.addObserver(WifiDialog.this);

                connectionHandler.startWifiPairing(ipOrHostname,
                        portNumber, ssidWhitelist,
                        getActivity().getApplicationContext());
            }
        }
    }

}
