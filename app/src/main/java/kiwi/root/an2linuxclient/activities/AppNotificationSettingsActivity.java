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
        SharedPreferences sp = getSharedPreferences(getString(R.string.notification_settings_custom), MODE_PRIVATE);
        String packageNameUnderscore = packageName + "_";
        boolean usingCustomSettings = sp.getBoolean(packageNameUnderscore + getString(R.string.preference_use_custom_settings), false);
        if (!usingCustomSettings){ // clear settings if not using them
            SharedPreferences.Editor edit = sp.edit();
            edit.remove(packageNameUnderscore + getString(R.string.preference_include_notification_title));
            edit.remove(packageNameUnderscore + getString(R.string.preference_force_title));
            edit.remove(packageNameUnderscore + getString(R.string.preference_title_max_size));
            edit.remove(packageNameUnderscore + getString(R.string.preference_include_notification_message));
            edit.remove(packageNameUnderscore + getString(R.string.preference_message_max_size));
            edit.remove(packageNameUnderscore + getString(R.string.preference_include_notification_icon));
            edit.remove(packageNameUnderscore + getString(R.string.preference_icon_size));
            edit.remove(packageNameUnderscore + getString(R.string.preference_block_ongoing));
            edit.remove(packageNameUnderscore + getString(R.string.preference_block_foreground));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                edit.remove(packageNameUnderscore + getString(R.string.preference_block_group));
                edit.remove(packageNameUnderscore + getString(R.string.preference_block_local));
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
            getPreferenceManager().setSharedPreferencesName(getString(R.string.notification_settings_custom));
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
            SharedPreferences prefsGeneral = getActivity().getSharedPreferences(getString(R.string.notification_settings_global), MODE_PRIVATE);
            String packageNameUnderscore = packageName + "_";
            String prefKeyUseCustomSettings = packageNameUnderscore + getString(R.string.preference_use_custom_settings);

            CheckBoxPreference useCustomSettings = new CheckBoxPreference(getPreferenceScreen().getContext());
            useCustomSettings.setWidgetLayoutResource(R.layout.appcompat_switch_layout);
            useCustomSettings.setDefaultValue(false);
            useCustomSettings.setKey(prefKeyUseCustomSettings);
            useCustomSettings.setTitle(R.string.notif_custom_use_custom_title);
            screen.addPreference(useCustomSettings);

            CheckBoxPreference includeTitle = new CheckBoxPreference(getPreferenceScreen().getContext());
            String prefKeyIncludeTitle = getString(R.string.preference_include_notification_title);
            includeTitle.setDefaultValue(prefsGeneral.getBoolean(prefKeyIncludeTitle, true));
            includeTitle.setKey(packageNameUnderscore + prefKeyIncludeTitle);
            includeTitle.setTitle(getString(R.string.main_include_title));
            screen.addPreference(includeTitle);
            includeTitle.setDependency(prefKeyUseCustomSettings);

            CheckBoxPreference forceTitle = new CheckBoxPreference(getPreferenceScreen().getContext());
            String prefKeyForceTitle = getString(R.string.preference_force_title);
            forceTitle.setDefaultValue(prefsGeneral.getBoolean(prefKeyForceTitle, false));
            forceTitle.setKey(packageNameUnderscore + prefKeyForceTitle);
            forceTitle.setTitle(getString(R.string.main_force_title));
            screen.addPreference(forceTitle);
            forceTitle.setDependency(packageNameUnderscore + prefKeyIncludeTitle);

            MaxTitleSizePreference maxTitle = new MaxTitleSizePreference(getPreferenceScreen().getContext(), null);
            String prefKeyMaxTitle = getString(R.string.preference_title_max_size);
            maxTitle.setDefaultValue(prefsGeneral.getInt(prefKeyMaxTitle, MaxTitleSizePreference.DEFAULT_VALUE));
            maxTitle.setDialogMessage(getString(R.string.main_max_title_dialog_message));
            maxTitle.setKey(packageNameUnderscore + prefKeyMaxTitle);
            maxTitle.setTitle(getString(R.string.main_max_title));
            screen.addPreference(maxTitle);
            maxTitle.setDependency(packageNameUnderscore + prefKeyIncludeTitle);

            CheckBoxPreference includeMessage = new CheckBoxPreference(getPreferenceScreen().getContext());
            String prefKeyIncludeMessage = getString(R.string.preference_include_notification_message);
            includeMessage.setDefaultValue(prefsGeneral.getBoolean(prefKeyIncludeMessage, true));
            includeMessage.setKey(packageNameUnderscore + prefKeyIncludeMessage);
            includeMessage.setTitle(getString(R.string.main_include_message));
            screen.addPreference(includeMessage);
            includeMessage.setDependency(prefKeyUseCustomSettings);

            MaxMessageSizePreference maxMessage = new MaxMessageSizePreference(getPreferenceScreen().getContext(), null);
            String prefKeyMaxMessage = getString(R.string.preference_message_max_size);
            maxMessage.setDefaultValue(prefsGeneral.getInt(prefKeyMaxMessage, MaxMessageSizePreference.DEFAULT_VALUE));
            maxMessage.setDialogMessage(getString(R.string.main_max_message_dialog_message));
            maxMessage.setKey(packageNameUnderscore + prefKeyMaxMessage);
            maxMessage.setTitle(getString(R.string.main_max_message));
            screen.addPreference(maxMessage);
            maxMessage.setDependency(packageNameUnderscore + prefKeyIncludeMessage);

            CheckBoxPreference includeIcon = new CheckBoxPreference(getPreferenceScreen().getContext());
            String prefKeyIncludeIcon = getString(R.string.preference_include_notification_icon);
            includeIcon.setDefaultValue(prefsGeneral.getBoolean(prefKeyIncludeIcon, true));
            includeIcon.setKey(packageNameUnderscore + prefKeyIncludeIcon);
            includeIcon.setTitle(getString(R.string.main_include_icon));
            screen.addPreference(includeIcon);
            includeIcon.setDependency(prefKeyUseCustomSettings);

            IconSizePreference iconSize = new IconSizePreference(getPreferenceScreen().getContext(), null);
            String prefKeyIconSize = getString(R.string.preference_icon_size);
            iconSize.setDefaultValue(prefsGeneral.getInt(prefKeyIconSize, IconSizePreference.DEFAULT_VALUE));
            iconSize.setDialogMessage(getString(R.string.main_icon_size_dialog_message));
            iconSize.setKey(packageNameUnderscore + prefKeyIconSize);
            iconSize.setTitle(getString(R.string.main_icon_size_dialog_message));
            screen.addPreference(iconSize);
            iconSize.setDependency(packageNameUnderscore + prefKeyIncludeIcon);

            CheckBoxPreference blockOngoing = new CheckBoxPreference(getPreferenceScreen().getContext());
            String prefKeyBlockOngoing = getString(R.string.preference_block_ongoing);
            blockOngoing.setDefaultValue(prefsGeneral.getBoolean(prefKeyBlockOngoing, false));
            blockOngoing.setKey(packageNameUnderscore + prefKeyBlockOngoing);
            blockOngoing.setTitle(getString(R.string.main_block_ongoing));
            blockOngoing.setSummary(getString(R.string.main_block_ongoing_summary));
            screen.addPreference(blockOngoing);
            blockOngoing.setDependency(prefKeyUseCustomSettings);

            CheckBoxPreference blockForeground = new CheckBoxPreference(getPreferenceScreen().getContext());
            String prefKeyBlockForeground = getString(R.string.preference_block_foreground);
            blockForeground.setDefaultValue(prefsGeneral.getBoolean(prefKeyBlockForeground, false));
            blockForeground.setKey(packageNameUnderscore + prefKeyBlockForeground);
            blockForeground.setTitle(getString(R.string.main_block_foreground));
            screen.addPreference(blockForeground);
            blockForeground.setDependency(prefKeyUseCustomSettings);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
                CheckBoxPreference blockGroup = new CheckBoxPreference(getPreferenceScreen().getContext());
                String prefKeyBlockGroup = getString(R.string.preference_block_group);
                blockGroup.setDefaultValue(prefsGeneral.getBoolean(prefKeyBlockGroup, false));
                blockGroup.setKey(packageNameUnderscore + prefKeyBlockGroup);
                blockGroup.setTitle(getString(R.string.main_block_group));
                blockGroup.setSummary(getString(R.string.main_block_group_summary));
                screen.addPreference(blockGroup);
                blockGroup.setDependency(prefKeyUseCustomSettings);

                CheckBoxPreference blockLocal = new CheckBoxPreference(getPreferenceScreen().getContext());
                String prefKeyBlockLocal = getString(R.string.preference_block_local);
                blockLocal.setDefaultValue(prefsGeneral.getBoolean(prefKeyBlockLocal, false));
                blockLocal.setKey(packageNameUnderscore + prefKeyBlockLocal);
                blockLocal.setTitle(getString(R.string.main_block_local));
                blockLocal.setSummary(getString(R.string.main_block_local_summary));
                screen.addPreference(blockLocal);
                blockLocal.setDependency(prefKeyUseCustomSettings);
            }

            setIconAndSummaries(packageNameUnderscore);
        }

        private void setIconAndSummaries(String packageNameUnderscore){
            String prefKeyUseCustomSettings = packageNameUnderscore + getString(R.string.preference_use_custom_settings);
            try {
                findPreference(prefKeyUseCustomSettings).setIcon(getActivity().getPackageManager().getApplicationIcon(packageName));
            } catch (Exception e){}
            findPreference(prefKeyUseCustomSettings).setSummary(getString(R.string.notif_custom_use_custom_summary, appName));
            SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.notification_settings_custom), MODE_PRIVATE);

            String prefKeyMaxTitle = packageNameUnderscore + getString(R.string.preference_title_max_size);
            findPreference(prefKeyMaxTitle).setSummary(String.valueOf(
                    sp.getInt(prefKeyMaxTitle, MaxTitleSizePreference.DEFAULT_VALUE)));

            String prefKeyMaxMessage = packageNameUnderscore + getString(R.string.preference_message_max_size);
            findPreference(prefKeyMaxMessage).setSummary(
                    String.valueOf(sp.getInt(prefKeyMaxMessage, MaxMessageSizePreference.DEFAULT_VALUE)));

            String prefKeyIconSize = packageNameUnderscore + getString(R.string.preference_icon_size);
            findPreference(prefKeyIconSize).setSummary(getString(
                    R.string.main_icon_size_summary,
                    sp.getInt(prefKeyIconSize, IconSizePreference.DEFAULT_VALUE)));
        }

    }

}
