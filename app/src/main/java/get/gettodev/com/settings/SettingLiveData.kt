/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.settings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Looper
import androidx.annotation.AnyRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import get.gettodev.com.app.application
import get.gettodev.com.app.defaultSharedPreferences
import get.gettodev.com.compat.PreferenceManagerCompat

abstract class SettingLiveData<T>(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @AnyRes private val defaultValueRes: Int
) : LiveData<T>(), OnSharedPreferenceChangeListener {
    private val sharedPreferences = getSharedPreferences(nameSuffix)
    private val key = getKey(keyRes, keySuffix)
    private var defaultValue: T? = null
    private var currentValue: T? = null

    constructor(@StringRes keyRes: Int, @AnyRes defaultValueRes: Int) : this(
        null, keyRes, null, defaultValueRes
    )

    override fun getValue(): T? = currentValue ?: super.getValue()

    protected fun init() {
        defaultValue = getDefaultValue(defaultValueRes)
        loadValue()
        // Only a weak reference is stored so we don't need to worry about unregistering.
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun getSharedPreferences(nameSuffix: String?): SharedPreferences =
        if (nameSuffix == null) {
            defaultSharedPreferences
        } else {
            val name = "${PreferenceManagerCompat.getDefaultSharedPreferencesName(application)
            }_$nameSuffix"
            val mode = PreferenceManagerCompat.defaultSharedPreferencesMode
            application.getSharedPreferences(name, mode)
        }

    private fun getKey(@StringRes keyRes: Int, keySuffix: String?): String {
        val key = application.getString(keyRes)
        return if (keySuffix != null) "${key}_$keySuffix" else key
    }

    protected abstract fun getDefaultValue(@AnyRes defaultValueRes: Int): T

    private fun loadValue() {
        @Suppress("UNCHECKED_CAST")
        val newValue = getValue(sharedPreferences, key, defaultValue as T)
        currentValue = newValue
        if (Looper.myLooper() == Looper.getMainLooper()) {
            value = newValue
        } else {
            postValue(newValue)
        }
    }

    protected abstract fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: T
    ): T

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == this.key) {
            loadValue()
        }
    }

    fun putValue(value: T) {
        putValue(sharedPreferences, key, value)
    }

    protected abstract fun putValue(sharedPreferences: SharedPreferences, key: String, value: T)
}
