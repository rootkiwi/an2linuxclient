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

import org.spongycastle.asn1.x500.X500NameBuilder;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import kiwi.root.an2linuxclient.utils.ConnectionHelper;

import static android.content.Context.MODE_PRIVATE;

public class TlsHelper {

    public static final String[] TLS_VERSIONS = new String[]{"TLSv1.2"};
    // TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 -> API_20+
    // TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA -> API_11+
    public static final String[] TLS_CIPHERS = new String[]{"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA"};

    static void initialiseCertificate(SharedPreferences deviceKeyPref, KeyPair keyPair){

        X509Certificate certificate;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date notBefore = calendar.getTime();
        calendar.add(Calendar.YEAR, 10);
        Date notAfter = calendar.getTime();

        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, "an2linuxclient");
        nameBuilder.addRDN(BCStyle.SERIALNUMBER, new BigInteger(128, new Random()).toString(16));

        X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                nameBuilder.build(),
                BigInteger.ONE,
                notBefore, notAfter,
                nameBuilder.build(),
                keyPair.getPublic()
        );

        try {
            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider("BC").build(keyPair.getPrivate());
            certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateBuilder.build(contentSigner));

            SharedPreferences.Editor edit = deviceKeyPref.edit();
            edit.putString("certificate", Base64.encodeToString(certificate.getEncoded(), Base64.NO_WRAP));
            edit.putBoolean("currently_generating", false);
            edit.apply();

            Log.d("TlsHelper", "Generated new certificate successfully");
        } catch (Exception e){
            Log.e("TlsHelper", "initialiseCertificate");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return;
        }

    }

    public static X509Certificate getCertificate(Context c){
        SharedPreferences deviceKeyPref = c.getSharedPreferences("device_key_and_cert", MODE_PRIVATE);
        try {
            byte[] certificateBytes = Base64.decode(deviceKeyPref.getString("certificate", ""), Base64.DEFAULT);
            X509CertificateHolder certificateHolder = new X509CertificateHolder(certificateBytes);
            return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);
        } catch (Exception e) {
            Log.e("TlsHelper", "getCertificate");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

    public static byte[] getCertificateBytes(Context c){
        SharedPreferences deviceKeyPref = c.getSharedPreferences("device_key_and_cert", MODE_PRIVATE);
        return Base64.decode(deviceKeyPref.getString("certificate", ""), Base64.DEFAULT);
    }

    public static byte[] certificateToBytes(Certificate certificate){
        try {
            return certificate.getEncoded();
        } catch (CertificateEncodingException e){
            Log.e("TlsHelper", "certificateToBytes");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

    public static SSLContext getPairingTlsContext(Context c){
        try {
            SSLContext tlsContext = SSLContext.getInstance(TLS_VERSIONS[0]);

            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};

            tlsContext.init(null, trustAllCerts, null);
            return tlsContext;
        } catch (Exception e){
            Log.e("TlsHelper", "getPairingTlsContext");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

    public static SSLContext getNotificationTlsContext(Context c, Certificate serverCert){
        try {
            SSLContext tlsContext = SSLContext.getInstance(TLS_VERSIONS[0]);

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setKeyEntry("key", RsaHelper.getPrivateKey(c), "".toCharArray(),
                    new Certificate[]{TlsHelper.getCertificate(c)});
            keyStore.setCertificateEntry("serverCert", serverCert);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            tlsContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), null);
            return tlsContext;
        } catch (Exception e){
            Log.e("TlsHelper", "getNotificationTlsContext");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

    public static SSLEngineResult.HandshakeStatus doHandshake(SSLEngine tlsEngine,
                                                              ByteBuffer netDataBuf,
                                                              OutputStream out,
                                                              InputStream in){
        try {
            ByteBuffer empty = ByteBuffer.allocate(0);
            // ClientHello -> netDataBuf
            tlsEngine.wrap(empty, netDataBuf);
            netDataBuf.flip();
            byte[] clientHello = new byte[netDataBuf.limit()];
            netDataBuf.get(clientHello);
            out.write(ConnectionHelper.intToByteArray(clientHello.length));
            out.write(clientHello);

            // netDataBuf <- ServerHello..ServerHelloDone
            int serverHelloSize = ByteBuffer.wrap(ConnectionHelper.readAll(4, in)).getInt();
            byte[] serverHello = ConnectionHelper.readAll(serverHelloSize, in);
            netDataBuf.clear();
            netDataBuf.put(serverHello);
            netDataBuf.flip();
            tlsEngine.unwrap(netDataBuf, empty);

            // [client]Certificate*..ClientKeyExchange..Finished -> netDataBuf
            netDataBuf.clear();
            tlsEngine.wrap(empty, netDataBuf);
            netDataBuf.flip();
            byte[] clientKeyExchange = new byte[netDataBuf.limit()];
            netDataBuf.get(clientKeyExchange);
            out.write(ConnectionHelper.intToByteArray(clientKeyExchange.length));
            out.write(clientKeyExchange);

            // netDataBuf <- ChangeCipherSpec..Finished
            int serverChangeCipherSpecSize = ByteBuffer.wrap(ConnectionHelper.readAll(4, in)).getInt();
            byte[] serverChangeCipherSpec = ConnectionHelper.readAll(serverChangeCipherSpecSize, in);
            netDataBuf.clear();
            netDataBuf.put(serverChangeCipherSpec);
            netDataBuf.flip();
            return tlsEngine.unwrap(netDataBuf, empty).getHandshakeStatus();
        } catch (IOException e){
            return null;
        }
    }

    public static byte[] tlsEncrypt(SSLEngine tlsEngine,
                                    ByteBuffer appDataBuf,
                                    ByteBuffer netDataBuf,
                                    byte[] appData){
        try {
            appDataBuf.clear();
            netDataBuf.clear();
            appDataBuf.put(appData);
            appDataBuf.flip();
            tlsEngine.wrap(appDataBuf, netDataBuf);
            netDataBuf.flip();
            byte[] netData = new byte[netDataBuf.limit()];
            netDataBuf.get(netData);
            return netData;
        } catch (SSLException e){
            Log.e("TlsHelper", "tlsEncrypt");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

    public static byte[] tlsDecrypt(SSLEngine tlsEngine,
                                    ByteBuffer appDataBuf,
                                    ByteBuffer netDataBuf,
                                    byte[] netData){
        try {
            appDataBuf.clear();
            netDataBuf.clear();
            netDataBuf.put(netData);
            netDataBuf.flip();
            tlsEngine.unwrap(netDataBuf, appDataBuf);
            appDataBuf.flip();
            byte[] appData = new byte[appDataBuf.limit()];
            appDataBuf.get(appData);
            return appData;
        } catch (SSLException e){
            Log.e("TlsHelper", "tlsDecrypt");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

}
