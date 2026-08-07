/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.storage

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Looper
import android.os.storage.StorageVolume
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import get.gettodev.com.app.application
import get.gettodev.com.app.storageManager
import get.gettodev.com.compat.registerReceiverCompat
import get.gettodev.com.compat.storageVolumesCompat

object StorageVolumeListLiveData : LiveData<List<StorageVolume>>() {
    // Backing field so we can read the value immediately even from background threads
    // without going through LiveData's main-thread-only getValue().
    @Volatile
    private var currentVolumes: List<StorageVolume>? = null

    override fun getValue(): List<StorageVolume>? = currentVolumes ?: super.getValue()

    init {
        loadValue()
        try {
            application.registerReceiverCompat(
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        loadValue()
                    }
                }, IntentFilter().apply {
                    // @see android.os.storage.VolumeInfo#sEnvironmentToBroadcast
                    addAction(Intent.ACTION_MEDIA_UNMOUNTED)
                    addAction(Intent.ACTION_MEDIA_CHECKING)
                    addAction(Intent.ACTION_MEDIA_MOUNTED)
                    addAction(Intent.ACTION_MEDIA_EJECT)
                    addAction(Intent.ACTION_MEDIA_UNMOUNTABLE)
                    addAction(Intent.ACTION_MEDIA_REMOVED)
                    addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
                    // The "file" data scheme is required to receive these broadcasts.
                    // @see https://stackoverflow.com/a/7143298
                    addDataScheme(ContentResolver.SCHEME_FILE)
                }, ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (e: Exception) {
            // In the Shizuku :sui subprocess there may be no valid main Looper
            // to register receivers — silently ignore.
        }
    }

    private fun loadValue() {
        val volumes = storageManager.storageVolumesCompat
        currentVolumes = volumes
        if (Looper.myLooper() == Looper.getMainLooper()) {
            value = volumes
        } else {
            postValue(volumes)
        }
    }
}
