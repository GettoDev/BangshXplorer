/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.navigation

import android.os.Looper
import androidx.lifecycle.MediatorLiveData
import java8.nio.file.Path
import get.gettodev.com.util.valueCompat

object NavigationRootMapLiveData : MediatorLiveData<Map<Path, NavigationRoot>>() {
    @Volatile
    private var currentRootMap: Map<Path, NavigationRoot>? = null

    override fun getValue(): Map<Path, NavigationRoot>? = currentRootMap ?: super.getValue()

    init {
        // Initialize value before we have any active observer.
        loadValue()
        if (Looper.myLooper() == Looper.getMainLooper()) {
            addSource(NavigationItemListLiveData) { loadValue() }
        }
    }

    private fun loadValue() {
        val rootMap = NavigationItemListLiveData.valueCompat
            .mapNotNull { it as? NavigationRoot }
            .associateBy { it.path }
        currentRootMap = rootMap
        if (Looper.myLooper() == Looper.getMainLooper()) {
            value = rootMap
        } else {
            postValue(rootMap)
        }
    }
}
