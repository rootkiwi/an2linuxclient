/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.data;

import java.security.cert.Certificate;

public class WifiServer extends TcpServer {

    private String ssidWhitelist;

    WifiServer(){
    }

    /**Add server to database existing certificate*/
    public WifiServer(String ipOrHostname,
                      int portNumber,
                      String ssidWhitelist){
        this.ipOrHostname = ipOrHostname;
        this.portNumber = portNumber;
        this.ssidWhitelist = ssidWhitelist;
    }

    /**Add server to database new certificate*/
    public WifiServer(Certificate certificate,
                      String ipOrHostname,
                      int portNumber,
                      String ssidWhitelist){
        this.certificate = certificate;
        this.ipOrHostname = ipOrHostname;
        this.portNumber = portNumber;
        this.ssidWhitelist = ssidWhitelist;
    }

    /**Update server in database existing certificate*/
    public WifiServer(long id,
                      String ipOrHostname,
                      int portNumber,
                      String ssidWhitelist){
        this.id = id;
        this.ipOrHostname = ipOrHostname;
        this.portNumber = portNumber;
        this.ssidWhitelist = ssidWhitelist;
    }

    /**Update server in database new certificate*/
    public WifiServer(long id,
                      Certificate certificate,
                      String ipOrHostname,
                      int portNumber,
                      String ssidWhitelist){
        this.id = id;
        this.certificate = certificate;
        this.ipOrHostname = ipOrHostname;
        this.portNumber = portNumber;
        this.ssidWhitelist = ssidWhitelist;
    }

    void setSsidWhitelist(String ssidWhitelist) {
        this.ssidWhitelist = ssidWhitelist;
    }

    public String getSsidWhitelist() {
        return this.ssidWhitelist;
    }

}
