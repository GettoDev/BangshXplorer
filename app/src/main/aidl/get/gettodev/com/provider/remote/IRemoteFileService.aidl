package get.gettodev.com.provider.remote;

import get.gettodev.com.provider.remote.IRemoteFileSystem;
import get.gettodev.com.provider.remote.IRemoteFileSystemProvider;
import get.gettodev.com.provider.remote.IRemotePosixFileAttributeView;
import get.gettodev.com.provider.remote.IRemotePosixFileStore;
import get.gettodev.com.provider.remote.ParcelableObject;

interface IRemoteFileService {
    IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(String scheme);

    IRemoteFileSystem getRemoteFileSystemInterface(in ParcelableObject fileSystem);

    IRemotePosixFileStore getRemotePosixFileStoreInterface(in ParcelableObject fileStore);

    IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
        in ParcelableObject attributeView
    );
}
