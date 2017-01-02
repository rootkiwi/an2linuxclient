/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.views;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.crypto.TlsHelper;
import kiwi.root.an2linuxclient.data.ServerDatabaseHandler;
import kiwi.root.an2linuxclient.data.WifiServer;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

public class WifiDialogNew extends WifiDialog {

    private Long spinnerSelectedCertificateId;

    @Override
    void initViews(View v) {
        super.initViews(v);
        super.initViewsDialogNew(v);
        saveServerBtn.setOnClickListener(new SaveServerOnClickListener());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerSelectedCertificateId = onItemSelectedNew(parent.getItemAtPosition(position));
    }

    @Override
    void saveWifiServerToDatabase(boolean newCertificate){
        ServerDatabaseHandler dbHandler = ServerDatabaseHandler.getInstance(getActivity());
        long rowId;
        if (newCertificate){
            long certificateId = dbHandler.getCertificateId(TlsHelper.certificateToBytes(serverCert));
            boolean certificateAlreadyInDatabase = certificateId != -1;
            if (certificateAlreadyInDatabase){
                Toast.makeText(getActivity(), R.string.certificate_already_in_database, Toast.LENGTH_LONG).show();
                rowId = dbHandler.addWifiServer(
                        new WifiServer(ipOrHostname, portNumber, ssidWhitelist),
                        certificateId);
            } else {
                rowId = dbHandler.addWifiServer(
                        new WifiServer(serverCert, ipOrHostname,
                                portNumber, ssidWhitelist));
            }
        } else {
            rowId = dbHandler.addWifiServer(
                    new WifiServer(ipOrHostname, portNumber, ssidWhitelist),
                    spinnerSelectedCertificateId);
        }
        serverAdapterListCallbacks.addServer(dbHandler.getWifiServer(rowId));
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
                        saveWifiServerToDatabase(true);
                    } else {
                        // server have not accepted pair yet, need to wait for that
                        pairingInfoTextView.setText(getString(R.string.waiting_for_server_to_accept, pairingInfoTextView.getText().toString()));
                        saveServerBtn.setEnabled(false);
                    }

                } else {
                    Toast.makeText(getActivity(), R.string.need_to_pair_first, Toast.LENGTH_SHORT).show();
                }
            } else {
                if (ConnectionHelper.checkIfValidAddressAndPortInput(ipOrHostnameEditText,
                        portNumberEditText, getActivity().getApplicationContext())) {

                    ipOrHostname = ipOrHostnameEditText.getText().toString().trim();
                    portNumber = Integer.parseInt(portNumberEditText.getText().toString());
                    setSsidWhitelist();

                    saveWifiServerToDatabase(false);
                }
            }
        }
    }

}
