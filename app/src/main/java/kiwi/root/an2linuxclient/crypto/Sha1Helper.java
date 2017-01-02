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

public class Sha1Helper {

    public static byte[] sha1(byte[] bytesToHash){
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(bytesToHash);
            return sha1.digest();
        } catch (Exception e){
            Log.e("Sha1Helper", "sha1");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

    public static byte[] sha1(byte[] bytesToHash, byte[] moreBytesToHash){
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(bytesToHash);
            sha1.update(moreBytesToHash);
            return sha1.digest();
        } catch (Exception e){
            Log.e("Sha1Helper", "sha1");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

    public static String getTwoLineHexString(byte[] sha1Hash){
        Formatter formatter = new Formatter();
        for (int i = 0; i < sha1Hash.length; i++){
            formatter.format("%02X", sha1Hash[i]);
            if (i < sha1Hash.length / 2 - 1){
                formatter.format(" ");
            } else if (i == sha1Hash.length / 2 - 1){
                formatter.format("\n");
            } else if (i < sha1Hash.length - 1){
                formatter.format(" ");
            }
        }
        return formatter.toString();
    }

    public static String getTwoLineHexString(String hexString){
        Formatter formatter = new Formatter();
        for (int i = 0; i < hexString.length(); i+=2){
            formatter.format(hexString.substring(i, i+2).toUpperCase());
            if (i == hexString.length() / 2 - 2){
                formatter.format("\n");
            } else if (i != hexString.length() - 2){
                formatter.format(" ");
            }
        }
        return formatter.toString();
    }

}
