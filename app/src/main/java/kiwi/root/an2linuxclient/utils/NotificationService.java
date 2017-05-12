/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.utils;

import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
//import android.os.Bundle;
//import android.util.Log;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.network.NotificationHandler;

public class NotificationService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //logDebug(sbn);
        if (filter(sbn)) {
            NotificationHandler.handleStatusBarNotification(sbn, getApplicationContext());
        }
    }

    private boolean filter(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        if (!globalEnabled() || !appEnabled(packageName)) {
            return false;
        }
        boolean usingCustomSettings = isUsingCustomSettings(packageName);
        SharedPreferences sp;
        if (usingCustomSettings) {
            sp = getSharedPreferences(getString(R.string.notification_settings_custom), MODE_PRIVATE);
        } else {
            sp = getSharedPreferences(getString(R.string.notification_settings_global), MODE_PRIVATE);
        }
        int flags = sbn.getNotification().flags;
        if (blockOngoing(sp, packageName, usingCustomSettings) && isOngoing(flags)){
            return false;
        }
        if (blockForeground(sp, packageName, usingCustomSettings) && isForeground(flags)){
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
            if (blockGroupSummary(sp, packageName, usingCustomSettings) && isGroupSummary(flags)){
                return false;
            }
            if (blockLocalOnly(sp, packageName, usingCustomSettings) && isLocalOnly(flags)){
                return false;
            }
        }
        return priorityAllowed(sp, packageName, usingCustomSettings, sbn.getNotification().priority);
    }

    private boolean globalEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.preference_enable_an2linux), false);
    }

    private boolean appEnabled(String packageName) {
        return getSharedPreferences(getString(R.string.enabled_applications), MODE_PRIVATE)
                .getBoolean(packageName, false);
    }

    private boolean isUsingCustomSettings(String packageName) {
        SharedPreferences sharedPrefsCustom = getSharedPreferences(getString(R.string.notification_settings_custom), MODE_PRIVATE);
        return sharedPrefsCustom.getBoolean(packageName + "_" + getString(R.string.preference_use_custom_settings), false);
    }

    private boolean blockOngoing(SharedPreferences sp, String packageName, boolean usingCustomSettings) {
        return sp.getBoolean(getCorrectPrefKey(
                getString(R.string.preference_block_ongoing), packageName, usingCustomSettings), false);
    }

    private boolean blockForeground(SharedPreferences sp, String packageName, boolean usingCustomSettings) {
        return sp.getBoolean(getCorrectPrefKey(
                getString(R.string.preference_block_foreground), packageName, usingCustomSettings), false);
    }

    private boolean blockGroupSummary(SharedPreferences sp, String packageName, boolean usingCustomSettings) {
        return sp.getBoolean(getCorrectPrefKey(
                getString(R.string.preference_block_group), packageName, usingCustomSettings), false);
    }

    private boolean blockLocalOnly(SharedPreferences sp, String packageName, boolean usingCustomSettings) {
        return sp.getBoolean(getCorrectPrefKey(
                getString(R.string.preference_block_local), packageName, usingCustomSettings), false);
    }

    private boolean priorityAllowed(SharedPreferences sp, String packageName, boolean usingCustomSettings, int priority) {
        int minNotificationPriority = Integer.parseInt(sp.getString(getCorrectPrefKey(
                getString(R.string.preference_min_notification_priority), packageName, usingCustomSettings),
                getString(R.string.preference_min_notification_priority_default)));
        return priority >= minNotificationPriority;
    }

    private String getCorrectPrefKey(String key, String packageName, boolean usingCustomSettings) {
        return usingCustomSettings ? packageName + "_" + key : key;
    }

    private boolean isOngoing(int flags) {
        return (flags & Notification.FLAG_ONGOING_EVENT) != 0;
    }

    private boolean isForeground(int flags) {
        return (flags & Notification.FLAG_FOREGROUND_SERVICE) != 0;
    }

    private boolean isGroupSummary(int flags) {
        return (flags & Notification.FLAG_GROUP_SUMMARY) != 0;
    }

    private boolean isLocalOnly(int flags) {
        return (flags & Notification.FLAG_LOCAL_ONLY) != 0;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    /*private void logDebug(StatusBarNotification sbn){
        Log.d("<<<NEW_NOTIFICATION>>>", "<<<" + sbn.getPackageName() + ">>>");
        Bundle extras = sbn.getNotification().extras;

        if (extras.getCharSequence(android.app.Notification.EXTRA_TITLE) != null){
            Log.d("contentTitle", extras.getCharSequence(android.app.Notification.EXTRA_TITLE).toString());
        } else {
            Log.d("contentTitle", "null");
        }

        if (extras.getCharSequence(android.app.Notification.EXTRA_TEXT) != null){
            Log.d("contentText", extras.getCharSequence(android.app.Notification.EXTRA_TEXT).toString());
        } else {
            Log.d("contentText", "null");
        }

        if (extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT) != null){
            Log.d("subText", extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT).toString());
        } else {
            Log.d("subText", "null");
        }

        Log.d("FLAG_ONGOING_EVENT", String.valueOf((sbn.getNotification().flags & android.app.Notification.FLAG_ONGOING_EVENT) == 2));
        Log.d("FLAG_FOREGROUND_SERVICE", String.valueOf((sbn.getNotification().flags & android.app.Notification.FLAG_FOREGROUND_SERVICE) == 64));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
            Log.d("FLAG_GROUP_SUMMARY", String.valueOf((sbn.getNotification().flags & android.app.Notification.FLAG_GROUP_SUMMARY) == 512));
            Log.d("FLAG_LOCAL_ONLY", String.valueOf((sbn.getNotification().flags & android.app.Notification.FLAG_LOCAL_ONLY) == 256));
        }
        Log.d("priority", String.valueOf(sbn.getNotification().priority));
        Log.d("<<<END_NOTIFICATION>>>", "<<<" + sbn.getPackageName() + ">>>");
    }*/

}
