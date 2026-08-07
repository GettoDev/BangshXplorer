/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.provider.root

import android.annotation.SuppressLint
import android.content.Context
import android.os.Process
import android.util.Log
import android.app.Application
import android.content.ContextWrapper
import get.gettodev.com.BuildConfig
import get.gettodev.com.app.application
import get.gettodev.com.app.isApplicationInitialized
import get.gettodev.com.provider.FileSystemProviders
import get.gettodev.com.provider.remote.RemoteFileService
import get.gettodev.com.provider.remote.RemoteInterface
import get.gettodev.com.util.lazyReflectedMethod

var isServerProcess = false
    internal set

val isRunningAsRoot: Boolean
    get() = Process.myUid() == 0 || Process.myUid() == 2000 || isServerProcess


@SuppressLint("StaticFieldLeak")
lateinit var rootContext: Context private set

object RootFileService : RemoteFileService(
    RemoteInterface {
        try {
            SuiFileServiceLauncher.launchService()
        } catch (e: Exception) {
            // Log the error for debugging
            android.util.Log.e("RootFileService", "Shizuku launch failed: ${e.message}", e)
            if (LibSuFileServiceLauncher.isSuAvailable()) {
                LibSuFileServiceLauncher.launchService()
            } else {
                android.util.Log.e("RootFileService", "LibSu also not available")
                throw e
            }
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
        isServerProcess = true
        Log.i(LOG_TAG, "Creating package context (server process)")
        rootContext = createPackageContext(BuildConfig.APPLICATION_ID)
        if (!isApplicationInitialized) {
            val app = (rootContext.applicationContext as? Application)
                ?: (rootContext as? Application)
                ?: makeApplication(rootContext)
            application = app
        }
        Log.i(LOG_TAG, "Installing file system providers")
        FileSystemProviders.install()
        FileSystemProviders.overflowWatchEvents = true
    }

    private fun makeApplication(context: Context): Application {
        val app = Application()
        val attachMethod = ContextWrapper::class.java.getDeclaredMethod(
            "attachBaseContext", Context::class.java
        )
        attachMethod.isAccessible = true
        attachMethod.invoke(app, context)
        return app
    }

    private fun createPackageContext(packageName: String): Context {
        val activityThread = activityThreadCurrentActivityThreadMethod.invoke(null)
        val systemContext = activityThreadGetSystemContextMethod.invoke(activityThread) as Context
        return systemContext.createPackageContext(
            packageName, Context.CONTEXT_IGNORE_SECURITY or Context.CONTEXT_INCLUDE_CODE
        )
    }
}
