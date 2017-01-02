/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.data;

import kiwi.root.an2linuxclient.crypto.Sha1Helper;
import kiwi.root.an2linuxclient.interfaces.CertificateSpinnerItem;

public class CertificateIdAndFingerprint implements CertificateSpinnerItem {

    private long id;
    private String fingerprint;

    CertificateIdAndFingerprint(long id, String fingerprint){
        this.id = id;
        this.fingerprint = fingerprint;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return Sha1Helper.getTwoLineHexString(fingerprint);
    }

}
