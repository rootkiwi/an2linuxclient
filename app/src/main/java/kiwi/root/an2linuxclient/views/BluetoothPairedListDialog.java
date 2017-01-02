/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.views;

import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import kiwi.root.an2linuxclient.R;
import kiwi.root.an2linuxclient.adapters.BluetoothPairedDevicesAdapter;

public class BluetoothPairedListDialog extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.view_add_bluetooth_server, container);

        ListView listViewBtPairedPCs = (ListView) view.findViewById(R.id.listViewBtPairedPCs);

        ArrayList<BluetoothDevice> pairedBluetoothList = new ArrayList<>();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if(device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER) {
                    pairedBluetoothList.add(device);
                }
            }
            if (pairedBluetoothList.size() == 0) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.bluetooth_no_paired_found, Toast.LENGTH_LONG).show();
                return null;
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), R.string.bluetooth_no_paired_found, Toast.LENGTH_LONG).show();
            return null;
        }
        BluetoothPairedDevicesAdapter adapter = new BluetoothPairedDevicesAdapter(getActivity(), pairedBluetoothList, this);
        listViewBtPairedPCs.setAdapter(adapter);
        return view;
    }

}
