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
import kiwi.root.an2linuxclient.data.MobileServer;
import kiwi.root.an2linuxclient.data.ServerDatabaseHandler;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

public class MobileDialogNew extends MobileDialog {

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
    void saveMobileServerToDatabase(boolean newCertificate){
        ServerDatabaseHandler dbHandler = ServerDatabaseHandler.getInstance(getActivity());
        long rowId;
        if (newCertificate){
            long certificateId = dbHandler.getCertificateId(TlsHelper.certificateToBytes(serverCert));
            boolean certificateAlreadyInDatabase = certificateId != -1;
            if (certificateAlreadyInDatabase){
                Toast.makeText(getActivity(), R.string.certificate_already_in_database, Toast.LENGTH_LONG).show();
                rowId = dbHandler.addMobileServer(
                        new MobileServer(ipOrHostname, portNumber, checkBoxRoaming.isChecked()),
                        certificateId);
            } else {
                rowId = dbHandler.addMobileServer(
                        new MobileServer(serverCert, ipOrHostname,
                                portNumber, checkBoxRoaming.isChecked()));
            }
        } else {
            rowId = dbHandler.addMobileServer(
                    new MobileServer(ipOrHostname, portNumber, checkBoxRoaming.isChecked()),
                    spinnerSelectedCertificateId);
        }
        serverAdapterListCallbacks.addServer(dbHandler.getMobileServer(rowId));
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
                        saveMobileServerToDatabase(true);
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

                    saveMobileServerToDatabase(false);
                }
            }
        }
    }

}
