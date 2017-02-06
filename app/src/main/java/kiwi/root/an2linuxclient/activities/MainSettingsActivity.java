/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.activities;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.crypto.KeyGeneratorService;
import kiwi.root.an2linuxclient.preferences.IconSizePreference;
import kiwi.root.an2linuxclient.preferences.MaxMessageSizePreference;
import kiwi.root.an2linuxclient.preferences.MaxTitleSizePreference;
import kiwi.root.an2linuxclient.data.MobileServer;
import kiwi.root.an2linuxclient.data.ServerDatabaseHandler;
import kiwi.root.an2linuxclient.data.WifiServer;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

public class MainSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.main_settings_preferences, false);

        // Display the fragment as the main content.
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
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.main_settings_preferences);
            getActivity().setTheme(R.style.PreferenceFragmentTheme);

            generateKeyIfNotExists();

            findPreference("preference_enable_an2linux").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean) newValue){
                        boolean isNotificationAccessEnabled = NotificationManagerCompat
                                .getEnabledListenerPackages(getActivity())
                                .contains(getActivity().getPackageName());
                        if (!isNotificationAccessEnabled){
                            new AskNotificationAccessDialogFragment().show(getFragmentManager(), "AskNotificationAccessDialogFragment");
                        }
                    }
                    return true;
                }
            });

            final SharedPreferences sharedPrefsDefault = PreferenceManager.getDefaultSharedPreferences(getActivity());
            findPreference("display_test_notification").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    boolean isNotificationAccessEnabled = NotificationManagerCompat
                            .getEnabledListenerPackages(getActivity())
                            .contains(getActivity().getPackageName());
                    if (isNotificationAccessEnabled){
                        boolean globalEnabled = sharedPrefsDefault.getBoolean("preference_enable_an2linux", false);
                        if (globalEnabled){
                            SharedPreferences sharedPrefsEnabledApplications = getActivity()
                                    .getSharedPreferences("enabled_applications", MODE_PRIVATE);
                            boolean appEnabled = sharedPrefsEnabledApplications.getBoolean(getActivity().getPackageName(), false);
                            if (appEnabled){
                                ServerDatabaseHandler dbHandler = ServerDatabaseHandler.getInstance(getActivity());
                                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                                boolean displayTestNotif = false;
                                if (networkInfo != null && networkInfo.isConnected()){
                                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                                        List<WifiServer> enabledWifiServers = dbHandler.getAllEnabledWifiServers();
                                        if (enabledWifiServers.size() == 0){
                                            Toast.makeText(getActivity(), getString(R.string.test_notif_found_no_enabled, getString(R.string.connection_type_wifi)), Toast.LENGTH_SHORT).show();
                                        } else {
                                            boolean found = false;
                                            for(WifiServer wifiServer : enabledWifiServers) {
                                                if (ConnectionHelper.checkIfSsidIsAllowed(wifiServer.getSsidWhitelist(), getActivity())){
                                                    found = true;
                                                    break;
                                                }
                                            }
                                            if (found){
                                                Toast.makeText(getActivity(), getString(R.string.test_notif_testing, getString(R.string.connection_type_wifi)), Toast.LENGTH_SHORT).show();
                                                displayTestNotif = true;
                                            } else {
                                                Toast.makeText(getActivity(), R.string.disallowed_ssid, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                                        List<MobileServer> enabledMobileServers = dbHandler.getAllEnabledMobileServers();
                                        if (enabledMobileServers.size() == 0){
                                            Toast.makeText(getActivity(), getString(R.string.test_notif_found_no_enabled, getString(R.string.connection_type_mobile)), Toast.LENGTH_SHORT).show();
                                        } else {
                                            boolean found = false;
                                            for(MobileServer mobileServer : enabledMobileServers){
                                                if (!networkInfo.isRoaming() || mobileServer.isRoamingAllowed()){
                                                    found = true;
                                                    break;
                                                }
                                            }
                                            if (found){
                                                Toast.makeText(getActivity(), getString(R.string.test_notif_testing, getString(R.string.connection_type_mobile)), Toast.LENGTH_SHORT).show();
                                                displayTestNotif = true;
                                            } else {
                                                Toast.makeText(getActivity(), R.string.you_are_roaming, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }
                                if (BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled()){
                                    if (dbHandler.getAllEnabledBluetoothServers().size() == 0){
                                        Toast.makeText(getActivity(), getString(R.string.test_notif_found_no_enabled, getString(R.string.connection_type_bluetooth)), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), getString(R.string.test_notif_testing, getString(R.string.connection_type_bluetooth)), Toast.LENGTH_SHORT).show();
                                        displayTestNotif = true;
                                    }
                                }
                                dbHandler.close();
                                if (displayTestNotif){
                                    final int NOTIFICATION_ID = 1;
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity().getApplicationContext());
                                    builder.setSmallIcon(R.drawable.ic_stat_tux);
                                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
                                    builder.setContentTitle(getString(R.string.test_notif_title));
                                    builder.setContentText(getString(R.string.test_notif_message));
                                    builder.setSubText(getString(R.string.test_notif_message_line_two));

                                    NotificationManager notificationManager = (NotificationManager) getActivity().
                                            getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

                                    for (int i = 0; i < 1; i++){
                                        notificationManager.notify(NOTIFICATION_ID, builder.build());
                                    }
                                }
                            } else {
                                Toast.makeText(getActivity(), R.string.test_notif_an2linux_not_enabled_in_applications, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.test_notif_an2linux_not_enabled, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), R.string.test_notif_need_access, Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
            });
            findPreference("preference_title_max_size").setSummary(
                    String.valueOf(sharedPrefsDefault.getInt("preference_title_max_size", MaxTitleSizePreference.DEFAULT_VALUE)));
            findPreference("preference_message_max_size").setSummary(
                    String.valueOf(sharedPrefsDefault.getInt("preference_message_max_size", MaxMessageSizePreference.DEFAULT_VALUE)));
            findPreference("preference_icon_size").setSummary(getString(R.string.main_icon_size_summary,
                    sharedPrefsDefault.getInt("preference_icon_size", IconSizePreference.DEFAULT_VALUE)));
            findPreference("license").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new LicenseDialogFragment().show(getFragmentManager(), "LicenseDialogFragment");
                    return true;
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
                PreferenceGroup g = (PreferenceGroup) findPreference("main_settings");
                CheckBoxPreference c = new CheckBoxPreference(getPreferenceScreen().getContext());
                c.setDefaultValue(true);
                c.setKey("preference_block_group");
                c.setTitle(getString(R.string.main_block_group));
                c.setSummary(getString(R.string.main_block_group_summary));
                g.addPreference(c);
                c = new CheckBoxPreference(getPreferenceScreen().getContext());
                c.setDefaultValue(false);
                c.setKey("preference_block_local");
                c.setTitle(getString(R.string.main_block_local));
                c.setSummary(getString(R.string.main_block_local_summary));
                g.addPreference(c);
            }

        }

        private void generateKeyIfNotExists(){
            final SharedPreferences deviceKeyPref = getActivity().getSharedPreferences("device_key_and_cert", MODE_PRIVATE);
            if ((!deviceKeyPref.contains("privatekey") || !deviceKeyPref.contains("certificate"))
                    && !KeyGeneratorService.currentlyGenerating){
                KeyGeneratorService.currentlyGenerating = true;
                getActivity().startService(new Intent(getActivity(), KeyGeneratorService.class));
            }
        }

        public static class AskNotificationAccessDialogFragment extends DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
                builder.setMessage(R.string.main_dialog_ask_notification_access)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                getActivity().startActivity((new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                return builder.create();
            }
        }

        public static class LicenseDialogFragment extends DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
                builder.setMessage(R.string.general_public_license_3)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                return builder.create();
            }
        }
    }

}
