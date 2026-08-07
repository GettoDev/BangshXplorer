/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * Copyright (c) 2026 GettoDev
 * All Rights Reserved.
 */

package get.gettodev.com.about

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import get.gettodev.com.databinding.AboutFragmentBinding
import get.gettodev.com.ui.LicensesDialogFragment
import get.gettodev.com.util.createViewIntent
import get.gettodev.com.util.startActivitySafe

class AboutFragment : Fragment() {
    private lateinit var binding: AboutFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        AboutFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.authorGitHubLayout.setOnClickListener {
            startActivitySafe(AUTHOR_GITHUB_URI.createViewIntent())
        }
        binding.authorWebsiteLayout.setOnClickListener {
            startActivitySafe(AUTHOR_WEBSITE_URI.createViewIntent())
        }
        binding.originalAuthorNameLayout.setOnClickListener {
            startActivitySafe(ORIGINAL_AUTHOR_RESUME_URI.createViewIntent())
        }
        binding.originalAuthorGitHubLayout.setOnClickListener {
            startActivitySafe(ORIGINAL_AUTHOR_GITHUB_URI.createViewIntent())
        }
        binding.originalAuthorTwitterLayout.setOnClickListener {
            startActivitySafe(ORIGINAL_AUTHOR_TWITTER_URI.createViewIntent())
        }
    }

    companion object {
        private val AUTHOR_GITHUB_URI = Uri.parse("https://github.com/GettoDev")
        private val AUTHOR_WEBSITE_URI = Uri.parse("https://gettodev.github.io/")
        private val ORIGINAL_AUTHOR_RESUME_URI = Uri.parse("https://resume.zhanghai.me/")
        private val ORIGINAL_AUTHOR_GITHUB_URI = Uri.parse("https://github.com/zhanghai")
        private val ORIGINAL_AUTHOR_TWITTER_URI = Uri.parse("https://twitter.com/zhanghai95")
    }
}
