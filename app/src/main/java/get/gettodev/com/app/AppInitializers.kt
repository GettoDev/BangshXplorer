/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.app

import android.os.AsyncTask
import android.os.Build
import android.webkit.WebView
import jcifs.context.SingletonContext
import get.gettodev.com.BuildConfig
import get.gettodev.com.coil.initializeCoil
import get.gettodev.com.filejob.fileJobNotificationTemplate
import get.gettodev.com.ftpserver.ftpServerServiceNotificationTemplate
import get.gettodev.com.hiddenapi.HiddenApi
import get.gettodev.com.provider.FileSystemProviders
import get.gettodev.com.settings.Settings
import get.gettodev.com.storage.FtpServerAuthenticator
import get.gettodev.com.storage.SftpServerAuthenticator
import get.gettodev.com.storage.SmbServerAuthenticator
import get.gettodev.com.storage.StorageVolumeListLiveData
import get.gettodev.com.storage.WebDavServerAuthenticator
import get.gettodev.com.theme.custom.CustomThemeHelper
import get.gettodev.com.theme.night.NightModeHelper
import java.util.Properties
import get.gettodev.com.provider.ftp.client.Client as FtpClient
import get.gettodev.com.provider.sftp.client.Client as SftpClient
import get.gettodev.com.provider.smb.client.Client as SmbClient
import get.gettodev.com.provider.webdav.client.Client as WebDavClient

val appInitializers = listOf(
    ::disableHiddenApiChecks,
    ::initializeWebViewDebugging,
    ::initializeCoil,
    ::initializeFileSystemProviders,
    ::upgradeApp,
    ::initializeLiveDataObjects,
    ::initializeCustomTheme,
    ::initializeNightMode,
    ::createNotificationChannels
)

private fun disableHiddenApiChecks() {
    HiddenApi.disableHiddenApiChecks()
}

private fun initializeWebViewDebugging() {
    if (BuildConfig.DEBUG) {
        WebView.setWebContentsDebuggingEnabled(true)
    }
}

private fun initializeFileSystemProviders() {
    FileSystemProviders.install()
    FileSystemProviders.overflowWatchEvents = true
    // SingletonContext.init() calls NameServiceClientImpl.initCache() which connects to network.
    AsyncTask.THREAD_POOL_EXECUTOR.execute {
        SingletonContext.init(
            Properties().apply {
                setProperty("jcifs.netbios.cachePolicy", "0")
                setProperty("jcifs.smb.client.maxVersion", "SMB1")
            }
        )
    }
    FtpClient.authenticator = FtpServerAuthenticator
    SftpClient.authenticator = SftpServerAuthenticator
    SmbClient.authenticator = SmbServerAuthenticator
    WebDavClient.authenticator = WebDavServerAuthenticator
}

private fun initializeLiveDataObjects() {
    // Force initialization of LiveData objects so that it won't happen on a background thread.
    StorageVolumeListLiveData.value
    Settings.FILE_LIST_DEFAULT_DIRECTORY.value
}

private fun initializeCustomTheme() {
    CustomThemeHelper.initialize(application)
}

private fun initializeNightMode() {
    NightModeHelper.initialize(application)
}

private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.createNotificationChannels(
            listOf(
                backgroundActivityStartNotificationTemplate.channelTemplate,
                fileJobNotificationTemplate.channelTemplate,
                ftpServerServiceNotificationTemplate.channelTemplate
            ).map { it.create(application) }
        )
    }
}
