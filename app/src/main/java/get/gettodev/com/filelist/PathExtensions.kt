/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.filelist

import java8.nio.file.Path
import get.gettodev.com.file.MimeType
import get.gettodev.com.file.isSupportedArchive
import get.gettodev.com.provider.archive.archiveFile
import get.gettodev.com.provider.archive.isArchivePath
import get.gettodev.com.provider.document.isDocumentPath
import get.gettodev.com.provider.document.resolver.DocumentResolver
import get.gettodev.com.provider.linux.isLinuxPath

val Path.name: String
    get() = fileName?.toString() ?: if (isArchivePath) archiveFile.fileName.toString() else "/"

fun Path.toUserFriendlyString(): String = if (isLinuxPath) toFile().path else toUri().toString()

fun Path.isArchiveFile(mimeType: MimeType): Boolean = !isArchivePath && mimeType.isSupportedArchive

val Path.isLocalPath: Boolean
    get() =
        isLinuxPath || (isDocumentPath && DocumentResolver.isLocal(this as DocumentResolver.Path))

val Path.isRemotePath: Boolean
    get() = !isLocalPath
