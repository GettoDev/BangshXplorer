/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.provider.root

import android.os.Build
import java8.nio.file.Path
import get.gettodev.com.settings.Settings
import get.gettodev.com.util.valueCompat
import java.io.IOException

interface RootablePath {
    fun isRootRequired(isAttributeAccess: Boolean): Boolean
}

private val rootStrategy: RootStrategy
    get() {
        // Inside server process (:sui), ALWAYS use local provider directly
        if (isServerProcess) return RootStrategy.NEVER

        // Force ALWAYS strategy when Shizuku is available
        // This bypasses Android 11+ restrictions for Android/data
        if (isRunningAsRoot) return RootStrategy.ALWAYS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val version = rikka.shizuku.Shizuku.getVersion()
                if (version >= 0) {
                    android.util.Log.d("RootablePath", "Shizuku detected (version=$version), forcing ALWAYS strategy")
                    return RootStrategy.ALWAYS
                }
            } catch (e: Exception) {
                android.util.Log.d("RootablePath", "Shizuku not available: ${e.message}")
            }
        }
        return Settings.ROOT_STRATEGY.valueCompat
    }

@Throws(IOException::class)
fun <T, R> callRootable(
    path: Path,
    isAttributeAccess: Boolean,
    localObject: T,
    rootObject: T, block: T.() -> R
): R {
    path as? RootablePath ?: throw IllegalArgumentException("$path is not a RootablePath")
    return when (rootStrategy) {
        RootStrategy.NEVER -> localObject.block()
        RootStrategy.AUTOMATIC ->
            if (path.isRootRequired(isAttributeAccess)) {
                rootObject.block()
            } else {
                localObject.block()
            }
        RootStrategy.ALWAYS -> rootObject.block()
    }
}

@Throws(IOException::class)
fun <T, R> callRootable(
    path1: Path,
    path2: Path,
    isAttributeAccess: Boolean,
    localObject: T,
    rootObject: T,
    block: T.() -> R
): R {
    path1 as? RootablePath ?: throw IllegalArgumentException("$path1 is not a RootablePath")
    path2 as? RootablePath ?: throw IllegalArgumentException("$path2 is not a RootablePath")
    return when (rootStrategy) {
        RootStrategy.NEVER ->
            localObject.block()
        RootStrategy.AUTOMATIC ->
            if (path1.isRootRequired(isAttributeAccess)
                || path2.isRootRequired(isAttributeAccess)) {
                rootObject.block()
            } else {
                localObject.block()
            }
        RootStrategy.ALWAYS ->
            rootObject.block()
    }
}
