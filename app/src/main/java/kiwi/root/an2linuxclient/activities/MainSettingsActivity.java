/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.activities;

import android.Manifest;
import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.crypto.KeyGeneratorService;
import kiwi.root.an2linuxclient.data.MobileServer;
import kiwi.root.an2linuxclient.data.ServerDatabaseHandler;
import kiwi.root.an2linuxclient.data.WifiServer;
import kiwi.root.an2linuxclient.utils.AN2LinuxService;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

import java.util.List;

public class MainSettingsActivity extends AppCompatActivity {
    private final static int HIDING_NOTIFICATION_ID = 2;
    private final static String INFORMATION_CHANNEL_ID = "AN2LinuxInformationChannel";

    private boolean useForegroundService = false;

    @Override
    protected void onResume() {
        super.onResume();

        if (useForegroundService){
            startService(new Intent(this, AN2LinuxService.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.main_preferences, false);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        if (useForegroundService){
            startService(new Intent(this, AN2LinuxService.class));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        NotificationChannel chan = new NotificationChannel(INFORMATION_CHANNEL_ID,
                getString(R.string.main_enable_service_information_notification_channel_name), NotificationManager.IMPORTANCE_LOW);
        chan.setLightColor(Color.GREEN);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(chan);

        return INFORMATION_CHANNEL_ID;
    }

    private void displayNotificationHidingHelp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, createNotificationChannel());

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
            notificationManager.notify(HIDING_NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private void setUseForegroundService(boolean useForegroundService) {
        this.useForegroundService = useForegroundService;
        if (useForegroundService){
            startService(new Intent(this, AN2LinuxService.class));
            displayNotificationHidingHelp();
        } else{
            stopService(new Intent(this, AN2LinuxService.class));
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
            addPreferencesFromResource(R.xml.main_preferences);
            getActivity().setTheme(R.style.PreferenceFragmentTheme);

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
                        if (!hasCoarseLocationPermission) {
                            new AskCoarseLocationAccessDialogFragment().show(getFragmentManager(), "AskCoarseLocationAccessDialogFragment");
                        }
                    }
                    return true;
                }
            });

            findPreference(getString(R.string.preference_enable_service)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Activity activity = getActivity();
                    if (activity instanceof MainSettingsActivity){
                        ((MainSettingsActivity) activity).setUseForegroundService((boolean) newValue);
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

            try {
                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                findPreference(getString(R.string.main_changelog_key)).setSummary(String.format("%s (%d)", packageInfo.versionName, packageInfo.versionCode));
            } catch (PackageManager.NameNotFoundException e){}
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
    }

}
