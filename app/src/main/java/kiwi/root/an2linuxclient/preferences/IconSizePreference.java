/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.preferences;

import android.content.Context;
import android.util.AttributeSet;

import kiwi.root.an2linuxclient.R;

public class IconSizePreference extends NumberPickerPreference {

    private static final int MIN_VALUE = 20;
    private static final int MAX_VALUE = 100;
    public static final int DEFAULT_VALUE = 64;

    public IconSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs, MIN_VALUE, MAX_VALUE, DEFAULT_VALUE);
    }

    @Override
    String getSummaryString(){
        return getContext().getString(R.string.main_icon_size_summary, mValue);
    }

}
