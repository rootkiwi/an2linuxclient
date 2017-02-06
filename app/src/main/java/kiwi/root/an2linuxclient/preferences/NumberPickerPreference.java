/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import kiwi.root.an2linuxclient.R;

abstract class NumberPickerPreference extends DialogPreference {

    private NumberPicker mNumberPicker;
    int mValue;
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

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final Resources res = getContext().getResources();
        final Window window = getDialog().getWindow();
        Button button1 = (Button) window.findViewById(res.getIdentifier("button1", "id", "android"));
        Button button2 = (Button) window.findViewById(res.getIdentifier("button2", "id", "android"));
        button1.setTextColor(res.getColor(R.color.black));
        button2.setTextColor(res.getColor(R.color.black));
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        TextView dialogMessageText = (TextView) view.findViewById(R.id.text_dialog_message);
        dialogMessageText.setText(getDialogMessage());
        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        mNumberPicker.setMinValue(minValue);
        mNumberPicker.setMaxValue(maxValue);
        mNumberPicker.setValue(mValue);
        mNumberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            mValue = mNumberPicker.getValue();
            findPreferenceInHierarchy(getKey()).setSummary(getSummaryString());
            persistInt(mValue);
        }
    }

    String getSummaryString(){
        return String.valueOf(mValue);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mValue = getPersistedInt(this.defaultValue);
        } else {
            // Set default state from the XML attribute
            mValue = (Integer) defaultValue;
            persistInt(mValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, defaultValue);
    }

}
