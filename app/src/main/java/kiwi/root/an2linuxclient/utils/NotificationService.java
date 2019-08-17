/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.utils;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.Display;
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

        if (dontSendIfScreenIsOn(sp, packageName, usingCustomSettings)) {
            boolean screenIsOn = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
                for (Display display : dm.getDisplays()) {
                    if (display.getState() == Display.STATE_ON) {
                        // private as in samsung always-on feature, not sure if this is how it works
                        // https://stackoverflow.com/questions/2474367/how-can-i-tell-if-the-screen-is-on-in-android#comment71534994_17348755
                        boolean displayIsPrivate = (display.getFlags() & Display.FLAG_PRIVATE) == Display.FLAG_PRIVATE;
                        if (!displayIsPrivate) {
                            screenIsOn = true;
                            break;
                        }
                    }
                }
            } else {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                if (powerManager.isScreenOn()){
                    screenIsOn = true;
                }
            }

            if (screenIsOn) {
                return false;
            }
        }

        int flags = sbn.getNotification().flags;
        if (isOngoing(flags) && blockOngoing(sp, packageName, usingCustomSettings)){
            return false;
        }
        if (isForeground(flags) && blockForeground(sp, packageName, usingCustomSettings)){
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
            if (isGroupSummary(flags) && blockGroupSummary(sp, packageName, usingCustomSettings)){
                return false;
            }
            if (isLocalOnly(flags) && blockLocalOnly(sp, packageName, usingCustomSettings)){
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

    private boolean dontSendIfScreenIsOn(SharedPreferences sp, String packageName, boolean usingCustomSettings) {
        return sp.getBoolean(getCorrectPrefKey(
                getString(R.string.preference_dont_send_if_screen_on), packageName, usingCustomSettings), false);
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
