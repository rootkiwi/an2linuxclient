/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import kiwi.root.an2linuxclient.R;

public class ConnectionHelper {

    public static byte[] intToByteArray(int value){
        if (value < 0) {
            throw new RuntimeException();
        }
        return new byte[] {
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    public static byte[] readAll(int size, InputStream in) throws IOException {
        if (size <= 0) {
            throw new RuntimeException();
        }
        byte[] buf = new byte[size];
        int bytesRead = 0;
        int tmp;
        while (bytesRead < size){
            tmp = in.read(buf, bytesRead, size-bytesRead);
            if (tmp == -1){
                throw new SocketException();
            } else {
                bytesRead += tmp;
            }
        }
        return buf;
    }

    public static boolean checkIfSsidIsAllowed(String ssidWhitelist, Context c){
        if (ssidWhitelist == null){
            return true;
        }
        WifiManager wifiManager = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String connectedToSsid = info.getSSID();
        for (String ssid : ssidWhitelist.split("(?<!\\\\),")){
            if (connectedToSsid.equals("\"" + ssid.trim().replace("\\,", ",") + "\"")){
                return true;
            }
        }
        return false;
    }

    private static boolean checkIfValidIpOrHostname(String ipOrHostname) {
        // source: http://stackoverflow.com/a/3824105
        String validHostnameRegex = "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$";
        // source: http://stackoverflow.com/a/106223
        String validIPv4Regex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
        // source: http://stackoverflow.com/a/17871737
        String validIPv6Regex = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";
        return ipOrHostname.matches(validHostnameRegex) && ipOrHostname.length() <= 255
                || ipOrHostname.matches(validIPv4Regex)
                || ipOrHostname.matches(validIPv6Regex);
    }

    public static boolean checkIfValidAddressAndPortInput(EditText ipOrHostnameEditText,
                                                          EditText portNumberEditText,
                                                          Context c){
        String ipOrHostname = ipOrHostnameEditText.getText().toString().trim();
        if (ConnectionHelper.checkIfValidIpOrHostname(ipOrHostname)){
            try {
                int portNumber = Integer.parseInt(portNumberEditText.getText().toString());
                if (portNumber < 0 || portNumber > 65535) {
                    sendToast(R.string.port_range, c);
                    return false;
                }
            } catch (NumberFormatException e) {
                sendToast(R.string.invalid_port, c);
                return false;
            }
        } else {
            sendToast(R.string.invalid_address, c);
            return false;
        }
        return true;
    }

    private static void sendToast(int msg, Context c){
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

}
