/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kiwi.root.an2linuxclient.R;

import static android.content.Context.MODE_PRIVATE;

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        taskCallbacks = (TaskCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        taskCallbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getPreferenceManager().setSharedPreferencesName(getString(R.string.enabled_applications));
        addPreferencesFromResource(R.xml.enabled_applications_preferences);
        getActivity().setTheme(R.style.PreferenceFragmentTheme);
        final PreferenceScreen prefScreen = (PreferenceScreen) findPreference(getString(R.string.enabled_apps_pref_screen));
        findPreference(getString(R.string.preference_enable_disable_all_applications)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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
                PackageManager pm = getActivity().getPackageManager();
                List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                // apparently loadLabel() is very slow so it's faster to do it once and store in map
                final Map<String, String> appLabels = new HashMap<>();
                final Map<String, Boolean> appSettings = new HashMap<>();
                SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.enabled_applications), MODE_PRIVATE);
                for (ApplicationInfo appInfo : appList) {
                    appLabels.put(appInfo.packageName, appInfo.loadLabel(pm).toString());
                    appSettings.put(appInfo.packageName, sp.getBoolean(appInfo.packageName, false));
                }

                Collections.sort(appList, new Comparator<ApplicationInfo>() {
                    @Override
                    public int compare(ApplicationInfo lhs, ApplicationInfo rhs) {
                        boolean lhsEnabled = appSettings.get(lhs.packageName);
                        boolean rhsEnabled = appSettings.get(rhs.packageName);
                        if (lhsEnabled && !rhsEnabled) {
                            return -1;
                        }
                        else if (!lhsEnabled && rhsEnabled) {
                            return 1;
                        }
                        else {
                            return appLabels.get(lhs.packageName).compareToIgnoreCase(appLabels.get(rhs.packageName));
                        }
                    }
                });

                List<CheckBoxPreference> checkBoxPreferences = new ArrayList<>();
                for (ApplicationInfo appInfo : appList) {
                    CheckBoxPreference c = new CheckBoxPreference(getPreferenceScreen().getContext());
                    c.setKey(appInfo.packageName);
                    c.setTitle(appLabels.get(appInfo.packageName));
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

}
