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
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.UUID;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;

import kiwi.root.an2linuxclient.crypto.Sha1Helper;
import kiwi.root.an2linuxclient.crypto.TlsHelper;
import kiwi.root.an2linuxclient.utils.ConnectionHelper;

import static kiwi.root.an2linuxclient.network.PairingConnectionCallbackMessage.CallbackType.FAILED_TO_CONNECT;
import static kiwi.root.an2linuxclient.network.PairingConnectionCallbackMessage.CallbackType.SERVER_ACCEPTED_PAIR;
import static kiwi.root.an2linuxclient.network.PairingConnectionCallbackMessage.CallbackType.SERVER_DENIED_PAIR;
import static kiwi.root.an2linuxclient.network.PairingConnectionCallbackMessage.CallbackType.SOCKET_CLOSED;
import static kiwi.root.an2linuxclient.network.PairingConnectionCallbackMessage.CallbackType.TLS_HANDSHAKE_COMPLETED;

public class BluetoothPairingConnection extends PairingConnection {

    private String serverMacAddress;

    private SSLEngine tlsEngine;
    private ByteBuffer appDataBuf;
    private ByteBuffer netDataBuf;

    BluetoothPairingConnection(String serverMacAddress, Context c) {
        super(c);
        this.serverMacAddress = serverMacAddress;
    }

    @Override
    void acceptPairing() {
        try {
            byte[] encrypted = TlsHelper.tlsEncrypt(tlsEngine, appDataBuf, netDataBuf, new byte[]{ACCEPT_PAIRING});
            mOut.write(ConnectionHelper.intToByteArray(encrypted.length));
            mOut.write(encrypted);
            mPairResponseSent = true;
        } catch (IOException ioe) {}
    }

    @Override
    void denyPairing() {
        try {
            byte[] encrypted = TlsHelper.tlsEncrypt(tlsEngine, appDataBuf, netDataBuf, new byte[]{DENY_PAIRING});
            mOut.write(ConnectionHelper.intToByteArray(encrypted.length));
            mOut.write(encrypted);
            mPairResponseSent = true;
        } catch (IOException ioe) {}
    }

    private void createTlsEngine(){
        tlsEngine = TlsHelper.getPairingTlsContext().createSSLEngine();
        tlsEngine.setUseClientMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
            tlsEngine.setEnabledProtocols(TlsHelper.TLS_VERSIONS);
            tlsEngine.setEnabledCipherSuites(TlsHelper.TLS_CIPHERS);
        } else {
            tlsEngine.setEnabledProtocols(TlsHelper.TLS_VERSIONS_COMPAT_BT);
            tlsEngine.setEnabledCipherSuites(TlsHelper.TLS_CIPHERS_COMPAT_BT);
        }
    }

    private void createBuffers(){
        appDataBuf = ByteBuffer.allocate(tlsEngine.getSession().getApplicationBufferSize());
        netDataBuf = ByteBuffer.allocate(tlsEngine.getSession().getPacketBufferSize());
    }

    @Override
    public void run() {
        try {
            BluetoothSocket bs = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(serverMacAddress)
                    .createRfcommSocketToServiceRecord(UUID.fromString("a97fbf21-2ef3-4daf-adfb-2a53ffa87b8e"));

            try {
                bs.connect();
            } catch (IOException connectException) {
                notifyObservers(new PairingConnectionCallbackMessage(FAILED_TO_CONNECT));
                try {
                    bs.close();
                } catch (IOException closeException) {}
                SystemClock.sleep(500);
                return;
            }

            mOut = bs.getOutputStream();
            mIn = bs.getInputStream();

            mOut.write(INITIATE_PAIRING);

            createTlsEngine();
            createBuffers();

            byte[] clientCertBytes = TlsHelper.getCertificateBytes(c);
            if (TlsHelper.doHandshake(tlsEngine, netDataBuf, mOut, mIn) == SSLEngineResult.HandshakeStatus.FINISHED){
                try {
                    Certificate serverCert = tlsEngine.getSession().getPeerCertificates()[0];

                    byte[] sha1Hash = Sha1Helper.sha1(clientCertBytes, serverCert.getEncoded());

                    notifyObservers(new PairingConnectionCallbackMessage(
                            TLS_HANDSHAKE_COMPLETED,
                            Sha1Helper.getTwoLineHexString(sha1Hash),
                            serverCert));
                } catch (CertificateEncodingException e){
                    Log.e("BluetoothPairingConn...", "run:handshakeCompleted");
                    Log.e("StackTrace", Log.getStackTraceString(e));
                }
            } else {
                notifyObservers(new PairingConnectionCallbackMessage(FAILED_TO_CONNECT));
                try {
                    mOut.close();
                    mIn.close();
                    bs.close();
                } catch (IOException e2) {}
                SystemClock.sleep(500);
                return;
            }

            byte[] encryptedClientCert = TlsHelper.tlsEncrypt(tlsEngine, appDataBuf, netDataBuf, clientCertBytes);
            /*I don't know how else to do this when using SSLEngine/SSL_BIO, but I don't see any security
            issue with sending the length of the encrypted data mIn cleartext, using something like wireshark
            it's possible to see the length anyway*/
            mOut.write(ConnectionHelper.intToByteArray(encryptedClientCert.length));
            mOut.write(encryptedClientCert);

            while (!mCancel) {
                /*Don't really know how to do non blocking or timeout in a good way with
                bluetooth socket so I will currently with this solution not be able to
                notice if the other peer (server) have closed the socket*/
                if (mIn.available() > 0){
                    try {
                        int serverPairResponseSize = ByteBuffer.wrap(ConnectionHelper.readAll(4, mIn)).getInt();
                        byte[] serverPairResponseEncrypted = ConnectionHelper.readAll(serverPairResponseSize, mIn);
                        byte serverPairResponse = TlsHelper.tlsDecrypt(tlsEngine, appDataBuf, netDataBuf, serverPairResponseEncrypted)[0];
                        if (serverPairResponse == ACCEPT_PAIRING) {
                            notifyObservers(new PairingConnectionCallbackMessage(SERVER_ACCEPTED_PAIR));
                            while (!mCancel && !mPairResponseSent) {
                                SystemClock.sleep(1000);
                            }
                            mCancel = true;
                        } else if (serverPairResponse == DENY_PAIRING) {
                            notifyObservers(new PairingConnectionCallbackMessage(SERVER_DENIED_PAIR));
                            mCancel = true;
                        } else {
                            // recieved something strange
                            notifyObservers(new PairingConnectionCallbackMessage(SOCKET_CLOSED));
                            mCancel = true;
                        }
                    } catch (IOException ioe){
                        // socket closed
                        notifyObservers(new PairingConnectionCallbackMessage(SOCKET_CLOSED));
                        mCancel = true;
                    }
                } else {
                    SystemClock.sleep(1000);
                }
            }

            mIn.close();
            mOut.close();
            bs.close();
        } catch (IOException e){
            Log.e("BluetoothPairingConn...", "run");
            Log.e("StackTrace", Log.getStackTraceString(e));
        }
    }

}
