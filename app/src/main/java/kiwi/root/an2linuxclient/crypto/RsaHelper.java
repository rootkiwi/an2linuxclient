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

import kiwi.root.an2linuxclient.R;

import static android.content.Context.MODE_PRIVATE;

class RsaHelper {

    static void initialiseRsaKeyAndCert(Context c){
        try {
            SharedPreferences deviceKeyPref = c.getSharedPreferences(
                    c.getString(R.string.device_key_and_cert), MODE_PRIVATE);
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(4096);
            KeyPair keyPair = kpg.generateKeyPair();
            deviceKeyPref.edit().putString(c.getString(R.string.privatekey),
                    Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.NO_WRAP)).apply();
            Log.d("RsaHelper", "Generated new keypair successfully");
            TlsHelper.initialiseCertificate(c, keyPair);
        } catch (Exception e){
            Log.e("RsaHelper", "initialiseRsaKeyAndCert");
            Log.e("StackTrace", Log.getStackTraceString(e));
        }
    }

    static PrivateKey getPrivateKey(Context c){
        try {
            SharedPreferences deviceKeyPref = c.getSharedPreferences(c.getString(R.string.device_key_and_cert), MODE_PRIVATE);
            byte[] privateKeyBytes = Base64.decode(deviceKeyPref.getString(c.getString(R.string.privatekey), ""), Base64.DEFAULT);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (Exception e) {
            Log.e("RsaHelper", "getPrivateKey");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

}
