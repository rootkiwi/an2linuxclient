/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.network;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.cert.Certificate;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;

import kiwi.root.an2linuxclient.crypto.Sha256Helper;
import kiwi.root.an2linuxclient.crypto.TlsHelper;

import static kiwi.root.an2linuxclient.network.PairingConnectionCallbackMessage.CallbackType.*;

import kiwi.root.an2linuxclient.utils.ConnectionHelper;

public class TcpPairingConnection extends PairingConnection {

    private String serverAddress;
    private int serverPort;

    TcpPairingConnection(String serverAddress, int serverPort, Context c) {
        super(c);
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    void acceptPairing() {
        try {
            mOut.write(ACCEPT_PAIRING);
            mPairResponseSent = true;
        } catch (IOException ioe) {}
    }

    @Override
    void denyPairing() {
        try {
            mOut.write(DENY_PAIRING);
            mPairResponseSent = true;
        } catch (IOException ioe) {}
    }

    @Override
    public void run() {
        try {
            Socket s = new Socket();
            try {
                s.connect(new InetSocketAddress(serverAddress, serverPort), 5000);
            } catch (UnknownHostException | IllegalArgumentException e) {
                notifyObservers(new PairingConnectionCallbackMessage(UNKNOWN_HOST));
                try {
                    s.close();
                } catch (IOException e2) {}
                return;
            } catch (SocketTimeoutException e) {
                notifyObservers(new PairingConnectionCallbackMessage(TIMED_OUT));
                try {
                    s.close();
                } catch (IOException e2) {}
                return;
            } catch (SocketException e) {
                notifyObservers(new PairingConnectionCallbackMessage(FAILED_TO_CONNECT));
                try {
                    s.close();
                } catch (IOException e2) {}
                return;
            }

            mOut = s.getOutputStream();

            mOut.write(INITIATE_PAIRING);

            SSLSocket tlsSocket = (SSLSocket) TlsHelper.getPairingTlsContext().getSocketFactory()
                    .createSocket(s, serverAddress, serverPort, true);
            tlsSocket.setUseClientMode(true);
            tlsSocket.setEnabledProtocols(TlsHelper.TLS_VERSIONS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
                tlsSocket.setEnabledCipherSuites(TlsHelper.TLS_CIPHERS);
            } else {
                tlsSocket.setEnabledCipherSuites(TlsHelper.TLS_CIPHERS_COMPAT);
            }

            final byte[] clientCertBytes = TlsHelper.getCertificateBytes(c);
            tlsSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
                @Override
                public void handshakeCompleted(HandshakeCompletedEvent event) {
                    try {
                        Certificate serverCert = event.getPeerCertificates()[0];

                        byte[] sha256 = Sha256Helper.sha256(clientCertBytes, serverCert.getEncoded());

                        notifyObservers(new PairingConnectionCallbackMessage(
                                TLS_HANDSHAKE_COMPLETED,
                                Sha256Helper.getFourLineHexString(sha256),
                                serverCert));

                    } catch (Exception e) {
                        Log.e("TcpPairingConnection", "run:handshakeCompleted");
                        Log.e("StackTrace", Log.getStackTraceString(e));
                    }
                }
            });

            try {
                tlsSocket.startHandshake();
            } catch (IOException e) {
                notifyObservers(new PairingConnectionCallbackMessage(FAILED_TO_CONNECT));
                try {
                    mOut.close();
                    tlsSocket.close();
                } catch (IOException e2) {}
                return;
            }

            mOut = tlsSocket.getOutputStream();
            mIn = tlsSocket.getInputStream();

            mOut.write(ConnectionHelper.intToByteArray(clientCertBytes.length));
            mOut.write(clientCertBytes);

            tlsSocket.setSoTimeout(1000);
            while (!mCancel) {
                try {
                    int serverPairResponse = mIn.read();
                    if (serverPairResponse == ACCEPT_PAIRING) {
                        notifyObservers(new PairingConnectionCallbackMessage(SERVER_ACCEPTED_PAIR));
                        while (!mCancel && !mPairResponseSent) {
                            try {
                                if (mIn.read() == -1) {
                                    // socket closed
                                    notifyObservers(new PairingConnectionCallbackMessage(SOCKET_CLOSED));
                                    mCancel = true;
                                }
                            } catch (SocketTimeoutException e) {}
                        }
                        mCancel = true;
                    } else if (serverPairResponse == DENY_PAIRING) {
                        notifyObservers(new PairingConnectionCallbackMessage(SERVER_DENIED_PAIR));
                        mCancel = true;
                    } else {
                        // socket closed or recieved something strange
                        notifyObservers(new PairingConnectionCallbackMessage(SOCKET_CLOSED));
                        mCancel = true;
                    }
                } catch (SocketTimeoutException ste) {}
            }

            mIn.close();
            mOut.close();
            tlsSocket.close();

        } catch (Exception e) {
            Log.e("TcpPairingConnection", "run");
            Log.e("StackTrace", Log.getStackTraceString(e));
        }
    }

}
