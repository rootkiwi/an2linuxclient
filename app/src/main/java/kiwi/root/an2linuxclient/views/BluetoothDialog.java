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
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.data.BluetoothServer;
import kiwi.root.an2linuxclient.data.Server;
import kiwi.root.an2linuxclient.network.PairingConnectionCallbackMessage;
import kiwi.root.an2linuxclient.network.PairingConnectionHandler;

public abstract class BluetoothDialog extends ServerDialog implements Observer {

    EditText btNameEditText;
    TextView btMacTextView;

    @Override
    void initViews(View v){
        super.initViews(v);
        btNameEditText = (EditText) v.findViewById(R.id.btNameEditText);
        btMacTextView = (TextView) v.findViewById(R.id.btMacTextView);
        initiatePairingButton.setOnClickListener(new InitiatePairingOnClickListener());
    }

    @Override
    void initViewsDialogEdit(View v, Server bluetoothServer, int serverListPosition) {
        super.initViewsDialogEdit(v, bluetoothServer, serverListPosition);
        btNameEditText.setText(((BluetoothServer)bluetoothServer).getBluetoothName());
        btMacTextView.setText(((BluetoothServer)bluetoothServer).getBluetoothMacAddress());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_dialog_bluetooth, container);
        initViews(v);
        initViewsRetainedInstance(savedInstanceState);
        return v;
    }

    abstract void saveBluetoothServerToDatabase(boolean newCertificate);

    private void resetAfterFailedPairingConnection(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initiatePairingButton.setVisibility(View.VISIBLE);
                initiatePairingButton.setText(R.string.try_again);
                if (certificateSpinner != null){
                    certificateSpinner.setVisibility(View.VISIBLE);
                }
                saveServerBtn.setEnabled(true);

                if (connectionHandler != null){
                    connectionHandler.cancel();
                }
            }
        });
    }

    @Override
    public void update(Observable observable, Object data) {
        PairingConnectionCallbackMessage callback = (PairingConnectionCallbackMessage) data;

        final String infoText;
        switch (callback.getType()) {
            case BLUETOOTH_NOT_ENABLED: // from handler
                infoText = getString(R.string.bluetooth_is_off);
                resetAfterFailedPairingConnection();
                break;
            case FAILED_TO_CONNECT: // from connection
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
                            saveBluetoothServerToDatabase(true);
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

    private class InitiatePairingOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            initiatePairingButton.setVisibility(View.GONE);
            if (certificateSpinner != null) {
                certificateSpinner.setVisibility(View.GONE);
            }

            pairingInfoTextView.setVisibility(View.VISIBLE);
            pairingInfoTextView.setText(R.string.pairing_connecting);

            if (connectionHandler == null){
                connectionHandler = new PairingConnectionHandler();
            }
            connectionHandler.addObserver(BluetoothDialog.this);

            connectionHandler.startBluetoothPairing(btMacTextView.getText().toString(),
                    getActivity().getApplicationContext());
        }
    }

}
