package get.gettodev.com.provider.remote;

import get.gettodev.com.provider.remote.ParcelableException;
import get.gettodev.com.util.RemoteCallback;

interface IRemotePathObservable {
    void addObserver(in RemoteCallback observer);

    void close(out ParcelableException exception);
}
