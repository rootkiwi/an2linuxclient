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
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.views.BluetoothDialogNew;
import kiwi.root.an2linuxclient.views.BluetoothPairedListDialog;


public class BluetoothPairedDevicesAdapter extends ArrayAdapter<BluetoothDevice> {
    private BluetoothPairedListDialog dialog;

    public BluetoothPairedDevicesAdapter(Context context, ArrayList<BluetoothDevice> pairedBluetoothList, BluetoothPairedListDialog dialog) {
        super(context, 0, pairedBluetoothList);
        this.dialog = dialog;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final BluetoothDevice device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_bluetooth_item, parent, false);
        }

        Button bluetoothEditBtn = (Button) convertView.findViewById(R.id.bluetoothEditBtn);
        TextView btDeviceNameText = (TextView) convertView.findViewById(R.id.btDeviceNameText);
        TextView btMacText = (TextView) convertView.findViewById(R.id.btMacText);

        btDeviceNameText.setText(device.getName());
        btMacText.setText(device.getAddress());

        bluetoothEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = ((Activity) getContext()).getFragmentManager();
                BluetoothDialogNew myDialog = BluetoothDialogNew.newInstance(device.getName(), device.getAddress());
                myDialog.setCancelable(false);
                myDialog.show(manager, "bluetooth");
                dialog.dismiss();
            }
        });

        return convertView;
    }

}
