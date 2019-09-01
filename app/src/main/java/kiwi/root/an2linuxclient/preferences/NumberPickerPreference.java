/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import kiwi.root.an2linuxclient.R;

abstract public class NumberPickerPreference extends DialogPreference {

    int value;
    private int minValue;
    private int maxValue;
    private int defaultValue;

    NumberPickerPreference(Context context, AttributeSet attrs,
                           int minValue, int maxValue, int defaultValue) {
        super(context, attrs);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = defaultValue;
        setDialogLayoutResource(R.layout.numberpicker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
        setDialogTitle(null);
    }

    String getSummaryString(){
        return String.valueOf(value);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        value = getPersistedInt(defaultValue == null ? this.defaultValue : (int) defaultValue);
        persistInt(value);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, defaultValue);
    }

    int getValue() {
        return value;
    }

    int getMinValue() {
        return minValue;
    }

    int getMaxValue() {
        return maxValue;
    }

    void saveValue(int value) {
        this.value = value;
        findPreferenceInHierarchy(getKey()).setSummary(getSummaryString());
        persistInt(value);
    }

}
