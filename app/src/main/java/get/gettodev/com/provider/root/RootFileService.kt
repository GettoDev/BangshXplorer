/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * Copyright (c) 2026 GettoDev
 * All Rights Reserved.
 */

package get.gettodev.com.provider.root

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import get.gettodev.com.BuildConfig
import get.gettodev.com.provider.FileSystemProviders
import get.gettodev.com.provider.remote.RemoteFileService
import get.gettodev.com.provider.remote.RemoteInterface
import get.gettodev.com.util.lazyReflectedMethod

/**
 * True inside an elevated UserService / RootService process.
 *
 * - UID 0: libsu or Sui (Magisk)
 * - UID [Process.SHELL_UID] (2000): Shizuku started via ADB
 *
 * Must be true in those processes so [callRootable] uses local syscalls and does not try to
 * escalate again (which would recurse / time out).
 */
val isRunningAsRoot: Boolean
    get() {
        val uid = Process.myUid()
        return uid == 0 || uid == Process.SHELL_UID
    }

@SuppressLint("StaticFieldLeak")
lateinit var rootContext: Context private set

object RootFileService : RemoteFileService(
    RemoteInterface {
        when {
            SuiFileServiceLauncher.isSuiAvailable() ->
                SuiFileServiceLauncher.launchService()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ShizukuFileServiceLauncher.isShizukuAvailable() ->
                ShizukuFileServiceLauncher.launchService()
            else ->
                LibSuFileServiceLauncher.launchService()
        }
    }
) {
    const val TIMEOUT_MILLIS = 15 * 1000L

    private val LOG_TAG = RootFileService::class.java.simpleName

    // Not actually restricted because there's no restriction when running as root.
    //@RestrictedHiddenApi
    private val activityThreadCurrentActivityThreadMethod by lazyReflectedMethod(
        "android.app.ActivityThread", "currentActivityThread"
    )
    //@RestrictedHiddenApi
    private val activityThreadGetSystemContextMethod by lazyReflectedMethod(
        "android.app.ActivityThread", "getSystemContext"
    )

    fun main() {
        Log.i(LOG_TAG, "Creating package context (uid=${Process.myUid()})")
        rootContext = createPackageContext(BuildConfig.APPLICATION_ID)
        Log.i(LOG_TAG, "Installing file system providers")
        FileSystemProviders.install()
        FileSystemProviders.overflowWatchEvents = true
    }

    private fun createPackageContext(packageName: String): Context {
        val activityThread = activityThreadCurrentActivityThreadMethod.invoke(null)
        val systemContext = activityThreadGetSystemContextMethod.invoke(activityThread) as Context
        return systemContext.createPackageContext(
            packageName, Context.CONTEXT_IGNORE_SECURITY or Context.CONTEXT_INCLUDE_CODE
        )
    }
}
