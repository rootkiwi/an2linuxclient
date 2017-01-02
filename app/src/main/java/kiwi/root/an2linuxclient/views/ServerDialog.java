/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.views;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.security.cert.Certificate;
import java.util.List;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.data.CertificateIdAndFingerprint;
import kiwi.root.an2linuxclient.data.Server;
import kiwi.root.an2linuxclient.data.ServerDatabaseHandler;
import kiwi.root.an2linuxclient.interfaces.CertificateSpinnerItem;
import kiwi.root.an2linuxclient.interfaces.ServerAdapterListCallbacks;
import kiwi.root.an2linuxclient.network.PairingConnectionHandler;

abstract class ServerDialog extends DialogFragment implements
        AdapterView.OnItemSelectedListener, View.OnTouchListener {

    TextView pairingInfoTextView;
    Spinner certificateSpinner;
    Button initiatePairingButton;
    Button saveServerBtn;

    PairingConnectionHandler connectionHandler;
    Certificate serverCert;
    boolean serverAcceptedPair;

    boolean activePairingConnection;
    boolean clientAcceptedPair;

    ServerAdapterListCallbacks serverAdapterListCallbacks;

    // work around for not changing view on onItemSelected
    // firing multiple times after orientation change
    boolean userSelect;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        userSelect = true;
        return false;
    }

    Long onItemSelectedNew(Object item) {
        if (item instanceof CertificateIdAndFingerprint){
            pairingInfoTextView.setVisibility(View.GONE);
            initiatePairingButton.setVisibility(View.GONE);
            return ((CertificateIdAndFingerprint) item).getId();
        } else {
            if (userSelect){
                pairingInfoTextView.setVisibility(View.GONE);
                initiatePairingButton.setVisibility(View.VISIBLE);
                initiatePairingButton.setText(R.string.dialog_pairing_button_text_or);
            }
            return null;
        }
    }

    void onItemSelectedEdit() {
        if (userSelect){
            pairingInfoTextView.setVisibility(View.GONE);
            initiatePairingButton.setText(R.string.dialog_pairing_button_text);
            userSelect = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            serverAdapterListCallbacks = (ServerAdapterListCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ServerAdapterListCallbacks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("initiatePairingButtonIsVisible",
                initiatePairingButton.getVisibility() == View.VISIBLE);
        outState.putString("initiatePairingButtonText", initiatePairingButton.getText().toString());
        if (certificateSpinner != null) {
            outState.putBoolean("certificateSpinnerIsVisible",
                    certificateSpinner.getVisibility() == View.VISIBLE);
        }
        outState.putBoolean("pairingInfoTextViewIsVisible",
                pairingInfoTextView.getVisibility() == View.VISIBLE);
        outState.putString("pairingInfoTextViewText", pairingInfoTextView.getText().toString());
        super.onSaveInstanceState(outState);
    }

    void initViews(View v) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        v.setMinimumWidth((int)(displayRectangle.width() * 0.9f));

        pairingInfoTextView = (TextView) v.findViewById(R.id.pairingInfoTextView);
        initiatePairingButton = (Button) v.findViewById(R.id.initiatePairingButton);
        saveServerBtn = (Button) v.findViewById(R.id.saveServerBtn);

        v.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }

    void initViewsDialogNew(View v) {
        ServerDatabaseHandler dbHandler = ServerDatabaseHandler.getInstance(getActivity());

        if (dbHandler.isThereAnyCertificatesInDatabase()){
            certificateSpinner = (Spinner) v.findViewById(R.id.certificateSpinner);
            List<CertificateSpinnerItem> spinnerList = dbHandler.getSpinnerList();
            spinnerList.add(0, new CertificateSpinnerItem(){
                @Override
                public String toString(){
                    return getString(R.string.spinner_choose_certificate);
                }
            });
            pairingInfoTextView.setVisibility(View.GONE);
            initiatePairingButton.setText(R.string.dialog_pairing_button_text_or);
            ArrayAdapter<CertificateSpinnerItem> spinnerArrayAdapter = new ArrayAdapter<>(
                    getActivity().getApplicationContext(),
                    R.layout.dialog_certificate_spinner_textview,
                    spinnerList);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.dialog_certificate_spinner_dropdown_textview);
            certificateSpinner.setAdapter(spinnerArrayAdapter);
            certificateSpinner.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            certificateSpinner.setOnTouchListener(this);
            certificateSpinner.setOnItemSelectedListener(this);
        } else {
            v.findViewById(R.id.certificateSpinner).setVisibility(View.GONE);
        }

        v.findViewById(R.id.deleteServerBtn).setVisibility(View.INVISIBLE);

        dbHandler.close();
    }

    void initViewsDialogEdit(View v, final Server server, final int serverListPosition) {
        pairingInfoTextView.setVisibility(View.GONE);
        final ServerDatabaseHandler dbHandler = ServerDatabaseHandler.getInstance(getActivity());

        certificateSpinner = (Spinner) v.findViewById(R.id.certificateSpinner);
        List<CertificateSpinnerItem> spinnerList = dbHandler.getSpinnerList();
        ArrayAdapter<CertificateSpinnerItem> spinnerArrayAdapter = new ArrayAdapter<>(
                getActivity().getApplicationContext(),
                R.layout.dialog_certificate_spinner_textview,
                spinnerList);

        spinnerArrayAdapter.setDropDownViewResource(R.layout.dialog_certificate_spinner_dropdown_textview);
        certificateSpinner.setAdapter(spinnerArrayAdapter);
        certificateSpinner.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        certificateSpinner.setOnTouchListener(this);
        certificateSpinner.setOnItemSelectedListener(this);

        for (int i = 0; i < spinnerList.size(); i++){
            if (((CertificateIdAndFingerprint) spinnerList.get(i)).getId() == server.getCertificateId()){
                certificateSpinner.setSelection(i);
            }
        }

        v.findViewById(R.id.deleteServerBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHandler.deleteServer(server.getId());
                serverAdapterListCallbacks.deleteServer(serverListPosition);
                cancel();
            }
        });

        dbHandler.close();
    }

    void initViewsRetainedInstance(Bundle savedInstanceState){
        if (savedInstanceState != null){
            initiatePairingButton.setVisibility(
                    savedInstanceState.getBoolean("initiatePairingButtonIsVisible") ? View.VISIBLE : View.GONE);
            initiatePairingButton.setText(savedInstanceState.getString("initiatePairingButtonText"));
            if (certificateSpinner != null) {
                certificateSpinner.setVisibility(
                        savedInstanceState.getBoolean("certificateSpinnerIsVisible") ? View.VISIBLE : View.GONE);
            }
            pairingInfoTextView.setVisibility(
                    savedInstanceState.getBoolean("pairingInfoTextViewIsVisible") ? View.VISIBLE : View.GONE);
            pairingInfoTextView.setText(savedInstanceState.getString("pairingInfoTextViewText"));
        }
    }

    @Override
    public void onDestroyView() {
        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        if ((getDialog() != null) && getRetainInstance()){
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        serverAdapterListCallbacks = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancel();
    }

    void cancel() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (connectionHandler != null){
            connectionHandler.cancel();
        }
        if (getDialog() != null){
            getDialog().cancel();
        }
    }

}
