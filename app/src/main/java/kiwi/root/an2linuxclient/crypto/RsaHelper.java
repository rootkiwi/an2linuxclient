/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import static android.content.Context.MODE_PRIVATE;

public class RsaHelper {

    public static void initialiseRsaKeyAndCert(SharedPreferences deviceKeyPref){
        deviceKeyPref.edit().putBoolean("currently_generating", true).apply();
        KeyPair keyPair;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(4096);
            keyPair = kpg.generateKeyPair();
            Log.d("RsaHelper", "Generated new keypair successfully");
        } catch (Exception e){
            Log.e("RsaHelper", "initialiseRsaKeyAndCert");
            Log.e("StackTrace", Log.getStackTraceString(e));
            deviceKeyPref.edit().putBoolean("currently_generating", false).apply();
            return;
        }
        deviceKeyPref.edit().putString("privatekey",
                Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.NO_WRAP)).apply();

        TlsHelper.initialiseCertificate(deviceKeyPref, keyPair);
    }

    static PrivateKey getPrivateKey(Context c){
        try {
            SharedPreferences deviceKeyPref = c.getSharedPreferences("device_key_and_cert", MODE_PRIVATE);
            byte[] privateKeyBytes = Base64.decode(deviceKeyPref.getString("privatekey", ""), Base64.DEFAULT);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (Exception e) {
            Log.e("RsaHelper", "getPrivateKey");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

}
