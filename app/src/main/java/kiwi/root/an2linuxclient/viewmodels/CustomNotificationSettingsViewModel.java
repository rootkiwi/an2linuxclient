package kiwi.root.an2linuxclient.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.data.CustomSettingsAppData;

import static android.content.Context.MODE_PRIVATE;

public class CustomNotificationSettingsViewModel extends AndroidViewModel {

    private MutableLiveData<List<CustomSettingsAppData>> appsDataList = new MutableLiveData<>();

    public CustomNotificationSettingsViewModel(@NonNull Application application) {
        super(application);
        loadAppListFromNewThread();
    }

    private void loadAppListFromNewThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadAppList();
            }
        }).start();
    }

    private void loadAppList() {
        Context c = getApplication();
        PackageManager pm = c.getPackageManager();
        SharedPreferences spEnabledApps = c.getSharedPreferences(c.getString(R.string.enabled_applications), MODE_PRIVATE);
        final Map<String, String> enabledAppLabels = new HashMap<>();
        List<ApplicationInfo> enabledApps = new ArrayList<>();
        List<ApplicationInfo> allAppsInfo = pm.getInstalledApplications(0);

        // apparently loadLabel() is very slow so it's faster to do it once and store in map
        for (ApplicationInfo appInfo : allAppsInfo) {
            if (spEnabledApps.getBoolean(appInfo.packageName, false)) {
                enabledApps.add(appInfo);
                enabledAppLabels.put(appInfo.packageName, appInfo.loadLabel(pm).toString());
            }
        }

        Collections.sort(enabledApps, new Comparator<ApplicationInfo>() {
            @Override
            public int compare(ApplicationInfo lhs, ApplicationInfo rhs) {
                return enabledAppLabels.get(lhs.packageName).compareToIgnoreCase(enabledAppLabels.get(rhs.packageName));
            }
        });

        List<CustomSettingsAppData> enabledAppsDataList = new ArrayList<>();
        SharedPreferences spCustomSettings = c.getSharedPreferences(c.getString(R.string.notification_settings_custom), MODE_PRIVATE);
        for (ApplicationInfo appInfo : enabledApps) {
            CustomSettingsAppData appData = new CustomSettingsAppData();
            appData.appIcon = appInfo.loadIcon(pm);
            appData.appName = enabledAppLabels.get(appInfo.packageName);
            appData.packageName = appInfo.packageName;
            boolean usingCustomSettings = spCustomSettings
                    .getBoolean(appInfo.packageName + "_" + c.getString(R.string.preference_use_custom_settings), false);
            appData.isUsingCustomSettings = usingCustomSettings;
            enabledAppsDataList.add(appData);
        }
        appsDataList.postValue(enabledAppsDataList);
    }

    /**
     * When a user have entered a specific apps custom settings, the user may have enabled/disabled
     * using custom settings for that app. Here we check for that and update if necessary
     * so that 'Using custom settings' or not is displayed correctly in the list.
     */
    public void maybeUpdateUsingCustom() {
        Context c = getApplication();
        SharedPreferences spCustomSettings = c.getSharedPreferences(c.getString(R.string.notification_settings_custom), MODE_PRIVATE);
        List<CustomSettingsAppData> updatedAppsDataList = appsDataList.getValue();
        boolean listChanged = false;

        for (int i = 0; i < updatedAppsDataList.size(); i++) {
            CustomSettingsAppData appData = updatedAppsDataList.get(i);

            boolean oldAppDataUsingCustomSettings = appData.isUsingCustomSettings;
            boolean newAppDataUsingCustomSettings = spCustomSettings
                    .getBoolean(appData.packageName + "_" + c.getString(R.string.preference_use_custom_settings), false);

            if (oldAppDataUsingCustomSettings != newAppDataUsingCustomSettings) {
                listChanged = true;
                appData.isUsingCustomSettings = newAppDataUsingCustomSettings;
                break;
            }
        }

        if (listChanged) {
            appsDataList.setValue(updatedAppsDataList);
        }
    }

    public void updateAfterEnabledApplicationSettings() {
        loadAppListFromNewThread();
    }

    public LiveData<List<CustomSettingsAppData>> getAppsDataList() {
        return appsDataList;
    }

}
