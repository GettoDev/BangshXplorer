/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.compat

import android.content.pm.PackageInfo
import androidx.core.content.pm.PackageInfoCompat

val PackageInfo.longVersionCodeCompat: Long
    get() = PackageInfoCompat.getLongVersionCode(this)
