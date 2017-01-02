/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.cert.Certificate;

import javax.net.ssl.SSLSocket;

import kiwi.root.an2linuxclient.crypto.TlsHelper;
import kiwi.root.an2linuxclient.data.Notification;
import kiwi.root.an2linuxclient.data.NotificationSettings;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

class TcpNotificationConnection extends NotificationConnection {

    private String serverAddress;
    private int serverPort;

    TcpNotificationConnection(Context c, Notification n, Certificate serverCert, String serverAddress, int serverPort){
        super(c, n, serverCert);
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public void run(){
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(serverAddress, serverPort), 5000);
            OutputStream out = s.getOutputStream();

            out.write(NOTIF_CONN);

            SSLSocket tlsSocket = (SSLSocket) TlsHelper.getNotificationTlsContext(c, serverCert).getSocketFactory()
                    .createSocket(s, serverAddress, serverPort, true);
            tlsSocket.setUseClientMode(true);
            tlsSocket.setEnabledProtocols(TlsHelper.TLS_VERSIONS);
            tlsSocket.setEnabledCipherSuites(TlsHelper.TLS_CIPHERS);

            try {
                tlsSocket.startHandshake();
            } catch (IOException e) {
                try {
                    out.close();
                    tlsSocket.close();
                } catch (IOException e2) {}
                return;
            }

            out = tlsSocket.getOutputStream();

            NotificationSettings ns = n.getNotificationSettings();

            out.write(ns.getNotificationFlags());

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
                out.write(ConnectionHelper.intToByteArray(titleAndOrMessage.length));
                out.write(titleAndOrMessage);
            }

            if (ns.includeIcon()){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                n.getIcon().compress(Bitmap.CompressFormat.PNG, 100, bos);

                byte[] image = bos.toByteArray();
                byte[] imageSize = ConnectionHelper.intToByteArray(image.length);
                out.write(imageSize);

                BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(image), 8192);

                byte[] buffer = new byte[8192];
                int len;

                while ((len = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                bos.close();
                bis.close();
            }
            out.close();
            tlsSocket.close();
        } catch(IOException e) {
            Log.e("TcpNotificationConne...", "run");
            Log.e("StackTrace", Log.getStackTraceString(e));
        }
    }

}
