/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.activities;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import kiwi.root.an2linuxclient.BuildConfig;
import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.crypto.KeyGeneratorService;
import kiwi.root.an2linuxclient.data.MobileServer;
import kiwi.root.an2linuxclient.data.ServerDatabaseHandler;
import kiwi.root.an2linuxclient.data.WifiServer;
import kiwi.root.an2linuxclient.utils.AN2LinuxService;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

import java.util.List;

import static kiwi.root.an2linuxclient.App.NOTIFICATION_ID_HIDE_FOREGROUND_NOTIF;
import static kiwi.root.an2linuxclient.App.CHANNEL_ID_INFORMATION;
import static kiwi.root.an2linuxclient.App.NOTIFICATION_ID_TEST_NOTIF;

public class MainSettingsActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();

        boolean an2linuxEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.preference_enable_an2linux), false);

        if (an2linuxEnabled){
            boolean useForegroundService = PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(getString(R.string.preference_enable_service), false);

            if (useForegroundService){
                startService(new Intent(this, AN2LinuxService.class));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.main_preferences, false);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    private void displayNotificationHidingHelp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_INFORMATION);

            notificationBuilder.setCategory(Notification.CATEGORY_MESSAGE);
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            notificationBuilder.setTicker(getString(R.string.main_enable_service_information_notification_title));
            notificationBuilder.setContentIntent(
                    PendingIntent.getActivity(this, 0,
                            new Intent(this, MainSettingsActivity.class),
                            PendingIntent.FLAG_UPDATE_CURRENT)
            );
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setContentTitle(getString(R.string.main_enable_service_information_notification_title));
            notificationBuilder.setContentText(getString(R.string.main_enable_service_information_notification_text));
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.main_enable_service_information_notification_text)));

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID_HIDE_FOREGROUND_NOTIF, notificationBuilder.build());
        }
    }

    private void changeForegroundService(boolean useForegroundService) {
        boolean an2linuxEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.preference_enable_an2linux), false);

        if (an2linuxEnabled && useForegroundService){
            startService(new Intent(this, AN2LinuxService.class));
            displayNotificationHidingHelp();
        } else{
            stopService(new Intent(this, AN2LinuxService.class));
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.main_preferences);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            view.setBackgroundColor(getResources().getColor(R.color.gray_dark));
            return view;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            generateKeyIfNotExists();
            showChangeLogIfNotSeen();

            findPreference(getString(R.string.preference_enable_an2linux)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean) newValue){
                        boolean isNotificationAccessEnabled = NotificationManagerCompat
                                .getEnabledListenerPackages(getActivity())
                                .contains(getActivity().getPackageName());
                        boolean hasCoarseLocationPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.O ||
                                ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

                        if (!isNotificationAccessEnabled){
                            new AskNotificationAccessDialogFragment().show(getFragmentManager(), "AskNotificationAccessDialogFragment");
                        }
                        if (!hasCoarseLocationPermission){
                            new AskCoarseLocationAccessDialogFragment().show(getFragmentManager(), "AskCoarseLocationAccessDialogFragment");
                        }

                        boolean useForegroundService = preference.getSharedPreferences().getBoolean(getString(R.string.preference_enable_service), false);

                        if (useForegroundService){
                            getActivity().startService(new Intent(getActivity(), AN2LinuxService.class));
                        }
                    } else{
                        getActivity().stopService(new Intent(getActivity(), AN2LinuxService.class));
                    }
                    return true;
                }
            });

            findPreference(getString(R.string.preference_enable_service)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Activity activity = getActivity();
                    if (activity instanceof MainSettingsActivity){
                        ((MainSettingsActivity) activity).changeForegroundService((boolean) newValue);
                    }
                    return true;
                }
            });

            final SharedPreferences sharedPrefsDefault = PreferenceManager.getDefaultSharedPreferences(getActivity());
            findPreference(getString(R.string.main_display_test_notification_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    boolean isNotificationAccessEnabled = NotificationManagerCompat
                            .getEnabledListenerPackages(getActivity())
                            .contains(getActivity().getPackageName());
                    if (isNotificationAccessEnabled){
                        boolean globalEnabled = sharedPrefsDefault.getBoolean(getString(R.string.preference_enable_an2linux), false);
                        if (globalEnabled){
                            SharedPreferences sharedPrefsEnabledApplications = getActivity()
                                    .getSharedPreferences(getString(R.string.enabled_applications), MODE_PRIVATE);
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
                                if (displayTestNotif) {
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                            getActivity().getApplicationContext(),
                                            CHANNEL_ID_INFORMATION
                                    );
                                    builder.setSmallIcon(R.mipmap.ic_launcher);
                                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
                                    builder.setContentTitle(getString(R.string.test_notif_title));
                                    builder.setContentText(getString(R.string.test_notif_message));
                                    builder.setSubText(getString(R.string.test_notif_message_line_two));

                                    NotificationManager notificationManager = (NotificationManager) getActivity().
                                            getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

                                    for (int i = 0; i < 1; i++){
                                        notificationManager.notify(NOTIFICATION_ID_TEST_NOTIF, builder.build());
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

            findPreference(getString(R.string.main_license_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new LicenseDialogFragment().show(getFragmentManager(), "LicenseDialogFragment");
                    return true;
                }
            });
            findPreference(getString(R.string.main_changelog_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ChangelogDialogFragment().show(getFragmentManager(), "ChangelogDialogFragment");
                    return true;
                }
            });
            findPreference(getString(R.string.main_changelog_key))
                    .setSummary(String.format("%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        }

        private void showChangeLogIfNotSeen(){
            PackageInfo packageInfo;
            try {
                packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e){
                return;
            }
            int versionCode = packageInfo.versionCode;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sp.getInt(getString(R.string.version_code_seen), 0) < versionCode) {
                sp.edit().putInt(getString(R.string.version_code_seen), versionCode).apply();
                new ChangelogDialogFragment().show(getFragmentManager(), "ChangelogDialogFragment");
            }
        }

        private void generateKeyIfNotExists(){
            SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.device_key_and_cert), MODE_PRIVATE);
            boolean certExists = sp.contains(getString(R.string.certificate));
            boolean keyExists = sp.contains(getString(R.string.privatekey));
            boolean certOrKeyNotExists = !certExists || !keyExists;
            if (certOrKeyNotExists && !KeyGeneratorService.currentlyGenerating){
                KeyGeneratorService.startGenerate(getActivity());
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

        public static class AskCoarseLocationAccessDialogFragment extends DialogFragment {
            static int UNIQUE_COARSE_LOCATION_ID  = 0; // WTF is this not a normal constant?

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
                builder.setMessage(R.string.main_dialog_ask_coarse_location_access)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        UNIQUE_COARSE_LOCATION_ID);
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

        public static class ChangelogDialogFragment extends DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
                builder.setMessage(Html.fromHtml(getString(R.string.changelog)))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                return builder.create();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            /*
            This is required if the user left the app but it's still running and the user uses the
            tile to change the setting and then reopens the app. Without this the checked state will
            be wrong. Without this it's required to unregister the listener below in onDestroy
            instead of onPause.
             */
            updateAn2linuxEnabled(PreferenceManager.getDefaultSharedPreferences(getActivity()));

            /*
            The listener: Preference.OnPreferenceChangeListener in onCreate does not notify updates
            from the quicksettings tile. For that this listener is required, if this is not used
            the state in the app will not be updated if the user changes the tile while the app
            is still running. It will only be updated after onDestroy / onCreate cycle.
             */
            PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            String prefEnableAn2linuxKey = getString(R.string.preference_enable_an2linux);
            if (key.equals(prefEnableAn2linuxKey)) {
                updateAn2linuxEnabled(sp);
            }
        }

        private void updateAn2linuxEnabled(SharedPreferences sp) {
            String prefEnableAn2linuxKey = getString(R.string.preference_enable_an2linux);
            boolean currentSetting = sp.getBoolean(prefEnableAn2linuxKey, false);
            ((SwitchPreferenceCompat) findPreference(prefEnableAn2linuxKey)).setChecked(currentSetting);
        }

    }

}
