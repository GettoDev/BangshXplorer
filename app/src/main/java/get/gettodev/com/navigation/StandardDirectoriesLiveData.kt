/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.navigation

import android.os.Looper
import androidx.lifecycle.MediatorLiveData
import get.gettodev.com.settings.Settings

object StandardDirectoriesLiveData : MediatorLiveData<List<StandardDirectory>>() {
    @Volatile
    private var currentDirectories: List<StandardDirectory>? = null

    override fun getValue(): List<StandardDirectory>? = currentDirectories ?: super.getValue()

    init {
        // Initialize value before we have any active observer.
        loadValue()
        if (Looper.myLooper() == Looper.getMainLooper()) {
            addSource(Settings.STANDARD_DIRECTORY_SETTINGS) { loadValue() }
        }
    }

    private fun loadValue() {
        val dirs = standardDirectories
        currentDirectories = dirs
        if (Looper.myLooper() == Looper.getMainLooper()) {
            value = dirs
        } else {
            postValue(dirs)
        }
    }
}
