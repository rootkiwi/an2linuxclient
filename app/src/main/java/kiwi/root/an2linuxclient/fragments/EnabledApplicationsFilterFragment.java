package kiwi.root.an2linuxclient.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.viewmodels.EnabledApplicationsViewModel;

public class EnabledApplicationsFilterFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.enabled_applications_filter_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final EnabledApplicationsViewModel viewModel = ViewModelProviders.of(getActivity()).get(EnabledApplicationsViewModel.class);

        EditText filterInputEditText = getView().findViewById(R.id.filterInputEditText);
        filterInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String filter = editable.toString().trim().toLowerCase();
                viewModel.filterTextChanged(filter);
            }
        });
    }

}
