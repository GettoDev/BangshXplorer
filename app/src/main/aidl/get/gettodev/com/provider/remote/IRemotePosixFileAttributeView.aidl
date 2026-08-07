package get.gettodev.com.provider.remote;

import get.gettodev.com.provider.common.ParcelableFileTime;
import get.gettodev.com.provider.common.ParcelablePosixFileMode;
import get.gettodev.com.provider.common.PosixGroup;
import get.gettodev.com.provider.common.PosixUser;
import get.gettodev.com.provider.remote.ParcelableException;
import get.gettodev.com.provider.remote.ParcelableObject;

interface IRemotePosixFileAttributeView {
    ParcelableObject readAttributes(out ParcelableException exception);

    void setTimes(
        in ParcelableFileTime lastModifiedTime,
        in ParcelableFileTime lastAccessTime,
        in ParcelableFileTime createTime,
        out ParcelableException exception
    );

    void setOwner(in PosixUser owner, out ParcelableException exception);

    void setGroup(in PosixGroup group, out ParcelableException exception);

    void setMode(in ParcelablePosixFileMode mode, out ParcelableException exception);

    void setSeLinuxContext(in ParcelableObject context, out ParcelableException exception);

    void restoreSeLinuxContext(out ParcelableException exception);
}
