/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import java.util.List;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.preferences.CheckBoxPreferenceData;
import kiwi.root.an2linuxclient.viewmodels.EnabledApplicationsViewModel;

public class EnabledApplicationsPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getString(R.string.enabled_applications));
        setPreferencesFromResource(R.xml.enabled_applications_preferences, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EnabledApplicationsViewModel viewModel = ViewModelProviders.of(getActivity()).get(EnabledApplicationsViewModel.class);
        viewModel.getFilteredCheckBoxPrefsData().observe(getViewLifecycleOwner(), new Observer<List<CheckBoxPreferenceData>>() {
            @Override
            public void onChanged(List<CheckBoxPreferenceData> checkBoxPreferencesData) {
                setPreferencesFromResource(R.xml.enabled_applications_preferences, null);
                PreferenceScreen prefScreen = (PreferenceScreen) findPreference(getString(R.string.enabled_apps_pref_screen));
                for (CheckBoxPreferenceData checkBoxPreferenceData : checkBoxPreferencesData) {
                    CheckBoxPreference c = new CheckBoxPreference(getPreferenceScreen().getContext());
                    c.setKey(checkBoxPreferenceData.key);
                    c.setTitle(checkBoxPreferenceData.title);
                    c.setSummary(checkBoxPreferenceData.summary);
                    c.setIcon(checkBoxPreferenceData.icon);
                    prefScreen.addPreference(c);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        boolean newSettingsValue = sp.getBoolean(key, false);
        String prefEnableDisableAllKey = getString(R.string.preference_enable_disable_all_applications);
        if (key.equals(prefEnableDisableAllKey)) {
            final PreferenceScreen prefScreen = findPreference(getString(R.string.enabled_apps_pref_screen));
            for (int i = 1; i < prefScreen.getPreferenceCount(); i++) {
                ((CheckBoxPreference) (prefScreen.getPreference(i))).setChecked(newSettingsValue);
            }
        }

        // If newSettingsValue == false, clear it from SharedPreferences file to save up some space.
        // Since AN2Linux will default to false if the setting is not found.
        if (!newSettingsValue) {
            sp.edit().remove(key).apply();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.gray_dark));
        return view;
    }

}
