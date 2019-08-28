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
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.fragments.CustomNotificationSettingsFragment;

public class CustomNotificationSettingsActivity extends AppCompatActivity implements CustomNotificationSettingsFragment.TaskCallbacks {

    final String STATE_IS_TASK_DONE = "applicationListTaskIsDone";
    private boolean applicationListTaskIsDone;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String TAG = "CustomNotificationSettingsFragment";
        FragmentManager fm = getFragmentManager();
        CustomNotificationSettingsFragment fragment = (CustomNotificationSettingsFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new CustomNotificationSettingsFragment();
            fm.beginTransaction().replace(android.R.id.content, fragment, TAG).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_IS_TASK_DONE, applicationListTaskIsDone);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        applicationListTaskIsDone = savedInstanceState.getBoolean(STATE_IS_TASK_DONE);
        if (!applicationListTaskIsDone){
            displayProgressDialog();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    private void displayProgressDialog(){
        progressDialog = new CustomProgressDialog();
        progressDialog.setCancelable(false);
        progressDialog.show(getFragmentManager(), "progressDialog");
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
            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            ((ProgressBar) view.findViewById(R.id.progressBar))
                    .getIndeterminateDrawable()
                    .setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            return view;
        }

    }

}
