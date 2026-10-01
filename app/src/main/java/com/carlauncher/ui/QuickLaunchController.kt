package com.carlauncher.ui

import androidx.fragment.app.FragmentActivity
import com.carlauncher.R
import com.carlauncher.data.AppPrefs
import com.carlauncher.data.AppsRepository
import com.carlauncher.databinding.ActivityMainBinding

/**
 * [6] Navigation & music quick-launch tiles.
 * Single-tap launches the bound app; long-press rebinds any installed app.
 */
class QuickLaunchController(
    private val activity: FragmentActivity,
    private val binding: ActivityMainBinding,
    private val prefs: AppPrefs,
) {
    private val repo = AppsRepository(activity)

    init {
        binding.navTile.setOnClickListener { launchNav() }
        binding.navTile.setOnLongClickListener { pickNav(); true }
        binding.musicTile.setOnClickListener { launchMusic() }
        binding.musicTile.setOnLongClickListener { pickMusic(); true }
    }

    fun refresh() {
        binding.navLabel.text =
            prefs.navApp?.let { repo.label(it) } ?: activity.getString(R.string.nav_default)
        binding.musicLabel.text =
            prefs.musicApp?.let { repo.label(it) } ?: activity.getString(R.string.music_default)
    }

    private fun launchNav() {
        val packageName = prefs.navApp
        if (packageName == null || !repo.launchPackage(packageName)) pickNav()
    }

    private fun launchMusic() {
        val packageName = prefs.musicApp
        if (packageName == null || !repo.launchPackage(packageName)) pickMusic()
    }

    private fun pickNav() {
        AppPickerDialog.show(activity, activity.getString(R.string.nav_pick_title)) { packageName ->
            prefs.navApp = packageName
            refresh()
        }
    }

    private fun pickMusic() {
        AppPickerDialog.show(activity, activity.getString(R.string.music_pick_title)) { packageName ->
            prefs.musicApp = packageName
            refresh()
        }
    }
}
