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
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.preferences.IconSizePreference;
import kiwi.root.an2linuxclient.preferences.MaxMessageSizePreference;
import kiwi.root.an2linuxclient.preferences.MaxTitleSizePreference;

public class NotificationSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.notification_preferences, false);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            view.setBackgroundColor(getResources().getColor(R.color.gray_dark));
            return view;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(getString(R.string.notification_settings_global));
            addPreferencesFromResource(R.xml.notification_preferences);
            getActivity().setTheme(R.style.PreferenceFragmentTheme);

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
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
        }

    }

}
