/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
import kiwi.root.an2linuxclient.activities.AppNotificationSettingsActivity;
import kiwi.root.an2linuxclient.activities.EnabledApplicationsActivity;

import static android.content.Context.MODE_PRIVATE;

public class CustomNotificationSettingsFragment extends PreferenceFragment {

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
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getActivity()));
        getActivity().setTheme(R.style.PreferenceFragmentTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().removeAll();
        class ApplicationTask extends AsyncTask<Void, Void, List<Preference>> {

            @Override
            protected void onPreExecute() {
                if (taskCallbacks != null) {
                    taskCallbacks.onPreExecute();
                }
            }

            @Override
            protected List<Preference> doInBackground(Void... params) {
                PackageManager pm = getActivity().getPackageManager();
                List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                List<ApplicationInfo> enabledApps = new ArrayList<>();
                SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.enabled_applications), MODE_PRIVATE);
                // apparently loadLabel() is very slow so it's faster to do it once and store in map
                final Map<String, String> appLabels = new HashMap<>();
                for (ApplicationInfo appInfo : appList) {
                    if (sp.getBoolean(appInfo.packageName, false)) {
                        enabledApps.add(appInfo);
                        appLabels.put(appInfo.packageName, appInfo.loadLabel(pm).toString());
                    }
                }
                Collections.sort(enabledApps, new Comparator<ApplicationInfo>() {
                    @Override
                    public int compare(ApplicationInfo lhs, ApplicationInfo rhs) {
                        return appLabels.get(lhs.packageName).compareToIgnoreCase(appLabels.get(rhs.packageName));
                    }
                });
                List<Preference> prefs = new ArrayList<>();
                for (ApplicationInfo appInfo : enabledApps) {
                    Preference p = new Preference(getPreferenceScreen().getContext());
                    p.setTitle(appLabels.get(appInfo.packageName));
                    p.setSummary(getActivity().getSharedPreferences(getString(R.string.notification_settings_custom), MODE_PRIVATE)
                            .getBoolean(appInfo.packageName + "_" + getString(R.string.preference_use_custom_settings), false)
                            ? getString(R.string.notif_custom_using_custom_settings) : getString(R.string.notif_custom_using_general_settings));
                    p.setIcon(appInfo.loadIcon(pm));
                    Intent i = new Intent(getActivity().getApplicationContext(), AppNotificationSettingsActivity.class);
                    i.putExtra("appName", appLabels.get(appInfo.packageName));
                    i.putExtra("packageName", appInfo.packageName);
                    p.setIntent(i);
                    prefs.add(p);
                }
                if (prefs.size() == 0) {
                    Preference p = new Preference(getPreferenceScreen().getContext());
                    p.setTitle(R.string.notif_settings_custom_no_enabled_title);
                    p.setSummary(R.string.notif_settings_custom_no_enabled_summary);
                    p.setIntent(new Intent(getActivity().getApplicationContext(), EnabledApplicationsActivity.class));
                    prefs.add(p);
                }
                return prefs;
            }

            @Override
            protected void onPostExecute(List<Preference> prefs) {
                PreferenceScreen screen = getPreferenceScreen();
                for(Preference pref : prefs){
                    screen.addPreference(pref);
                }
                if (taskCallbacks != null) {
                    taskCallbacks.onPostExecute();
                }
            }

        }

        new ApplicationTask().execute();
    }

}
