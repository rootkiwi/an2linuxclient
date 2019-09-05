package kiwi.root.an2linuxclient.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.data.CustomSettingsAppData;
import kiwi.root.an2linuxclient.interfaces.OnItemClickListener;

public class CustomNotificationSettingsAdapter extends RecyclerView.Adapter<CustomNotificationSettingsAdapter.AppDataViewHolder> {

    private List<CustomSettingsAppData> appDataList;

    class AppDataViewHolder extends RecyclerView.ViewHolder {
        private ImageView appIcon;
        private TextView appLabelTextView;
        private TextView usingCustomTextView;
        private String usingCustomText;

        private AppDataViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.appIcon);
            appLabelTextView = itemView.findViewById(R.id.appLabelTextView);
            usingCustomTextView = itemView.findViewById(R.id.usingCustomTextView);
            usingCustomText = itemView.getContext().getString(R.string.notif_custom_using_custom_settings);
        }

        private void bind(final CustomSettingsAppData appData, final OnItemClickListener onItemClickListener) {
            appIcon.setImageDrawable(appData.appIcon);
            appLabelTextView.setText(appData.appName);
            if (appData.isUsingCustomSettings) {
                usingCustomTextView.setText(usingCustomText);
            } else {
                usingCustomTextView.setText("");
            }

            itemView.setOnClickListener(view -> onItemClickListener.onItemClick(appData));
        }
    }

    private final OnItemClickListener onItemClickListener;

    public CustomNotificationSettingsAdapter(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public AppDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_notification_settings_recyclerview_item, parent, false);
        return new AppDataViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppDataViewHolder holder, int position) {
        holder.bind(appDataList.get(position), onItemClickListener);
    }

    @Override
    public int getItemCount() {
        if (appDataList == null) {
            return 0;
        }

        return appDataList.size();
    }

    public void setAppDataList(List<CustomSettingsAppData> appDataList) {
        this.appDataList = appDataList;
        notifyDataSetChanged();
    }

}
