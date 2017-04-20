/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.data;

import android.content.Context;
import android.content.SharedPreferences;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.preferences.IconSizePreference;
import kiwi.root.an2linuxclient.preferences.MaxMessageSizePreference;
import kiwi.root.an2linuxclient.preferences.MaxTitleSizePreference;

import static android.content.Context.MODE_PRIVATE;

public class NotificationSettings {

    private boolean prefIncludeTitle;
    private boolean prefForceTitle;
    private boolean prefIncludeMessage;
    private boolean prefIncludeIcon;
    private int prefMaxTitleSize;
    private int prefMaxMessageSize;
    private int prefIconSize;
    private byte notificationFlags;

    private final byte FLAG_INCLUDE_ICON = 4;

    private String packageName;
    private boolean usingCustomSettings;

    NotificationSettings(Context c, String packageName){
        this.packageName = packageName;
        initializeSettings(c);
    }

    private void initializeSettings(Context c){
        SharedPreferences sharedPrefs = c.getSharedPreferences(c.getString(R.string.notification_settings_custom), MODE_PRIVATE);
        usingCustomSettings = sharedPrefs.getBoolean(packageName + "_" + c.getString(R.string.preference_use_custom_settings), false);
        if (!usingCustomSettings) {
            sharedPrefs = c.getSharedPreferences(c.getString(R.string.notification_settings_global), MODE_PRIVATE);
        }
        prefIncludeTitle = sharedPrefs.getBoolean(getCorrectPrefKey(c.getString(R.string.preference_include_notification_title)), true);
        prefIncludeMessage = sharedPrefs.getBoolean(getCorrectPrefKey(c.getString(R.string.preference_include_notification_message)), true);
        prefIncludeIcon = sharedPrefs.getBoolean(getCorrectPrefKey(c.getString(R.string.preference_include_notification_icon)), true);
        if (prefIncludeTitle){
            final byte FLAG_INCLUDE_TITLE = 1;
            notificationFlags |= FLAG_INCLUDE_TITLE;
            prefForceTitle = sharedPrefs.getBoolean(getCorrectPrefKey(c.getString(R.string.preference_force_title)), false);
            prefMaxTitleSize = sharedPrefs.getInt(getCorrectPrefKey(c.getString(R.string.preference_title_max_size)), MaxTitleSizePreference.DEFAULT_VALUE);
        }
        if (prefIncludeMessage){
            final byte FLAG_INCLUDE_MESSAGE = 2;
            notificationFlags |= FLAG_INCLUDE_MESSAGE;
            prefMaxMessageSize = sharedPrefs.getInt(getCorrectPrefKey(c.getString(R.string.preference_message_max_size)), MaxMessageSizePreference.DEFAULT_VALUE);
        }
        if (prefIncludeIcon) {
            notificationFlags |= FLAG_INCLUDE_ICON;
            prefIconSize = sharedPrefs.getInt(getCorrectPrefKey(c.getString(R.string.preference_icon_size)), IconSizePreference.DEFAULT_VALUE);
        }
    }

    public boolean includeTitle(){
        return prefIncludeTitle;
    }

    boolean forceTitle(){
        return prefForceTitle;
    }

    int getTitleMax(){
        return prefMaxTitleSize;
    }

    public boolean includeMessage(){
        return prefIncludeMessage;
    }

    int getMessageMax(){
        return prefMaxMessageSize;
    }

    public boolean includeIcon(){
        return prefIncludeIcon;
    }

    int getIconSize(){
        return prefIconSize;
    }

    public byte getNotificationFlags(){
        return notificationFlags;
    }

    void removeIconFlag(){
        notificationFlags &= ~FLAG_INCLUDE_ICON;
    }

    private String getCorrectPrefKey(String key) {
        return usingCustomSettings ? packageName + "_" + key : key;
    }

}
