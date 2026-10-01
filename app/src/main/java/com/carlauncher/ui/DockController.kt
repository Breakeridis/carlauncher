package com.carlauncher.ui

import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.carlauncher.R
import com.carlauncher.data.AppPrefs
import com.carlauncher.data.AppsRepository
import com.carlauncher.databinding.ActivityMainBinding

/**
 * [7] Bottom-left customizable app dock + All Apps button.
 * Long-press a slot to replace/remove; the [+] slot adds a new pin.
 */
class DockController(
    private val activity: FragmentActivity,
    private val binding: ActivityMainBinding,
    private val prefs: AppPrefs,
) {
    private val repo = AppsRepository(activity)
    private val slots: List<ImageButton> =
        listOf(binding.dockSlot1, binding.dockSlot2, binding.dockSlot3, binding.dockSlot4)

    init {
        slots.forEachIndexed { index, slot ->
            slot.setOnClickListener {
                prefs.pinnedApps.getOrNull(index)?.let { repo.launchPackage(it) }
            }
            slot.setOnLongClickListener {
                showSlotMenu(index)
                true
            }
        }
        binding.dockAdd.setOnClickListener { addApp() }
        binding.dockAllApps.setOnClickListener {
            AppDrawerFragment().show(activity.supportFragmentManager, AppDrawerFragment.TAG)
        }
    }

    fun refresh() {
        val pinned = prefs.pinnedApps
        slots.forEachIndexed { index, slot ->
            val packageName = pinned.getOrNull(index)
            if (packageName == null) {
                slot.setImageDrawable(null)
                slot.visibility = View.INVISIBLE
            } else {
                slot.setImageDrawable(repo.icon(packageName))
                slot.visibility = View.VISIBLE
            }
        }
        binding.dockAdd.visibility = if (pinned.size < MAX_SLOTS) View.VISIBLE else View.GONE
    }

    private fun addApp() {
        AppPickerDialog.show(activity, activity.getString(R.string.dock_pick_app)) { packageName ->
            prefs.pinnedApps = prefs.pinnedApps + packageName
            refresh()
        }
    }

    private fun showSlotMenu(slot: Int) {
        val options = arrayOf(
            activity.getString(R.string.dock_replace),
            activity.getString(R.string.dock_remove),
        )
        AlertDialog.Builder(activity)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> AppPickerDialog.show(
                        activity,
                        activity.getString(R.string.dock_pick_app),
                    ) { packageName ->
                        val pinned = prefs.pinnedApps.toMutableList()
                        if (slot < pinned.size) pinned[slot] = packageName
                        prefs.pinnedApps = pinned
                        refresh()
                    }
                    1 -> {
                        prefs.pinnedApps = prefs.pinnedApps.filterIndexed { index, _ -> index != slot }
                        refresh()
                    }
                }
            }
            .show()
    }

    companion object {
        private const val MAX_SLOTS = 4
    }
}
