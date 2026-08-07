package get.gettodev.com.provider.remote;

import get.gettodev.com.provider.remote.ParcelableException;

interface IRemoteFileSystem {
    void close(out ParcelableException exception);
}
