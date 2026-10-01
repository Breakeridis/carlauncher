package com.carlauncher.ui

import android.content.Intent
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import com.carlauncher.R
import com.carlauncher.data.AppsRepository
import com.carlauncher.databinding.ActivityMainBinding

/**
 * [5] Phone projection quick-launch tile (ZLink / Android Auto / CarPlay).
 * Detects the pre-installed projection app on the head unit; tapping it
 * launches wireless/wired phone mirroring directly.
 */
class ProjectionController(
    private val activity: FragmentActivity,
    private val binding: ActivityMainBinding,
) {
    private val repo = AppsRepository(activity)

    // Package names vary per head-unit firmware; extend this list to match
    // the projection app pre-installed on your unit.
    private val candidatePackages = listOf(
        "com.zjinnova.zlink", // ZLink 5 (common on NWD K2401P units)
        "com.syu.zlink",      // ZLink variants
        "com.hik.autokit",    // AutoKit (CarPlay dongles)
        "com.speedplay",      // SpeedPlay (Android Auto)
    )

    private var detectedPackage: String? = null

    init {
        detect()
        render()
        binding.projectionTile.setOnClickListener {
            val packageName = detectedPackage
            if (packageName != null) {
                repo.launchPackage(packageName)
            } else {
                openBluetoothSettings()
            }
        }
    }

    fun refresh() {
        detect()
        render()
    }

    private fun detect() {
        detectedPackage = candidatePackages.firstOrNull { repo.isInstalled(it) }
    }

    private fun render() {
        val packageName = detectedPackage
        if (packageName != null) {
            binding.projectionStatus.text =
                repo.label(packageName) ?: activity.getString(R.string.projection_ready)
            binding.projectionSubtitle.text = activity.getString(R.string.projection_tap_connect)
        } else {
            binding.projectionStatus.text = activity.getString(R.string.projection_none)
            binding.projectionSubtitle.text = activity.getString(R.string.projection_open_bt)
        }
    }

    private fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            activity.startActivity(intent)
        } catch (_: Exception) {
            activity.startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
