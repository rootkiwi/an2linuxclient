/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.adapters;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.crypto.Sha1Helper;
import kiwi.root.an2linuxclient.data.BluetoothServer;
import kiwi.root.an2linuxclient.data.Server;
import kiwi.root.an2linuxclient.data.ServerDatabaseHandler;
import kiwi.root.an2linuxclient.data.MobileServer;
import kiwi.root.an2linuxclient.data.TcpServer;
import kiwi.root.an2linuxclient.data.WifiServer;
import kiwi.root.an2linuxclient.views.BluetoothDialogEdit;
import kiwi.root.an2linuxclient.views.MobileDialogEdit;
import kiwi.root.an2linuxclient.views.WifiDialogEdit;

public class ServerConfigurationAdapter extends ArrayAdapter<Server> {
    private List<Server> serverList = new ArrayList<>();
    private Context context;

    public ServerConfigurationAdapter(Context context, List<Server> serverList) {
        super(context, 0, serverList);
        this.context = context;
        this.serverList = serverList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Server server = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_server_item, parent, false);
        }
        TextView listItemServerName = (TextView) convertView.findViewById(R.id.listItemServerName);
        TextView listItemServerInfo1 = (TextView) convertView.findViewById(R.id.listItemServerInfo1);
        TextView listItemServerInfo2 = (TextView) convertView.findViewById(R.id.listItemServerInfo2);
        TextView listItemServerInfo3 = (TextView) convertView.findViewById(R.id.listItemServerInfo3);

        ImageView iconConnectionType = (ImageView) convertView.findViewById(R.id.iconConnectionType);

        if (server instanceof WifiServer){
            iconConnectionType.setImageResource(R.drawable.ic_signal_wifi_4_bar_white_24dp);
        } else if (server instanceof MobileServer) {
            iconConnectionType.setImageResource(R.drawable.ic_signal_cellular_4_bar_white_24dp);
        } else {
            iconConnectionType.setImageResource(R.drawable.ic_bluetooth_white_24dp);
        }

        ServerDatabaseHandler dbHandler = ServerDatabaseHandler.getInstance(context);
        if (server instanceof TcpServer){
            listItemServerInfo1.setText(String.format("%s %s",
                    context.getString(R.string.server_list_port_colon),
                    Integer.toString(((TcpServer)server).getPortNumber())));

            listItemServerInfo3.setText(String.format("%s\n%s",
                    context.getString(R.string.server_list_certificate_fingerprint),
                    Sha1Helper.getTwoLineHexString(
                            dbHandler.getCertificateFingerprint(
                                    server.getCertificateId()))));

            if (server instanceof WifiServer) {
                WifiServer wifiServer = (WifiServer) server;
                listItemServerName.setText(wifiServer.getIpOrHostname());
                if(wifiServer.getSsidWhitelist() == null){
                    listItemServerInfo2.setText(String.format("%s %s",
                            context.getString(R.string.server_list_ssid_whitelist_colon),
                            context.getString(R.string.server_list_any_ssid)));
                } else {
                    String[] allowedSSIDs = wifiServer.getSsidWhitelist().split(",");
                    String allowedSSIDsString = "";

                    for (int i = 0; i < allowedSSIDs.length; i++){
                        allowedSSIDsString += String.format("'%s'", allowedSSIDs[i].trim());
                        if (i < allowedSSIDs.length - 1){
                            allowedSSIDsString += ", ";
                        }
                    }

                    listItemServerInfo2.setText(String.format("%s %s",
                            context.getString(R.string.server_list_ssid_whitelist_colon), allowedSSIDsString));
                }
            } else {
                MobileServer mobileServer = (MobileServer) server;
                listItemServerName.setText(mobileServer.getIpOrHostname());
                listItemServerInfo2.setText(String.format("%s %s",
                        context.getString(R.string.server_list_roaming_allowed),
                        mobileServer.isRoamingAllowed() ? context.getString(R.string.server_list_roaming_allowed_yes) : context.getString(R.string.server_list_roaming_allowed_no)));
            }
        } else {
            BluetoothServer bluetoothServer = (BluetoothServer) server;
            listItemServerName.setText(bluetoothServer.getBluetoothName());
            listItemServerInfo1.setText(bluetoothServer.getBluetoothMacAddress());
            listItemServerInfo2.setText(String.format("%s\n%s",
                    context.getString(R.string.server_list_certificate_fingerprint),
                    Sha1Helper.getTwoLineHexString(
                            dbHandler.getCertificateFingerprint(
                                    server.getCertificateId()))));
            listItemServerInfo3.setText("");
        }

        final SwitchCompat enabledSwitch = (SwitchCompat) convertView.findViewById(R.id.prefServerEnabled);

        enabledSwitch.setChecked(server.isEnabled());

        enabledSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerDatabaseHandler dbHandler = ServerDatabaseHandler.getInstance(getContext());
                if (enabledSwitch.isChecked()) {
                    dbHandler.updateIsEnabled(server.getId(), true);
                    updateServerInListIsEnabled(position, true);
                } else {
                    dbHandler.updateIsEnabled(server.getId(), false);
                    updateServerInListIsEnabled(position, false);
                }
            }
        });

        ((ListView) parent).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Server server = serverList.get(position);
                long id = serverList.get(position).getId();
                FragmentManager manager = ((Activity) context).getFragmentManager();

                if (server instanceof WifiServer) {
                    WifiDialogEdit myDialog = WifiDialogEdit.newInstance(id, position);
                    myDialog.setCancelable(false);
                    myDialog.show(manager, "wifi");
                } else if (server instanceof MobileServer) {
                    MobileDialogEdit myDialog = MobileDialogEdit.newInstance(id, position);
                    myDialog.setCancelable(false);
                    myDialog.show(manager, "mobile");
                } else {
                    BluetoothDialogEdit myDialog = BluetoothDialogEdit.newInstance(id, position);
                    myDialog.setCancelable(false);
                    myDialog.show(manager, "bluetooth");
                }

            }
        });

        return convertView;
    }

    public void addServerToList(Server server){
        serverList.add(server);
        notifyDataSetChanged();
    }

    public void deleteServerFromList(int serverListPosition){
        serverList.remove(serverListPosition);
        notifyDataSetChanged();
    }

    public void updateServerInList(Server server, int serverListPosition){
        serverList.remove(serverListPosition);
        serverList.add(serverListPosition, server);
        notifyDataSetChanged();
    }

    private void updateServerInListIsEnabled(int serverListPosition, boolean isEnabled){
        serverList.get(serverListPosition).setIsEnabled(isEnabled);
        notifyDataSetChanged();
    }

}

