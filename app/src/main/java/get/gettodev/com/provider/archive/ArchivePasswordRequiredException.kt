/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.provider.archive

import android.content.Context
import java8.nio.file.Path
import get.gettodev.com.fileaction.ArchivePasswordDialogActivity
import get.gettodev.com.fileaction.ArchivePasswordDialogFragment
import get.gettodev.com.provider.common.UserAction
import get.gettodev.com.provider.common.UserActionRequiredException
import get.gettodev.com.util.createIntent
import get.gettodev.com.util.putArgs
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class ArchivePasswordRequiredException(
    private val file: Path,
    reason: String?
) :
    UserActionRequiredException(file.toString(), null, reason) {

    override fun getUserAction(continuation: Continuation<Boolean>, context: Context): UserAction {
        return UserAction(
            ArchivePasswordDialogActivity::class.createIntent().putArgs(
                ArchivePasswordDialogFragment.Args(file) { continuation.resume(it) }
            ), ArchivePasswordDialogFragment.getTitle(context),
            ArchivePasswordDialogFragment.getMessage(file, context)
        )
    }
}
