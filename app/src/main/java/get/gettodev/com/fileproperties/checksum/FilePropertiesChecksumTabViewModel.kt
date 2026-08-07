/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.fileproperties.checksum

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java8.nio.file.Path
import get.gettodev.com.util.Stateful

class FilePropertiesChecksumTabViewModel(path: Path) : ViewModel() {
    private val _checksumInfoLiveData = ChecksumInfoLiveData(path)
    val checksumInfoLiveData: LiveData<Stateful<ChecksumInfo>>
        get() = _checksumInfoLiveData

    fun reload() {
        _checksumInfoLiveData.loadValue()
    }

    override fun onCleared() {
        _checksumInfoLiveData.close()
    }
}
