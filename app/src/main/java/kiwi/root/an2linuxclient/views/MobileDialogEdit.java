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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.crypto.TlsHelper;
import kiwi.root.an2linuxclient.data.CertificateIdAndFingerprint;
import kiwi.root.an2linuxclient.data.MobileServer;
import kiwi.root.an2linuxclient.data.ServerDatabaseHandler;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

public class MobileDialogEdit extends MobileDialog {

    private long spinnerSelectedCertificateId;

    private long serverId;
    private int serverListPosition;

    public static MobileDialogEdit newInstance(long serverId, int serverListPosition) {
        MobileDialogEdit dialog = new MobileDialogEdit();
        Bundle args = new Bundle();

        args.putLong("serverId", serverId);
        args.putInt("serverListPosition", serverListPosition);

        dialog.setArguments(args);
        return dialog;
    }

    @Override
    void initViews(View v) {
        super.initViews(v);
        super.initViewsDialogEdit(v,
                ServerDatabaseHandler.getInstance(getActivity()).getMobileServer(serverId),
                serverListPosition);
        saveServerBtn.setOnClickListener(new SaveServerOnClickListener());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverId = getArguments().getLong("serverId");
        serverListPosition = getArguments().getInt("serverListPosition");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerSelectedCertificateId = ((CertificateIdAndFingerprint)
                parent.getItemAtPosition(position)).getId();
        onItemSelectedEdit();
    }

    @Override
    void saveMobileServerToDatabase(boolean newCertificate){
        ServerDatabaseHandler dbHandler = ServerDatabaseHandler.getInstance(getActivity());
        if (newCertificate){
            long certificateId = dbHandler.getCertificateId(TlsHelper.certificateToBytes(serverCert));
            boolean certificateAlreadyInDatabase = certificateId != -1;
            if (certificateAlreadyInDatabase){
                Toast.makeText(getActivity(), R.string.certificate_already_in_database, Toast.LENGTH_LONG).show();
                dbHandler.updateMobileServer(new MobileServer(serverId, ipOrHostname, portNumber,
                        checkBoxRoaming.isChecked()), certificateId);
            } else {
                dbHandler.updateMobileServer(new MobileServer(serverId, serverCert, ipOrHostname,
                        portNumber, checkBoxRoaming.isChecked()));
            }
        } else {
            dbHandler.updateMobileServer(new MobileServer(serverId, ipOrHostname, portNumber,
                    checkBoxRoaming.isChecked()), spinnerSelectedCertificateId);
        }
        serverAdapterListCallbacks.updateServer(dbHandler.getMobileServer(serverId), serverListPosition);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getDialog().cancel();
    }

    private class SaveServerOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
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
