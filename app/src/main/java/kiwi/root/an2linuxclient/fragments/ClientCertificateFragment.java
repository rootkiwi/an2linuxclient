/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.crypto.KeyGeneratorService;
import kiwi.root.an2linuxclient.crypto.Sha256Helper;
import kiwi.root.an2linuxclient.crypto.TlsHelper;

public class ClientCertificateFragment extends Fragment {

    private TextView fingerprintTextView;
    private Button generateNewButton;
    private GeneratorBroadcastReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (KeyGeneratorService.currentlyGenerating){
            registerReceiver();
        } else {
            setFingerprintText();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (receiver != null){
            unregisterReceiver();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_client_certificate, container, false);
        fingerprintTextView = (TextView) v.findViewById(R.id.fingerprintTextView);
        generateNewButton = (Button) v.findViewById(R.id.generateNewButton);
        generateNewButton.setOnClickListener(v1 -> {
            registerReceiver();
            KeyGeneratorService.startGenerate(getActivity());
        });
        return v;
    }

    private void setFingerprintText(){
        byte[] cert = TlsHelper.getCertificateBytes(getActivity());
        byte[] sha256 = Sha256Helper.sha256(cert);
        fingerprintTextView.setText(Sha256Helper.getFourLineHexString(sha256));
    }

    private void registerReceiver(){
        if (receiver == null){
            receiver = new GeneratorBroadcastReceiver();
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,
                new IntentFilter(KeyGeneratorService.BROADCAST_ACTION));
        generateNewButton.setEnabled(false);
        fingerprintTextView.setText(R.string.generate_key_working);
    }

    private void unregisterReceiver(){
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    private class GeneratorBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            generateNewButton.setEnabled(true);
            setFingerprintText();
            unregisterReceiver();
        }
    }

}
