package com.carlauncher.ui

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.carlauncher.R
import com.carlauncher.data.AppsRepository

/**
 * Simple single-choice app selector dialog used for dock pinning and
 * nav/music tile binding.
 */
object AppPickerDialog {

    fun show(context: Context, title: String, onPicked: (String) -> Unit) {
        val apps = AppsRepository(context.applicationContext).installedApps()
        if (apps.isEmpty()) {
            Toast.makeText(context, R.string.no_apps, Toast.LENGTH_SHORT).show()
            return
        }
        val labels = apps.map { it.label }.toTypedArray()
        AlertDialog.Builder(context)
            .setTitle(title)
            .setItems(labels) { _, which -> onPicked(apps[which].packageName) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
