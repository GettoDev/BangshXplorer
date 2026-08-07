/*
 * Copyright (c) 2026 GettoDev
 * All Rights Reserved.
 */

package get.gettodev.com.filelist

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import get.gettodev.com.R
import get.gettodev.com.util.ParcelableArgs
import get.gettodev.com.util.args
import get.gettodev.com.util.getQuantityString
import get.gettodev.com.util.putArgs
import get.gettodev.com.util.show

class ConfirmDeleteBnilliosFilesDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val files = args.files
        val message = if (files.size == 1) {
            val file = files.single()
            val messageRes = if (file.attributesNoFollowLinks.isDirectory) {
                R.string.file_delete_bnillios_message_directory_format
            } else {
                R.string.file_delete_bnillios_message_file_format
            }
            getString(messageRes, file.name)
        } else {
            val allDirectories = files.all { it.attributesNoFollowLinks.isDirectory }
            val allFiles = files.none { it.attributesNoFollowLinks.isDirectory }
            val messageRes = when {
                allDirectories -> R.plurals.file_delete_bnillios_message_multiple_directories_format
                allFiles -> R.plurals.file_delete_bnillios_message_multiple_files_format
                else -> R.plurals.file_delete_bnillios_message_multiple_mixed_format
            }
            getQuantityString(messageRes, files.size, files.size)
        }
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.delete_bnillios)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> listener.deleteBnilliosFiles(files) }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    companion object {
        fun show(files: FileItemSet, fragment: Fragment) {
            ConfirmDeleteBnilliosFilesDialogFragment().putArgs(Args(files)).show(fragment)
        }
    }

    @Parcelize
    class Args(val files: FileItemSet) : ParcelableArgs

    interface Listener {
        fun deleteBnilliosFiles(files: FileItemSet)
    }
}
