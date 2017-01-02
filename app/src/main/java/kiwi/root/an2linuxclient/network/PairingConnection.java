package kiwi.root.an2linuxclient.network;

import android.content.Context;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;

abstract class PairingConnection implements Runnable {

    Context c;
    private PairingObservable observable;

    boolean mCancel;
    boolean mPairResponseSent;
    OutputStream mOut;
    InputStream mIn;

    final byte INITIATE_PAIRING = 0;
    final byte DENY_PAIRING = 2;
    final byte ACCEPT_PAIRING = 3;

    PairingConnection(Context c) {
        this.c = c;
        this.observable = new PairingObservable();
    }

    abstract void acceptPairing();

    abstract void denyPairing();

    void cancel() {
        if (!mPairResponseSent && mOut != null) {
            denyPairing();
        }
        mCancel = true;
    }

    void notifyObservers(Object data) {
        observable.notifyObservers(data);
    }

    void addObserver(Observer observer) {
        observable.addObserver(observer);
    }

    void deleteObservers(){
        observable.deleteObservers();
    }

    private class PairingObservable extends Observable {

        @Override
        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }

    }

}
