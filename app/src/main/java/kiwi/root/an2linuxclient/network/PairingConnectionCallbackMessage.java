/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.network;

import java.security.cert.Certificate;

public class PairingConnectionCallbackMessage {

    private CallbackType type;
    private String verifyHash;
    private Certificate serverCert;

    public enum CallbackType {
        NOT_CONNECTED,
        DISALLOWED_SSID,
        NOT_CONNECTED_TO_WIFI,
        NOT_ALLOWED_TO_ROAM,
        BLUETOOTH_NOT_ENABLED,
        UNKNOWN_HOST,
        TIMED_OUT,
        FAILED_TO_CONNECT,
        TLS_HANDSHAKE_COMPLETED,
        SERVER_ACCEPTED_PAIR,
        SERVER_DENIED_PAIR,
        SOCKET_CLOSED
    }

    public PairingConnectionCallbackMessage(CallbackType type){
        this.type = type;
    }

    public PairingConnectionCallbackMessage(CallbackType type, String verifyHash, Certificate serverCert){
        this.type = type;
        this.verifyHash = verifyHash;
        this.serverCert = serverCert;
    }

    public CallbackType getType(){
        return this.type;
    }

    public String getVerifyHash(){
        return this.verifyHash;
    }

    public Certificate getServerCert(){
        return this.serverCert;
    }

}
