/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.navigation

import android.os.Looper
import androidx.lifecycle.MediatorLiveData
import get.gettodev.com.settings.Settings
import get.gettodev.com.storage.StorageVolumeListLiveData

object NavigationItemListLiveData : MediatorLiveData<List<NavigationItem?>>() {
    @Volatile
    private var currentItems: List<NavigationItem?>? = null

    override fun getValue(): List<NavigationItem?>? = currentItems ?: super.getValue()

    init {
        // Initialize value before we have any active observer.
        loadValue()
        if (Looper.myLooper() == Looper.getMainLooper()) {
            addSource(Settings.STORAGES) { loadValue() }
            addSource(StorageVolumeListLiveData) { loadValue() }
            addSource(StandardDirectoriesLiveData) { loadValue() }
            addSource(Settings.BOOKMARK_DIRECTORIES) { loadValue() }
        }
    }

    private fun loadValue() {
        val items = navigationItems
        currentItems = items
        if (Looper.myLooper() == Looper.getMainLooper()) {
            value = items
        } else {
            postValue(items)
        }
    }
}
