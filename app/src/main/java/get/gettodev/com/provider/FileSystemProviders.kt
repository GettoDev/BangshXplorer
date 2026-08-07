/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.provider

import java8.nio.file.Files
import java8.nio.file.ProviderNotFoundException
import java8.nio.file.spi.FileSystemProvider
import get.gettodev.com.provider.archive.ArchiveFileSystemProvider
import get.gettodev.com.provider.common.AndroidFileTypeDetector
import get.gettodev.com.provider.content.ContentFileSystemProvider
import get.gettodev.com.provider.document.DocumentFileSystemProvider
import get.gettodev.com.provider.ftp.FtpFileSystemProvider
import get.gettodev.com.provider.ftp.FtpesFileSystemProvider
import get.gettodev.com.provider.ftp.FtpsFileSystemProvider
import get.gettodev.com.provider.linux.LinuxFileSystemProvider
import get.gettodev.com.provider.root.isRunningAsRoot
import get.gettodev.com.provider.sftp.SftpFileSystemProvider
import get.gettodev.com.provider.smb.SmbFileSystemProvider
import get.gettodev.com.provider.webdav.WebDavFileSystemProvider
import get.gettodev.com.provider.webdav.WebDavsFileSystemProvider

object FileSystemProviders {
    /**
     * If set, WatchService implementations will skip processing any event data and simply send an
     * overflow event to all the registered keys upon successful read from the inotify fd. This can
     * help reducing the JNI and GC overhead when large amount of inotify events are generated.
     * Simply sending an overflow event to all the keys is okay because we use only one key per
     * service for WatchServicePathObservable.
     */
    @Volatile
    var overflowWatchEvents = false

    fun install() {
        FileSystemProvider.installDefaultProvider(LinuxFileSystemProvider)
        FileSystemProvider.installProvider(ArchiveFileSystemProvider)
        if (!isRunningAsRoot) {
            FileSystemProvider.installProvider(ContentFileSystemProvider)
            FileSystemProvider.installProvider(DocumentFileSystemProvider)
            FileSystemProvider.installProvider(FtpFileSystemProvider)
            FileSystemProvider.installProvider(FtpsFileSystemProvider)
            FileSystemProvider.installProvider(FtpesFileSystemProvider)
            FileSystemProvider.installProvider(SftpFileSystemProvider)
            FileSystemProvider.installProvider(SmbFileSystemProvider)
            FileSystemProvider.installProvider(WebDavFileSystemProvider)
            FileSystemProvider.installProvider(WebDavsFileSystemProvider)
        }
        Files.installFileTypeDetector(AndroidFileTypeDetector)
    }

    operator fun get(scheme: String): FileSystemProvider {
        for (provider in FileSystemProvider.installedProviders()) {
            if (provider.scheme.equals(scheme, ignoreCase = true)) {
                return provider
            }
        }
        throw ProviderNotFoundException(scheme)
    }
}
