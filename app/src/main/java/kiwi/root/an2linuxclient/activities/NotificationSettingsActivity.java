/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.preferences.IconSizePreference;
import kiwi.root.an2linuxclient.preferences.MaxMessageSizePreference;
import kiwi.root.an2linuxclient.preferences.MaxTitleSizePreference;

public class NotificationSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.notification_preferences, false);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(getString(R.string.notification_settings_global));
            addPreferencesFromResource(R.xml.notification_preferences);

            SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.notification_settings_global), MODE_PRIVATE);

            String prefKeyMaxTitle = getString(R.string.preference_title_max_size);
            findPreference(prefKeyMaxTitle).setSummary(String.valueOf(
                    sp.getInt(prefKeyMaxTitle, MaxTitleSizePreference.DEFAULT_VALUE)));

            String prefKeyMaxMessage = getString(R.string.preference_message_max_size);
            findPreference(prefKeyMaxMessage)
                    .setSummary(String.valueOf(sp.getInt(prefKeyMaxMessage, MaxMessageSizePreference.DEFAULT_VALUE)));

            String prefKeyIconSize = getString(R.string.preference_icon_size);
            findPreference(prefKeyIconSize).setSummary(getString(
                            R.string.main_icon_size_summary,
                            sp.getInt(prefKeyIconSize, IconSizePreference.DEFAULT_VALUE)));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                PreferenceGroup g = (PreferenceGroup) findPreference(getString(R.string.notification_settings_global_category));
                CheckBoxPreference c = new CheckBoxPreference(getPreferenceScreen().getContext());
                c.setDefaultValue(false);
                c.setKey(getString(R.string.preference_block_group));
                c.setTitle(getString(R.string.main_block_group));
                c.setSummary(getString(R.string.main_block_group_summary));
                g.addPreference(c);
                c = new CheckBoxPreference(getPreferenceScreen().getContext());
                c.setDefaultValue(false);
                c.setKey(getString(R.string.preference_block_local));
                c.setTitle(getString(R.string.main_block_local));
                c.setSummary(getString(R.string.main_block_local_summary));
                g.addPreference(c);
            }

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
                CheckBoxPreference forceTitleAppNamePref = findPreference(getString(R.string.preference_force_title));
                forceTitleAppNamePref.setSummary(getString(R.string.pref_force_appname_info_extraction_unsupported));
                forceTitleAppNamePref.setDefaultValue(true);
                forceTitleAppNamePref.setChecked(true);
                forceTitleAppNamePref.setEnabled(false);

                CheckBoxPreference includeMessagePref = findPreference(getString(R.string.preference_include_notification_message));
                includeMessagePref.setSummary(getString(R.string.pref_message_info_extraction_unsupported_version));
                includeMessagePref.setDefaultValue(false);
                includeMessagePref.setChecked(false);
                includeMessagePref.setEnabled(false);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            view.setBackgroundColor(getResources().getColor(R.color.gray_dark));
            return view;
        }
    }

}
