/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.activities;

import android.app.FragmentManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import kiwi.root.an2linuxclient.fragments.ClientCertificateFragment;

public class ClientCertificateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String TAG = "ClientCertificateFragment";

        FragmentManager fm = getFragmentManager();
        ClientCertificateFragment fragment = (ClientCertificateFragment) fm.findFragmentByTag(TAG);

        if (fragment == null) {
            fragment = new ClientCertificateFragment();
            fm.beginTransaction().replace(android.R.id.content, fragment, TAG).commit();
        }
    }

}
