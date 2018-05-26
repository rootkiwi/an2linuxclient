package kiwi.root.an2linuxclient.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import kiwi.root.an2linuxclient.R;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean an2linuxEnabled = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.preference_enable_an2linux), false);

        if (an2linuxEnabled){
            boolean useForegroundService = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.preference_enable_service), false);

            if (useForegroundService){
                context.startService(new Intent(context, AN2LinuxService.class));
            }
        }
    }
}
