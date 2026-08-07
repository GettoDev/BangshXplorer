/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.storage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import get.gettodev.com.app.AppActivity
import get.gettodev.com.util.args
import get.gettodev.com.util.putArgs

class EditSftpServerActivity : AppActivity() {
    private val args by args<EditSftpServerFragment.Args>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            val fragment = EditSftpServerFragment().putArgs(args)
            supportFragmentManager.commit { add(android.R.id.content, fragment) }
        }
    }
}
