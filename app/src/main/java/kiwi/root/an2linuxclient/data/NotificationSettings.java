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
import android.preference.PreferenceManager;

import kiwi.root.an2linuxclient.preferences.MaxMessageSizePreference;
import kiwi.root.an2linuxclient.preferences.MaxTitleSizePreference;

public class NotificationSettings {

    private boolean prefIncludeTitle;
    private boolean prefForceTitle;
    private boolean prefIncludeMessage;
    private boolean prefIncludeIcon;
    private int prefMaxTitleSize;
    private int prefMaxMessageSize;
    private byte notificationFlags;

    private final byte FLAG_INCLUDE_ICON = 4;

    NotificationSettings(Context c){
        initializeSettings(c);
    }

    private void initializeSettings(Context c){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefIncludeTitle = sharedPrefs.getBoolean("preference_include_notification_title", true);
        prefIncludeMessage = sharedPrefs.getBoolean("preference_include_notification_message", true);
        prefIncludeIcon = sharedPrefs.getBoolean("preference_include_notification_icon", true);

        if (prefIncludeTitle){
            final byte FLAG_INCLUDE_TITLE = 1;
            notificationFlags |= FLAG_INCLUDE_TITLE;
            prefForceTitle = sharedPrefs.getBoolean("preference_force_title", false);
            prefMaxTitleSize = sharedPrefs.getInt("preference_title_max_size", MaxTitleSizePreference.DEFAULT_VALUE);
        }
        if (prefIncludeMessage){
            final byte FLAG_INCLUDE_MESSAGE = 2;
            notificationFlags |= FLAG_INCLUDE_MESSAGE;
            prefMaxMessageSize = sharedPrefs.getInt("preference_message_max_size", MaxMessageSizePreference.DEFAULT_VALUE);
        }
        if (prefIncludeIcon) {
            notificationFlags |= FLAG_INCLUDE_ICON;
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

    public byte getNotificationFlags(){
        return notificationFlags;
    }

    void removeIconFlag(){
        notificationFlags &= ~FLAG_INCLUDE_ICON;
    }

}
