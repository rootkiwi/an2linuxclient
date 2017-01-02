/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.data;

import android.util.Log;

import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

public abstract class Server implements Comparable<Server> {

    long id;
    private boolean isEnabled;
    Certificate certificate;
    private long certificateId;

    public void setId(long id) {
        this.id = id;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void setCertificate(byte[] certificateBytes){
        try {
            X509CertificateHolder certificateHolder = new X509CertificateHolder(certificateBytes);
            this.certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);
        } catch (Exception e) {
            Log.e("Server", "setCertificate");
            Log.e("StackTrace", Log.getStackTraceString(e));
        }
    }

    void setCertificateId(long certificateId){
        this.certificateId = certificateId;
    }

    public long getId() {
        return this.id;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public Certificate getCertificate(){
        return this.certificate;
    }

    byte[] getCertificateBytes(){
        try {
            return this.certificate.getEncoded();
        } catch (CertificateEncodingException e){
            Log.e("Server", "getCertificateBytes");
            Log.e("StackTrace", Log.getStackTraceString(e));
            return null;
        }
    }

    public long getCertificateId(){
        return certificateId;
    }

    @Override
    public int compareTo(Server server) {
        return Long.compare(this.id, server.getId());
    }

}
