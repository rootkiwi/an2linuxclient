/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.util.UUID;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;

import kiwi.root.an2linuxclient.crypto.TlsHelper;
import kiwi.root.an2linuxclient.data.Notification;
import kiwi.root.an2linuxclient.data.NotificationSettings;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

class BluetoothNotificationConnection extends NotificationConnection implements Runnable {

    private String serverMacAddress;

    private SSLEngine tlsEngine;
    private ByteBuffer appDataBuf;
    private ByteBuffer netDataBuf;

    BluetoothNotificationConnection(Context c, Notification n, Certificate serverCert, String serverMacAddress){
        super(c, n, serverCert);
        this.serverMacAddress = serverMacAddress;
    }

    private void createTlsEngine(){
        tlsEngine = TlsHelper.getNotificationTlsContext(c, serverCert).createSSLEngine();
        tlsEngine.setUseClientMode(true);
        tlsEngine.setEnabledProtocols(TlsHelper.TLS_VERSIONS);
        tlsEngine.setEnabledCipherSuites(TlsHelper.TLS_CIPHERS);
    }

    private void createBuffers(){
        appDataBuf = ByteBuffer.allocate(tlsEngine.getSession().getApplicationBufferSize());
        netDataBuf = ByteBuffer.allocate(tlsEngine.getSession().getPacketBufferSize());
    }

    @Override
    public void run(){
        try {
            // hardcoded uuid generated from https://www.uuidgenerator.net/
            BluetoothSocket bs = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(serverMacAddress)
                    .createRfcommSocketToServiceRecord(UUID.fromString("a97fbf21-2ef3-4daf-adfb-2a53ffa87b8e"));

            try {
                bs.connect();
            } catch (IOException connectException) {
                try {
                    bs.close();
                } catch (IOException closeException) {}
                /*I dont know why but I need sleep() so there are no more new connections
                than every about 0.5 sec, apparently with too many sockets in short time
                android throws some connection error when using bluetooth.*/
                SystemClock.sleep(500);
                return;
            }

            OutputStream out = bs.getOutputStream();
            InputStream in = bs.getInputStream();

            out.write(NOTIF_CONN);

            createTlsEngine();
            createBuffers();

            if (TlsHelper.doHandshake(tlsEngine, netDataBuf, out, in) != SSLEngineResult.HandshakeStatus.FINISHED){
                try {
                    out.close();
                    in.close();
                    bs.close();
                } catch (IOException e){}
                SystemClock.sleep(500);
                return;
            }

            NotificationSettings ns = n.getNotificationSettings();

            byte[] encryptedNotificationFlags = TlsHelper.tlsEncrypt(tlsEngine,
                    appDataBuf, netDataBuf, new byte[]{ns.getNotificationFlags()});

            out.write(ConnectionHelper.intToByteArray(encryptedNotificationFlags.length));
            out.write(encryptedNotificationFlags);

            if (ns.includeTitle() || ns.includeMessage()){
                String title = "";
                if (ns.includeTitle()){
                    title = n.getTitle();
                }

                String message = "";
                if (ns.includeMessage()){
                    message = "|||" + n.getMessage();
                }

                byte[] titleAndOrMessage = (title + message).getBytes();
                byte[] encryptedTitleAndOrMessage = TlsHelper.tlsEncrypt(tlsEngine,
                        appDataBuf, netDataBuf, titleAndOrMessage);
                out.write(ConnectionHelper.intToByteArray(encryptedTitleAndOrMessage.length));
                out.write(encryptedTitleAndOrMessage);
            }

            if (ns.includeIcon()){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                n.getIcon().compress(Bitmap.CompressFormat.PNG, 100, bos);

                byte[] encryptedImage = TlsHelper.tlsEncrypt(tlsEngine, appDataBuf, netDataBuf,
                        bos.toByteArray());
                out.write(ConnectionHelper.intToByteArray(encryptedImage.length));

                BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(encryptedImage), 8192);

                byte[] buffer = new byte[8192];
                int len;

                while ((len = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                bos.close();
                bis.close();
            }

            out.close();
            in.close();
            bs.close();
        } catch (IOException e) {
            Log.e("BluetoothNotificatio...", "run");
            Log.e("StackTrace", Log.getStackTraceString(e));
        }
    }

}
