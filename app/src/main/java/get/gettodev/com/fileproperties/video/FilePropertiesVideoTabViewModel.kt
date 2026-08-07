/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.fileproperties.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java8.nio.file.Path
import get.gettodev.com.util.Stateful

class FilePropertiesVideoTabViewModel(path: Path) : ViewModel() {
    private val _videoInfoLiveData = VideoInfoLiveData(path)
    val videoInfoLiveData: LiveData<Stateful<VideoInfo>>
        get() = _videoInfoLiveData

    fun reload() {
        _videoInfoLiveData.loadValue()
    }

    override fun onCleared() {
        _videoInfoLiveData.close()
    }
}
