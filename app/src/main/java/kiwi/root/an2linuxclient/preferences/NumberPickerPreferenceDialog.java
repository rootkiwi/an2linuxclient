package kiwi.root.an2linuxclient.preferences;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.preference.PreferenceDialogFragmentCompat;

import kiwi.root.an2linuxclient.R;

public class NumberPickerPreferenceDialog extends PreferenceDialogFragmentCompat {

    private NumberPicker numberPicker;

    public static NumberPickerPreferenceDialog newInstance(String prefKey) {
        NumberPickerPreferenceDialog fragment = new NumberPickerPreferenceDialog();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", prefKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Need to do this here, since getDialog() returns null in for example onBindDialogView()
        final Resources res = getContext().getResources();
        final Window window = getDialog().getWindow();
        Button button1 = window.findViewById(android.R.id.button1);
        Button button2 = window.findViewById(android.R.id.button2);
        button1.setTextColor(res.getColor(R.color.black));
        button2.setTextColor(res.getColor(R.color.black));
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        NumberPickerPreference npp = getNumberPickerPreference();
        numberPicker = view.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(npp.getMinValue());
        numberPicker.setMaxValue(npp.getMaxValue());
        numberPicker.setValue(npp.getValue());
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            getNumberPickerPreference().saveValue(numberPicker.getValue());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    private NumberPickerPreference getNumberPickerPreference() {
        return (NumberPickerPreference) getPreference();
    }

}
