/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.storage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import get.gettodev.com.app.AppActivity
import get.gettodev.com.util.args
import get.gettodev.com.util.putArgs

class EditFtpServerActivity : AppActivity() {
    private val args by args<EditFtpServerFragment.Args>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            val fragment = EditFtpServerFragment().putArgs(args)
            supportFragmentManager.commit { add(android.R.id.content, fragment) }
        }
    }
}
