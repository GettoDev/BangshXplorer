/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.filelist

import android.content.Intent
import android.os.Bundle
import java8.nio.file.Path
import get.gettodev.com.app.AppActivity
import get.gettodev.com.app.application
import get.gettodev.com.file.MimeType
import get.gettodev.com.file.asMimeTypeOrNull
import get.gettodev.com.file.fileProviderUri
import get.gettodev.com.filejob.FileJobService
import get.gettodev.com.provider.archive.isArchivePath
import get.gettodev.com.util.createViewIntent
import get.gettodev.com.util.extraPath
import get.gettodev.com.util.startActivitySafe

class OpenFileActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val path = intent.extraPath
        val mimeType = intent.type?.asMimeTypeOrNull()
        if (path != null && mimeType != null) {
            openFile(path, mimeType)
        }
        finish()
    }

    private fun openFile(path: Path, mimeType: MimeType) {
        if (path.isArchivePath) {
            FileJobService.open(path, mimeType, false, this)
        } else {
            val intent = path.fileProviderUri.createViewIntent(mimeType)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .apply { extraPath = path }
            startActivitySafe(intent)
        }
    }

    companion object {
        private const val ACTION_OPEN_FILE = "get.gettodev.com.intent.action.OPEN_FILE"

        fun createIntent(path: Path, mimeType: MimeType): Intent =
            Intent(ACTION_OPEN_FILE)
                .setPackage(application.packageName)
                .setType(mimeType.value)
                .apply { extraPath = path }
    }
}
