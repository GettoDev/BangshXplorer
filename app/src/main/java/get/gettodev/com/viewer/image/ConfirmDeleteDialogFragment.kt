/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.viewer.image

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import get.gettodev.com.R
import get.gettodev.com.util.ParcelableArgs
import get.gettodev.com.util.ParcelableParceler
import get.gettodev.com.util.args
import get.gettodev.com.util.putArgs
import get.gettodev.com.util.show

class ConfirmDeleteDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setMessage(getString(R.string.image_viewer_delete_message_format, args.path.fileName))
            .setPositiveButton(android.R.string.ok) { _, _ -> listener.delete(args.path) }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    companion object {
        fun show(path: Path, fragment: Fragment) {
            ConfirmDeleteDialogFragment().putArgs(Args(path)).show(fragment)
        }
    }

    @Parcelize
    class Args(val path: @WriteWith<ParcelableParceler> Path) : ParcelableArgs

    interface Listener {
        fun delete(path: Path)
    }
}
