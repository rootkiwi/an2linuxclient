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
import android.widget.EditText;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.data.Server;
import kiwi.root.an2linuxclient.data.TcpServer;

abstract class TcpServerDialog extends ServerDialog {

    EditText ipOrHostnameEditText;
    EditText portNumberEditText;

    String ipOrHostname;
    int portNumber;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("ipOrHostnameEditTextIsEnabled", ipOrHostnameEditText.isEnabled());
        outState.putBoolean("portNumberEditTextIsEnabled", portNumberEditText.isEnabled());
        super.onSaveInstanceState(outState);
    }

    void resetAfterFailedPairingConnection(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ipOrHostnameEditText.setEnabled(true);
                portNumberEditText.setEnabled(true);
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
    void initViews(View v){
        super.initViews(v);
        ipOrHostnameEditText = (EditText) v.findViewById(R.id.ipOrHostname);
        portNumberEditText = (EditText) v.findViewById(R.id.portNumber);
    }

    @Override
    void initViewsDialogEdit(View v, Server tcpServer, int serverListPosition) {
        super.initViewsDialogEdit(v, tcpServer, serverListPosition);
        ipOrHostnameEditText.setText(((TcpServer)tcpServer).getIpOrHostname());
        portNumberEditText.setText(String.valueOf(((TcpServer)tcpServer).getPortNumber()));
    }

    void initViewsRetainedInstance(Bundle savedInstanceState){
        super.initViewsRetainedInstance(savedInstanceState);
        if (savedInstanceState != null){
            ipOrHostnameEditText.setEnabled(savedInstanceState.getBoolean("ipOrHostnameEditTextIsEnabled"));
            portNumberEditText.setEnabled(savedInstanceState.getBoolean("portNumberEditTextIsEnabled"));
        }
    }

}
