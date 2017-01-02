/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.data;

public abstract class TcpServer extends Server {

    String ipOrHostname;
    int portNumber;

    void setIpOrHostname(String ipOrHostname) {
        this.ipOrHostname = ipOrHostname;
    }

    void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getIpOrHostname() {
        return this.ipOrHostname;
    }

    public int getPortNumber() {
        return this.portNumber;
    }

}
