/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.activities;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.fragments.EnabledApplicationsPreferenceFragment;

public class EnabledApplicationsActivity extends AppCompatActivity implements EnabledApplicationsPreferenceFragment.TaskCallbacks {


    final String STATE_IS_TASK_DONE = "applicationListTaskIsDone";
    private boolean applicationListTaskIsDone;

    CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String TAG_TASK_FRAGMENT = "EnabledApplicationsPreferenceFragment";

        FragmentManager fm = getFragmentManager();
        EnabledApplicationsPreferenceFragment taskFragment = (EnabledApplicationsPreferenceFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (taskFragment == null) {
            taskFragment = new EnabledApplicationsPreferenceFragment();
            fm.beginTransaction().replace(android.R.id.content, taskFragment, TAG_TASK_FRAGMENT).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_IS_TASK_DONE, applicationListTaskIsDone);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        applicationListTaskIsDone = savedInstanceState.getBoolean(STATE_IS_TASK_DONE);

        if (!applicationListTaskIsDone){
            displayProgressDialog();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    private void displayProgressDialog(){
        progressDialog = new CustomProgressDialog();
        progressDialog.setCancelable(false);
        progressDialog.show(getFragmentManager(), "test_dialog");
    }

    @Override
    public void onPreExecute(){
        displayProgressDialog();
    }

    @Override
    public void onPostExecute(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        applicationListTaskIsDone = true;
    }

    public static class CustomProgressDialog extends DialogFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ProgressDialogStyle);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.view_progressbar, container);

            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

            return view;
        }

    }

}
