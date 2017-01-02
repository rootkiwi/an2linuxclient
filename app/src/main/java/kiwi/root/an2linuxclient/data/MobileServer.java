/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.data;

import java.security.cert.Certificate;

public class MobileServer extends TcpServer {

    private boolean roamingAllowed;

    MobileServer(){
    }

    /**Add server to database existing certificate*/
    public MobileServer(String ipOrHostname,
                        int portNumber,
                        boolean roamingAllowed){
        this.ipOrHostname = ipOrHostname;
        this.portNumber = portNumber;
        this.roamingAllowed = roamingAllowed;
    }

    /**Add server to database new certificate*/
    public MobileServer(Certificate certificate,
                        String ipOrHostname,
                        int portNumber,
                        boolean roamingAllowed){
        this.certificate = certificate;
        this.ipOrHostname = ipOrHostname;
        this.portNumber = portNumber;
        this.roamingAllowed = roamingAllowed;
    }

    /**Update server in database existing certificate*/
    public MobileServer(long id,
                        String ipOrHostname,
                        int portNumber,
                        boolean roamingAllowed){
        this.id = id;
        this.ipOrHostname = ipOrHostname;
        this.portNumber = portNumber;
        this.roamingAllowed = roamingAllowed;
    }

    /**Update server in database new certificate*/
    public MobileServer(long id,
                        Certificate certificate,
                        String ipOrHostname,
                        int portNumber,
                        boolean roamingAllowed){
        this.id = id;
        this.certificate = certificate;
        this.ipOrHostname = ipOrHostname;
        this.portNumber = portNumber;
        this.roamingAllowed = roamingAllowed;
    }

    void setRoamingAllowed(boolean roamingAllowed) {
        this.roamingAllowed = roamingAllowed;
    }

    public boolean isRoamingAllowed() {
        return this.roamingAllowed;
    }
}
