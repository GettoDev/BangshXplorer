/*
 * Copyright (c) 2026 GettoDev
 * All Rights Reserved.
 */

package get.gettodev.com.provider.root

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method

/**
 * Triggers Android StorageManager fstrim (`sm fstrim`) through Shizuku when available.
 *
 * Shizuku runs as shell UID (or root with Sui), which can invoke `sm fstrim` like `adb shell`.
 * Direct `/system/bin/fstrim` on mount points usually needs full root and is not required here.
 *
 * Best-effort only: never prompts for permission mid-job; skips quietly if Shizuku is down
 * or permission was not granted yet.
 */
object FstrimViaShizuku {
    private const val LOG_TAG = "FstrimViaShizuku"

    fun tryRunAfterSecureDelete() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        try {
            runSmFstrim()
        } catch (e: Exception) {
            Log.d(LOG_TAG, "fstrim skipped: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun runSmFstrim() {
        if (!ShizukuFileServiceLauncher.isShizukuAvailable()) {
            return
        }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Shizuku permission not granted; skip fstrim")
            return
        }
        val process = newShizukuProcess(arrayOf("sm", "fstrim")) ?: return
        try {
            // Drain streams so the remote process cannot block on a full pipe.
            val stdout = drain(process.inputStream)
            val stderr = drain(process.errorStream)
            val code = process.waitFor()
            if (code != 0) {
                Log.w(LOG_TAG, "sm fstrim exit=$code stdout=$stdout stderr=$stderr")
            } else {
                Log.i(LOG_TAG, "sm fstrim finished")
            }
        } finally {
            process.destroy()
        }
    }

    /**
     * [Shizuku.newProcess] is private in the public API; invoke via reflection (same as many
     * Shizuku clients). Prefer this over expanding the root AIDL surface for a one-shot command.
     */
    private fun newShizukuProcess(cmd: Array<String>): Process? {
        return try {
            val method: Method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            method.invoke(null, cmd, null, null) as Process
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Shizuku.newProcess unavailable: ${e.message}")
            null
        }
    }

    private fun drain(stream: java.io.InputStream): String =
        BufferedReader(InputStreamReader(stream)).use { it.readText().trim() }
}
