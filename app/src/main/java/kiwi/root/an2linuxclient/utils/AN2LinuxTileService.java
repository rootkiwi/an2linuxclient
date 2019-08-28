/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.annotation.RequiresApi;

import kiwi.root.an2linuxclient.R;

@RequiresApi(Build.VERSION_CODES.N)
public class AN2LinuxTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();

        Context c = getApplicationContext();
        boolean an2linuxEnabled = PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean(c.getString(R.string.preference_enable_an2linux), false);

        Tile tile = getQsTile();

        if (an2linuxEnabled) {
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();

        Context c = getApplicationContext();

        SharedPreferences sharedPrefsDefault = PreferenceManager.getDefaultSharedPreferences(c);
        boolean an2linuxEnabled = sharedPrefsDefault.getBoolean(c.getString(R.string.preference_enable_an2linux), false);

        Tile tile = getQsTile();
        SharedPreferences.Editor edit = sharedPrefsDefault.edit();

        if (an2linuxEnabled) {
            tile.setState(Tile.STATE_INACTIVE);
            edit.putBoolean(c.getString(R.string.preference_enable_an2linux), false);
        } else {
            tile.setState(Tile.STATE_ACTIVE);
            edit.putBoolean(c.getString(R.string.preference_enable_an2linux), true);
        }
        tile.updateTile();
        edit.apply();
    }

}
