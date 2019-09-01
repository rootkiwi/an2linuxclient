/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.preferences;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import androidx.preference.Preference;

public class MaxTitleSizePreference extends NumberPickerPreference {

    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 999;
    public static final int DEFAULT_VALUE = 20;

    public MaxTitleSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs, MIN_VALUE, MAX_VALUE, DEFAULT_VALUE);
    }

}
