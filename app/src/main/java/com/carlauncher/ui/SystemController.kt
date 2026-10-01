package com.carlauncher.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import com.carlauncher.R
import com.carlauncher.data.AppPrefs
import com.carlauncher.databinding.ActivityMainBinding
import com.carlauncher.update.UpdateManager

/**
 * [8] System controls & cloud updater dock:
 * day/night toggle, factory settings shortcut, OTA updater.
 */
class SystemController(
    private val activity: FragmentActivity,
    private val binding: ActivityMainBinding,
    private val prefs: AppPrefs,
) {
    private val updateManager = UpdateManager(activity)

    init {
        binding.btnTheme.setOnClickListener { toggleTheme() }
        binding.btnSettings.setOnClickListener { openSettings() }
        binding.btnUpdate.setOnClickListener { updateManager.showUpdateDialog() }
    }

    private fun toggleTheme() {
        prefs.nightMode = !prefs.nightMode
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.nightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            activity.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(activity, R.string.settings_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
