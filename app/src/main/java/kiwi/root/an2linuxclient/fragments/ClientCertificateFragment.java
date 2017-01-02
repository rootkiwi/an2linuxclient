/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.crypto.RsaHelper;
import kiwi.root.an2linuxclient.crypto.Sha1Helper;
import kiwi.root.an2linuxclient.crypto.TlsHelper;

import static android.content.Context.MODE_PRIVATE;

public class ClientCertificateFragment extends Fragment implements Observer {

    private TextView fingerprintTextView;
    private Button generateNewButton;
    private KeyGenerator generator;
    private Poller poller;
    private Thread pollThread;
    private SharedPreferences deviceKeyPref;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (generator != null){
            generator.addObserver(this);
        }
        if (poller != null){
            poller.addObserver(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (generator != null){
            generator.deleteObservers();
        }
        if (poller != null){
            poller.deleteObservers();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pollThread != null){
            pollThread.interrupt();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_client_certificate, container, false);
        fingerprintTextView = (TextView) v.findViewById(R.id.fingerprintTextView);
        generateNewButton = (Button) v.findViewById(R.id.generateNewButton);
        deviceKeyPref = getActivity().getSharedPreferences("device_key_and_cert", MODE_PRIVATE);

        generateNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generator = new KeyGenerator();
                generator.addObserver(ClientCertificateFragment.this);
                new Thread(generator).start();
                generateNewButton.setEnabled(false);
                fingerprintTextView.setText(R.string.generate_key_working);
            }
        });

        boolean currentlyGenerating = deviceKeyPref.getBoolean("currently_generating", false);

        if (currentlyGenerating){
            generateNewButton.setEnabled(false);
            fingerprintTextView.setText(R.string.generate_key_working);
            if (generator == null){
                /*this means it's either the first run that is running or the user have pressed
                generate button and then exited the activity and returned before the generator
                finished, which means no callback*/
                if (poller == null){
                    poller = new Poller();
                    poller.addObserver(this);
                    pollThread = new Thread(poller);
                    pollThread.start();
                }
            }
        } else {
            setFingerprintText();
        }

        return v;
    }

    private void setFingerprintText(){
        byte[] cert = TlsHelper.getCertificateBytes(getActivity());
        byte[] sha1 = Sha1Helper.sha1(cert);
        fingerprintTextView.setText(Sha1Helper.getTwoLineHexString(sha1));
    }

    @Override
    public void update(Observable observable, Object data) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                generateNewButton.setEnabled(true);
                setFingerprintText();
            }
        });
    }

    private class KeyGenerator extends Observable implements Runnable {
        @Override
        public void run() {
            RsaHelper.initialiseRsaKeyAndCert(deviceKeyPref);
            setChanged();
            notifyObservers();
        }
    }

    private class Poller extends Observable implements Runnable {
        @Override
        public void run() {
            try {
                boolean currentlyGenerating = deviceKeyPref.getBoolean("currently_generating", false);
                while (currentlyGenerating){
                    Thread.sleep(1000);
                    currentlyGenerating = deviceKeyPref.getBoolean("currently_generating", false);
                }
                setChanged();
                notifyObservers();
            } catch (InterruptedException e){}
        }
    }

}
