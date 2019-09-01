package kiwi.root.an2linuxclient.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.adapters.CustomNotificationSettingsAdapter;
import kiwi.root.an2linuxclient.data.CustomSettingsAppData;
import kiwi.root.an2linuxclient.interfaces.OnItemClickListener;
import kiwi.root.an2linuxclient.viewmodels.CustomNotificationSettingsViewModel;
import kiwi.root.an2linuxclient.views.CustomProgressDialog;

public class CustomNotificationSettingsActivity extends AppCompatActivity implements OnItemClickListener {

    private CustomNotificationSettingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_notification_settings);

        final CustomProgressDialog progressDialog = new CustomProgressDialog();
        progressDialog.setCancelable(false);
        progressDialog.show(getSupportFragmentManager(), "progressDialog");

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final ConstraintLayout emptyView = findViewById(R.id.emptyView);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomNotificationSettingsActivity.this, EnabledApplicationsActivity.class);
                startActivityForResult(intent, RETURNED_FROM_ENABLED_APPS_SETTINGS_REQUEST);
            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final CustomNotificationSettingsAdapter adapter = new CustomNotificationSettingsAdapter(this);
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        viewModel = ViewModelProviders.of(this).get(CustomNotificationSettingsViewModel.class);
        viewModel.getAppsDataList().observe(this, new Observer<List<CustomSettingsAppData>>() {
            @Override
            public void onChanged(List<CustomSettingsAppData> customSettingsAppData) {
                adapter.setAppDataList(customSettingsAppData);
                progressDialog.dismiss();
                if (customSettingsAppData.size() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private static final int RETURNED_FROM_APP_SETTINGS_REQUEST = 1;
    private static final int RETURNED_FROM_ENABLED_APPS_SETTINGS_REQUEST = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RETURNED_FROM_APP_SETTINGS_REQUEST:
                viewModel.maybeUpdateUsingCustom();
                break;
            case RETURNED_FROM_ENABLED_APPS_SETTINGS_REQUEST:
                viewModel.updateAfterEnabledApplicationSettings();
                break;
        }
    }

    @Override
    public void onItemClick(CustomSettingsAppData appData) {
        Intent intent = new Intent(this, AppNotificationSettingsActivity.class);
        intent.putExtra("appName", appData.appName);
        intent.putExtra("packageName", appData.packageName);
        startActivityForResult(intent, RETURNED_FROM_APP_SETTINGS_REQUEST);
    }

}
