/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.fragments;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kiwi.root.an2linuxclient.R;

public class EnabledApplicationsPreferenceFragment extends PreferenceFragment {

    private TaskCallbacks taskCallbacks;

    public interface TaskCallbacks {
        void onPreExecute();
        void onPostExecute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.gray_dark));

        return view;
    }

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        taskCallbacks = (TaskCallbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        getPreferenceManager().setSharedPreferencesName("enabled_applications");

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.enabled_applications_preferences);

        getActivity().setTheme(R.style.PreferenceFragmentTheme);

        final PreferenceScreen prefScreen = (PreferenceScreen) findPreference("installed_applications");
        findPreference("preference_enable_disable_all_applications").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference arg0, Object arg1) {
                for (int i = 1; i < prefScreen.getPreferenceCount(); i++) {
                    ((CheckBoxPreference) (prefScreen.getPreference(i))).setChecked((Boolean) arg1);
                }
                return true;
            }
        });

        class ApplicationTask extends AsyncTask<Void, Void, List<CheckBoxPreference>> {

            @Override
            protected void onPreExecute() {
                if (taskCallbacks != null) {
                    taskCallbacks.onPreExecute();
                }
            }

            @Override
            protected List<CheckBoxPreference> doInBackground(Void... params) {
                final PackageManager pm = getActivity().getPackageManager();

                List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                Collections.sort(appList, new Comparator<ApplicationInfo>() {
                    @Override
                    public int compare(ApplicationInfo lhs, ApplicationInfo rhs) {
                        return lhs.loadLabel(pm).toString().compareToIgnoreCase(rhs.loadLabel(pm).toString());
                    }
                });

                List<CheckBoxPreference> checkBoxPreferences = new ArrayList<>();

                for (ApplicationInfo appInfo : appList) {
                    CheckBoxPreference c = new CheckBoxPreference(getPreferenceScreen().getContext());
                    c.setKey(appInfo.packageName);
                    c.setTitle(appInfo.loadLabel(pm));
                    c.setSummary(appInfo.packageName);
                    c.setIcon(appInfo.loadIcon(pm));
                    checkBoxPreferences.add(c);
                }

                return checkBoxPreferences;

            }

            @Override
            protected void onPostExecute(List<CheckBoxPreference> checkBoxPreferences) {
                for(CheckBoxPreference checkBoxPreference : checkBoxPreferences){
                    prefScreen.addPreference(checkBoxPreference);
                }
                if (taskCallbacks != null) {
                    taskCallbacks.onPostExecute();
                }
            }

        }

        new ApplicationTask().execute();
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        taskCallbacks = null;
    }

}
