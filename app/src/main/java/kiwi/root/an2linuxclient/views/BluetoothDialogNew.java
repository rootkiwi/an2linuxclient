/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.views;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.crypto.TlsHelper;
import kiwi.root.an2linuxclient.data.BluetoothServer;
import kiwi.root.an2linuxclient.data.ServerDatabaseHandler;

public class BluetoothDialogNew extends BluetoothDialog {

    private Long spinnerSelectedCertificateId;

    private String btNameNewServer;
    private String btMacNewServer;

    public static BluetoothDialogNew newInstance(String btNameNewServer, String btMacNewServer) {
        BluetoothDialogNew dialog = new BluetoothDialogNew();
        Bundle args = new Bundle();

        args.putString("btNameNewServer", btNameNewServer);
        args.putString("btMacNewServer", btMacNewServer);

        dialog.setArguments(args);
        return dialog;
    }

    @Override
    void initViews(View v) {
        super.initViews(v);
        super.initViewsDialogNew(v);
        btNameEditText.setText(btNameNewServer);
        btMacTextView.setText(btMacNewServer);
        saveServerBtn.setOnClickListener(new SaveServerOnClickListener());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btNameNewServer = getArguments().getString("btNameNewServer");
        btMacNewServer = getArguments().getString("btMacNewServer");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerSelectedCertificateId = onItemSelectedNew(parent.getItemAtPosition(position));
    }

    @Override
    void saveBluetoothServerToDatabase(boolean newCertificate){
        ServerDatabaseHandler dbHandler = ServerDatabaseHandler.getInstance(getActivity());
        long rowId;
        String btName = btNameEditText.getText().toString();
        String btMacAddress = btMacTextView.getText().toString();
        if (newCertificate){
            long certificateId = dbHandler.getCertificateId(TlsHelper.certificateToBytes(serverCert));
            boolean certificateAlreadyInDatabase = certificateId != -1;
            if (certificateAlreadyInDatabase){
                Toast.makeText(getActivity(), R.string.certificate_already_in_database, Toast.LENGTH_LONG).show();
                rowId = dbHandler.addBluetoothServer(
                        new BluetoothServer(btMacAddress, btName),
                        certificateId);
            } else {
                rowId = dbHandler.addBluetoothServer(
                        new BluetoothServer(serverCert, btMacAddress, btName));
            }
        } else {
            rowId = dbHandler.addBluetoothServer(
                    new BluetoothServer(btMacAddress, btName),
                    spinnerSelectedCertificateId);
        }
        serverAdapterListCallbacks.addServer(dbHandler.getBluetoothServer(rowId));
        getActivity().getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getDialog().cancel();
    }

    private class SaveServerOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if (spinnerSelectedCertificateId == null){
                if (activePairingConnection){

                    connectionHandler.acceptPairing(); // let server know you have accepted pair
                    clientAcceptedPair = true;

                    if (serverAcceptedPair){
                        saveBluetoothServerToDatabase(true);
                    } else {
                        // server have not accepted pair yet, need to wait for that
                        pairingInfoTextView.setText(getString(R.string.waiting_for_server_to_accept, pairingInfoTextView.getText().toString()));
                        saveServerBtn.setEnabled(false);
                    }

                } else {
                    Toast.makeText(getActivity(), R.string.need_to_pair_first, Toast.LENGTH_SHORT).show();
                }
            } else {
                saveBluetoothServerToDatabase(false);
            }
        }
    }

}
