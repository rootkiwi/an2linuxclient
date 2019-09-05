/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.viewmodels.EnabledApplicationsViewModel;
import kiwi.root.an2linuxclient.views.CustomProgressDialog;

public class EnabledApplicationsActivity extends AppCompatActivity {

    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enabled_applications);

        EnabledApplicationsViewModel viewModel = ViewModelProviders.of(this).get(EnabledApplicationsViewModel.class);
        viewModel.getOperationRunning().observe(this, operationIsRunning -> {
            if (operationIsRunning) {
                progressDialog = new CustomProgressDialog();
                progressDialog.setCancelable(false);
                progressDialog.show(getSupportFragmentManager(), "progressDialog");
            } else {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
        });
    }

}
