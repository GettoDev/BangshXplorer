package get.gettodev.com.util

import android.os.storage.StorageVolume
import get.gettodev.com.compat.directoryCompat

val StorageVolume.isMounted: Boolean
    get() = directoryCompat != null
