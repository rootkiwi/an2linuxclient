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
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.preferences.IconSizePreference;
import kiwi.root.an2linuxclient.preferences.MaxMessageSizePreference;
import kiwi.root.an2linuxclient.preferences.MaxTitleSizePreference;

public class AppNotificationSettingsActivity extends AppCompatActivity {

    private static String appName;
    private static String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appName = getIntent().getStringExtra("appName");
        packageName = getIntent().getStringExtra("packageName");
        setTitle(appName);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = getSharedPreferences("notification_settings_custom", MODE_PRIVATE);
        String packageNameUnderscore = packageName + "_";
        boolean usingCustomSettings = sp.getBoolean(packageNameUnderscore + "preference_use_custom_settings", false);
        if (!usingCustomSettings){ // clear settings if not using them
            SharedPreferences.Editor edit = sp.edit();
            edit.remove(packageNameUnderscore + "preference_include_notification_title");
            edit.remove(packageNameUnderscore + "preference_force_title");
            edit.remove(packageNameUnderscore + "preference_title_max_size");
            edit.remove(packageNameUnderscore + "preference_include_notification_message");
            edit.remove(packageNameUnderscore + "preference_message_max_size");
            edit.remove(packageNameUnderscore + "preference_include_notification_icon");
            edit.remove(packageNameUnderscore + "preference_icon_size");
            edit.remove(packageNameUnderscore + "preference_block_ongoing");
            edit.remove(packageNameUnderscore + "preference_block_foreground");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                edit.remove(packageNameUnderscore + "preference_block_group");
                edit.remove(packageNameUnderscore + "preference_block_local");
            }
            edit.apply();
        }
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
            getPreferenceManager().setSharedPreferencesName("notification_settings_custom");
            getActivity().setTheme(R.style.PreferenceFragmentTheme);
        }

        @Override
        public void onStart() {
            super.onStart();
            initScreenAndPreferences();
        }

        private void initScreenAndPreferences(){
            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(screen);
            SharedPreferences prefsGeneral = getActivity().getSharedPreferences("notification_settings_global", MODE_PRIVATE);
            String packageNameUnderscore = packageName + "_";

            CheckBoxPreference useCustomSettings = new CheckBoxPreference(getPreferenceScreen().getContext());
            useCustomSettings.setWidgetLayoutResource(R.layout.appcompat_switch_layout);
            useCustomSettings.setDefaultValue(false);
            useCustomSettings.setKey(packageNameUnderscore + "preference_use_custom_settings");
            useCustomSettings.setTitle(R.string.notif_custom_use_custom_title);
            screen.addPreference(useCustomSettings);

            CheckBoxPreference includeTitle = new CheckBoxPreference(getPreferenceScreen().getContext());
            includeTitle.setDefaultValue(prefsGeneral.getBoolean("preference_include_notification_title", true));
            includeTitle.setKey(packageNameUnderscore + "preference_include_notification_title");
            includeTitle.setTitle(getString(R.string.main_include_title));
            screen.addPreference(includeTitle);
            includeTitle.setDependency(packageNameUnderscore + "preference_use_custom_settings");

            CheckBoxPreference forceTitle = new CheckBoxPreference(getPreferenceScreen().getContext());
            forceTitle.setDefaultValue(prefsGeneral.getBoolean("preference_force_title", false));
            forceTitle.setKey(packageNameUnderscore + "preference_force_title");
            forceTitle.setTitle(getString(R.string.main_force_title));
            screen.addPreference(forceTitle);
            forceTitle.setDependency(packageNameUnderscore + "preference_include_notification_title");

            MaxTitleSizePreference maxTitle = new MaxTitleSizePreference(getPreferenceScreen().getContext(), null);
            maxTitle.setDefaultValue(prefsGeneral.getInt("preference_title_max_size", MaxTitleSizePreference.DEFAULT_VALUE));
            maxTitle.setKey(packageNameUnderscore + "preference_title_max_size");
            maxTitle.setTitle(getString(R.string.main_max_title));
            screen.addPreference(maxTitle);
            maxTitle.setDependency(packageNameUnderscore + "preference_include_notification_title");

            CheckBoxPreference includeMessage = new CheckBoxPreference(getPreferenceScreen().getContext());
            includeMessage.setDefaultValue(prefsGeneral.getBoolean("preference_include_notification_message", true));
            includeMessage.setKey(packageNameUnderscore + "preference_include_notification_message");
            includeMessage.setTitle(getString(R.string.main_include_message));
            screen.addPreference(includeMessage);
            includeMessage.setDependency(packageNameUnderscore + "preference_use_custom_settings");

            MaxMessageSizePreference maxMessage = new MaxMessageSizePreference(getPreferenceScreen().getContext(), null);
            maxMessage.setDefaultValue(prefsGeneral.getInt("preference_message_max_size", MaxMessageSizePreference.DEFAULT_VALUE));
            maxMessage.setKey(packageNameUnderscore + "preference_message_max_size");
            maxMessage.setTitle(getString(R.string.main_max_message));
            screen.addPreference(maxMessage);
            maxMessage.setDependency(packageNameUnderscore + "preference_include_notification_message");

            CheckBoxPreference includeIcon = new CheckBoxPreference(getPreferenceScreen().getContext());
            includeIcon.setDefaultValue(prefsGeneral.getBoolean("preference_include_notification_icon", true));
            includeIcon.setKey(packageNameUnderscore + "preference_include_notification_icon");
            includeIcon.setTitle(getString(R.string.main_include_icon));
            screen.addPreference(includeIcon);
            includeIcon.setDependency(packageNameUnderscore + "preference_use_custom_settings");

            IconSizePreference iconSize = new IconSizePreference(getPreferenceScreen().getContext(), null);
            iconSize.setDefaultValue(prefsGeneral.getInt("preference_icon_size", IconSizePreference.DEFAULT_VALUE));
            iconSize.setKey(packageNameUnderscore + "preference_icon_size");
            iconSize.setTitle(getString(R.string.main_icon_size_dialog_message));
            screen.addPreference(iconSize);
            iconSize.setDependency(packageNameUnderscore + "preference_include_notification_icon");

            CheckBoxPreference blockOngoing = new CheckBoxPreference(getPreferenceScreen().getContext());
            blockOngoing.setDefaultValue(prefsGeneral.getBoolean("preference_block_ongoing", false));
            blockOngoing.setKey(packageNameUnderscore + "preference_block_ongoing");
            blockOngoing.setTitle(getString(R.string.main_block_ongoing));
            screen.addPreference(blockOngoing);
            blockOngoing.setDependency(packageNameUnderscore + "preference_use_custom_settings");

            CheckBoxPreference blockForeground = new CheckBoxPreference(getPreferenceScreen().getContext());
            blockForeground.setDefaultValue(prefsGeneral.getBoolean("preference_block_foreground", false));
            blockForeground.setKey(packageNameUnderscore + "preference_block_foreground");
            blockForeground.setTitle(getString(R.string.main_block_foreground));
            screen.addPreference(blockForeground);
            blockForeground.setDependency(packageNameUnderscore + "preference_use_custom_settings");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
                CheckBoxPreference blockGroup = new CheckBoxPreference(getPreferenceScreen().getContext());
                blockGroup.setDefaultValue(prefsGeneral.getBoolean("preference_block_group", false));
                blockGroup.setKey(packageNameUnderscore + "preference_block_group");
                blockGroup.setTitle(getString(R.string.main_block_group));
                blockGroup.setSummary(getString(R.string.main_block_group_summary));
                screen.addPreference(blockGroup);
                blockGroup.setDependency(packageNameUnderscore + "preference_use_custom_settings");

                CheckBoxPreference blockLocal = new CheckBoxPreference(getPreferenceScreen().getContext());
                blockLocal.setDefaultValue(prefsGeneral.getBoolean("preference_block_local", false));
                blockLocal.setKey(packageNameUnderscore + "preference_block_local");
                blockLocal.setTitle(getString(R.string.main_block_local));
                blockLocal.setSummary(getString(R.string.main_block_local_summary));
                screen.addPreference(blockLocal);
                blockLocal.setDependency(packageNameUnderscore + "preference_use_custom_settings");
            }

            setIconAndSummaries(packageNameUnderscore);
        }

        private void setIconAndSummaries(String packageNameUnderscore){
            try {
                findPreference(packageNameUnderscore + "preference_use_custom_settings").setIcon(getActivity().getPackageManager().getApplicationIcon(packageName));
            } catch (Exception e){}
            findPreference(packageNameUnderscore + "preference_use_custom_settings").setSummary(getString(R.string.notif_custom_use_custom_summary, appName));
            SharedPreferences sp = getActivity().getSharedPreferences("notification_settings_custom", MODE_PRIVATE);
            findPreference(packageNameUnderscore + "preference_title_max_size").setSummary(
                    String.valueOf(sp.getInt(packageNameUnderscore + "preference_title_max_size", MaxTitleSizePreference.DEFAULT_VALUE)));
            findPreference(packageNameUnderscore + "preference_message_max_size").setSummary(
                    String.valueOf(sp.getInt(packageNameUnderscore + "preference_message_max_size", MaxMessageSizePreference.DEFAULT_VALUE)));
            findPreference(packageNameUnderscore + "preference_icon_size").setSummary(getString(R.string.main_icon_size_summary,
                    sp.getInt(packageNameUnderscore + "preference_icon_size", IconSizePreference.DEFAULT_VALUE)));
        }

    }

}
