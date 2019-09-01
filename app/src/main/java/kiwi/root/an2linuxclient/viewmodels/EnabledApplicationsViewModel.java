package kiwi.root.an2linuxclient.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

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
import kiwi.root.an2linuxclient.preferences.CheckBoxPreferenceData;

import static android.content.Context.MODE_PRIVATE;

public class EnabledApplicationsViewModel extends AndroidViewModel {

    private MutableLiveData<Boolean> operationRunning = new MutableLiveData<>();
    private List<ApplicationInfo> allAppsInfo;
    private MutableLiveData<List<CheckBoxPreferenceData>> filteredCheckBoxPrefsData = new MutableLiveData<>();
    private String filterTrimmedLowerCase = "";
    private Map<String, String> allAppLabels = new HashMap<>();
    private Map<String, Boolean> appSettings = new HashMap<>();

    public EnabledApplicationsViewModel(Application application) {
        super(application);
        operationRunning.setValue(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadAppList();
                setFilteredCheckBoxPreferences();
                operationRunning.postValue(false);
            }
        }).start();
    }

    private void loadAppList() {
        Context c = getApplication();
        PackageManager pm = c.getPackageManager();
        allAppsInfo = pm.getInstalledApplications(0);

        // apparently loadLabel() is very slow so it's faster to do it once and store in map
        for (ApplicationInfo appInfo : allAppsInfo) {
            allAppLabels.put(appInfo.packageName, appInfo.loadLabel(pm).toString());
        }
    }

    private void updateAppSettings() {
        Context c = getApplication();
        SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.enabled_applications), MODE_PRIVATE);
        appSettings.clear();
        for (ApplicationInfo appInfo : allAppsInfo) {
            appSettings.put(appInfo.packageName, sp.getBoolean(appInfo.packageName, false));
        }
    }

    private void setFilteredCheckBoxPreferences() {
        List<ApplicationInfo> filteredAppInfos;
        if (filterTrimmedLowerCase.isEmpty()) {
            filteredAppInfos = allAppsInfo;
        } else {
            filteredAppInfos = new ArrayList<>();
            for (ApplicationInfo appInfo : allAppsInfo) {
                String appLabel = allAppLabels.get(appInfo.packageName).toLowerCase();
                if (appLabel.startsWith(filterTrimmedLowerCase)) {
                    filteredAppInfos.add(appInfo);
                }
            }
        }

        updateAppSettings();
        Collections.sort(filteredAppInfos, new Comparator<ApplicationInfo>() {
            @Override
            public int compare(ApplicationInfo lhs, ApplicationInfo rhs) {
                boolean lhsEnabled = appSettings.get(lhs.packageName);
                boolean rhsEnabled = appSettings.get(rhs.packageName);
                if (lhsEnabled && !rhsEnabled) {
                    return -1;
                }
                else if (!lhsEnabled && rhsEnabled) {
                    return 1;
                }
                else {
                    return allAppLabels.get(lhs.packageName).compareToIgnoreCase(allAppLabels.get(rhs.packageName));
                }
            }
        });

        PackageManager pm = getApplication().getPackageManager();
        List<CheckBoxPreferenceData> checkBoxPreferencesData = new ArrayList<>();
        for (ApplicationInfo appInfo : filteredAppInfos) {
            CheckBoxPreferenceData preferenceData = new CheckBoxPreferenceData();
            preferenceData.key = appInfo.packageName;
            preferenceData.title = allAppLabels.get(appInfo.packageName);
            preferenceData.summary = appInfo.packageName;
            preferenceData.icon = appInfo.loadIcon(pm);
            checkBoxPreferencesData.add(preferenceData);
        }

        filteredCheckBoxPrefsData.postValue(checkBoxPreferencesData);
    }

    public void filterTextChanged(String newFilterTrimmedLowerCase) {
        String oldFilter = filterTrimmedLowerCase;
        if (!oldFilter.equals(newFilterTrimmedLowerCase)) {
            filterTrimmedLowerCase = newFilterTrimmedLowerCase;
            setFilteredCheckBoxPreferences();
        }
    }

    public LiveData<Boolean> getOperationRunning() {
        return operationRunning;
    }

    public LiveData<List<CheckBoxPreferenceData>> getFilteredCheckBoxPrefsData() {
        return filteredCheckBoxPrefsData;
    }

}
