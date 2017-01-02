/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.data;

import java.security.cert.Certificate;

public class BluetoothServer extends Server {

    private String bluetoothMacAddress;
    private String bluetoothName;

    BluetoothServer(){
    }

    /**Add server to database existing certificate*/
    public BluetoothServer(String bluetoothMacAddress,
                           String bluetoothName){
        this.bluetoothMacAddress = bluetoothMacAddress;
        this.bluetoothName = bluetoothName;
    }

    /**Add server to database new certificate*/
    public BluetoothServer(Certificate certificate,
                           String bluetoothMacAddress,
                           String bluetoothName){
        this.certificate = certificate;
        this.bluetoothMacAddress = bluetoothMacAddress;
        this.bluetoothName = bluetoothName;
    }

    /**Update server in database existing certificate*/
    public BluetoothServer(long id,
                           String bluetoothName){
        this.id = id;
        this.bluetoothName = bluetoothName;
    }

    /**Update server in database new certificate*/
    public BluetoothServer(long id,
                           Certificate certificate,
                           String bluetoothName){
        this.id = id;
        this.certificate = certificate;
        this.bluetoothName = bluetoothName;
    }

    void setBluetoothMacAddress(String bluetoothMacAddress) {
        this.bluetoothMacAddress = bluetoothMacAddress;
    }

    void setBluetoothName(String bluetoothName) {
        this.bluetoothName = bluetoothName;
    }

    public String getBluetoothMacAddress() {
        return this.bluetoothMacAddress;
    }

    public String getBluetoothName() {
        return this.bluetoothName;
    }

}
