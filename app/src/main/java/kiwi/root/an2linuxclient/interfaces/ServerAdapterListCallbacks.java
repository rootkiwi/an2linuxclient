/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.interfaces;

import kiwi.root.an2linuxclient.data.Server;

public interface ServerAdapterListCallbacks {
    void addServer(Server server);
    void deleteServer(int serverListPosition);
    void updateServer(Server server, int serverListPosition);
}
