/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.adapters.ServerConfigurationAdapter;
import kiwi.root.an2linuxclient.data.Server;
import kiwi.root.an2linuxclient.data.ServerDatabaseHandler;
import kiwi.root.an2linuxclient.interfaces.ServerAdapterListCallbacks;
import kiwi.root.an2linuxclient.views.BluetoothPairedListDialog;
import kiwi.root.an2linuxclient.views.MobileDialogNew;
import kiwi.root.an2linuxclient.views.WifiDialogNew;

public class ServerConfigurationActivity extends AppCompatActivity implements ServerAdapterListCallbacks {

    ServerConfigurationAdapter adapter;
    FragmentManager manager;
    private final static int REQUEST_ENABLE_BT = 1;

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK) {
                BluetoothPairedListDialog myDialog = new BluetoothPairedListDialog();
                myDialog.show(manager, "bluetooth");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_configuration);

        manager = getFragmentManager();

        ServerDatabaseHandler dbHandler = ServerDatabaseHandler.getInstance(this);
        List<Server> serverList = dbHandler.getAllServers();

        adapter = new ServerConfigurationAdapter(this, serverList);

        ListView lView = (ListView)findViewById(R.id.listViewAllServers);
        lView.setEmptyView(findViewById(R.id.listViewEmpty));
        lView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setRippleColor(getResources().getColor(R.color.white));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence type[] = new CharSequence[] {getString(R.string.connection_type_wifi), getString(R.string.connection_type_mobile), getString(R.string.connection_type_bluetooth)};

                AlertDialog.Builder builder = new AlertDialog.Builder(ServerConfigurationActivity.this);
                builder.setItems(type, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            WifiDialogNew myDialog = new WifiDialogNew();
                            myDialog.setCancelable(false);
                            myDialog.show(manager, "wifi");
                        } else if (which == 1) {
                            MobileDialogNew myDialog = new MobileDialogNew();
                            myDialog.setCancelable(false);
                            myDialog.show(manager, "mobile");
                        } else {
                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (bluetoothAdapter == null) {
                                Toast.makeText(getApplicationContext(), R.string.bluetooth_not_supported, Toast.LENGTH_LONG).show();
                            } else {
                                if (!bluetoothAdapter.isEnabled()) {
                                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                } else {
                                    BluetoothPairedListDialog myDialog = new BluetoothPairedListDialog();
                                    myDialog.show(manager, "bluetooth");
                                }
                            }
                        }
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    public void addServer(Server server){
        adapter.addServerToList(server);
    }

    @Override
    public void deleteServer(int serverListPosition){
        adapter.deleteServerFromList(serverListPosition);
    }

    @Override
    public void updateServer(Server server, int serverListPosition){
        adapter.updateServerInList(server, serverListPosition);
    }

}
