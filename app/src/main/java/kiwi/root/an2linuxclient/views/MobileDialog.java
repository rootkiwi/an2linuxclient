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
import android.widget.CheckBox;

import java.util.Observable;
import java.util.Observer;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.data.MobileServer;
import kiwi.root.an2linuxclient.data.Server;
import kiwi.root.an2linuxclient.network.PairingConnectionCallbackMessage;
import kiwi.root.an2linuxclient.network.PairingConnectionHandler;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

abstract class MobileDialog extends TcpServerDialog implements Observer {

    CheckBox checkBoxRoaming;

    @Override
    void initViews(View v){
        super.initViews(v);
        checkBoxRoaming = (CheckBox) v.findViewById(R.id.checkBoxRoaming);
        initiatePairingButton.setOnClickListener(new InitiatePairingOnClickListener());
    }

    @Override
    void initViewsDialogEdit(View v, Server mobileServer, int serverListPosition) {
        super.initViewsDialogEdit(v, mobileServer, serverListPosition);
        checkBoxRoaming.setChecked(((MobileServer)mobileServer).isRoamingAllowed());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_dialog_mobile, container);
        initViews(v);
        initViewsRetainedInstance(savedInstanceState);
        return v;
    }

    abstract void saveMobileServerToDatabase(boolean newCertificate);

    @Override
    public void update(Observable observable, Object data) {
        PairingConnectionCallbackMessage callback = (PairingConnectionCallbackMessage) data;

        final String infoText;
        switch (callback.getType()) {
            case NOT_CONNECTED: // from handler
                infoText = getString(R.string.not_connected);
                resetAfterFailedPairingConnection();
                break;
            case NOT_ALLOWED_TO_ROAM: // from handler
                infoText = getString(R.string.you_are_roaming);
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
                    getActivity().runOnUiThread(() -> saveMobileServerToDatabase(true));
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

        getActivity().runOnUiThread(() -> pairingInfoTextView.setText(infoText));
    }

    private class InitiatePairingOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            if (ConnectionHelper.checkIfValidAddressAndPortInput(ipOrHostnameEditText,
                    portNumberEditText, getActivity().getApplicationContext())){

                ipOrHostname = ipOrHostnameEditText.getText().toString().trim();
                portNumber = Integer.parseInt(portNumberEditText.getText().toString());

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
                connectionHandler.addObserver(MobileDialog.this);

                connectionHandler.startMobilePairing(ipOrHostname,
                        portNumber, checkBoxRoaming.isChecked(),
                        getActivity().getApplicationContext());
            }
        }
    }

}
