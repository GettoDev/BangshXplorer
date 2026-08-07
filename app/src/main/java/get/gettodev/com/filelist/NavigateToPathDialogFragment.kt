/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.filelist

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import get.gettodev.com.R
import get.gettodev.com.util.ParcelableArgs
import get.gettodev.com.util.ParcelableParceler
import get.gettodev.com.util.args
import get.gettodev.com.util.putArgs
import get.gettodev.com.util.show

class NavigateToPathDialogFragment : PathDialogFragment() {
    private val args by args<Args>()

    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_list_navigate_to_title

    override val initialName: String?
        get() = args.path.toUserFriendlyString()

    override fun onOk(path: Path) {
        listener.navigateTo(path)
    }

    companion object {
        fun show(path: Path, fragment: Fragment) {
            NavigateToPathDialogFragment().putArgs(Args(path)).show(fragment)
        }
    }

    @Parcelize
    class Args(val path: @WriteWith<ParcelableParceler> Path) : ParcelableArgs

    interface Listener : NameDialogFragment.Listener {
        fun navigateTo(path: Path)
    }
}
