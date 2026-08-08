/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.navigation

import androidx.lifecycle.MediatorLiveData
import get.gettodev.com.settings.Settings

object StandardDirectoriesLiveData : MediatorLiveData<List<StandardDirectory>>() {
    init {
        // Initialize value before we have any active observer.
        loadValue()
        addSource(Settings.STANDARD_DIRECTORY_SETTINGS) { loadValue() }
    }

    private fun loadValue() {
        value = standardDirectories
    }
}
