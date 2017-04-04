/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.crypto;

import android.util.Log;

import java.security.MessageDigest;
import java.util.Formatter;

public class Sha256Helper {

    public static byte[] sha256(byte[] bytesToHash){
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(bytesToHash);
            return sha256.digest();
        } catch (Exception e){
            Log.e("Sha256Helper", "sha256");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

    public static byte[] sha256(byte[] bytesToHash, byte[] moreBytesToHash){
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(bytesToHash);
            sha256.update(moreBytesToHash);
            return sha256.digest();
        } catch (Exception e){
            Log.e("Sha256Helper", "sha256");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

    public static String getFourLineHexString(byte[] sha256Hash){
        Formatter formatter = new Formatter();
        for (int i = 0; i < 32; i++) {
            formatter.format("%02X", sha256Hash[i]);
            if (i == 7 || i == 15 || i == 23) {
                formatter.format("\n");
            } else if (i != 31) {
                formatter.format(" ");
            }
        }
        return formatter.toString();
    }

    public static String getFourLineHexString(String hexString){
        Formatter formatter = new Formatter();
        for (int i = 0; i < 64; i+=2) {
            formatter.format(hexString.substring(i, i+2).toUpperCase());
            if (i == 14 || i == 30 || i == 46) {
                formatter.format("\n");
            } else if (i != 62) {
                formatter.format(" ");
            }
        }
        return formatter.toString();
    }

}
