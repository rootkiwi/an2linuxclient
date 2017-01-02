/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.network;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class ThreadPoolHandler {

    private static ThreadPoolExecutor threadPool;
    private static ThreadPoolExecutor bluetoothThreadPool;

    private static final long KEEP_ALIVE_TIME = 30;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    static void enqueueRunnable(Runnable r){
        if (threadPool == null || threadPool.isShutdown()){
            final int CORE_POOL_SIZE = 2;
            final int MAXIMUM_POOL_SIZE = 2;
            threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,
                    MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE_TIME, TIME_UNIT,
                    new LinkedBlockingQueue<Runnable>());
            threadPool.allowCoreThreadTimeOut(true);
        }
        threadPool.execute(r);
    }

    /**
     * needed so that no more than one bluetooth connection is active at any time
     */
    static void enqueueBtConn(Runnable r){
        if (bluetoothThreadPool == null || bluetoothThreadPool.isShutdown()){
            final int CORE_POOL_SIZE = 0;
            final int MAXIMUM_POOL_SIZE = 1;
            bluetoothThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,
                    MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE_TIME, TIME_UNIT,
                    new LinkedBlockingQueue<Runnable>());
        }
        bluetoothThreadPool.execute(r);
    }

}
