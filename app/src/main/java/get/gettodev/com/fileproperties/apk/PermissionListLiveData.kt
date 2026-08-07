/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.fileproperties.apk

import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import get.gettodev.com.app.packageManager
import get.gettodev.com.util.Failure
import get.gettodev.com.util.Loading
import get.gettodev.com.util.Stateful
import get.gettodev.com.util.Success
import get.gettodev.com.util.getPermissionInfoOrNull
import get.gettodev.com.util.valueCompat

class PermissionListLiveData(
    private val permissionNames: Array<String>
) : MutableLiveData<Stateful<List<PermissionItem>>>() {
    init {
        loadValue()
    }

    private fun loadValue() {
        value = Loading(value?.value)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val value = try {
                val permissions = permissionNames.map { name ->
                    val packageManager = packageManager
                    val permissionInfo = packageManager.getPermissionInfoOrNull(name, 0)
                    val label = permissionInfo?.loadLabel(packageManager)?.toString()
                        .takeIf { it != name }
                    val description = permissionInfo?.loadDescription(packageManager)?.toString()
                    PermissionItem(name, permissionInfo, label, description)
                }
                Success(permissions)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }
}
