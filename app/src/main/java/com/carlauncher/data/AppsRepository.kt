package com.carlauncher.data

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable

data class AppEntry(
    val label: String,
    val packageName: String,
    val icon: Drawable,
)

/**
 * Enumerates and launches installed applications using the standard
 * launcher intent resolution.
 */
class AppsRepository(private val context: Context) {

    fun installedApps(): List<AppEntry> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, 0)
        return resolved
            .map { it.activityInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != context.packageName }
            .map { info ->
                AppEntry(
                    label = info.loadLabel(pm).toString(),
                    packageName = info.packageName,
                    icon = info.loadIcon(pm),
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun launchPackage(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun label(packageName: String): String? = try {
        val pm = context.packageManager
        pm.getApplicationInfo(packageName, 0).loadLabel(pm).toString()
    } catch (_: Exception) {
        null
    }

    fun icon(packageName: String): Drawable? = try {
        val pm = context.packageManager
        pm.getApplicationInfo(packageName, 0).loadIcon(pm)
    } catch (_: Exception) {
        null
    }

    fun isInstalled(packageName: String): Boolean = try {
        context.packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (_: Exception) {
        false
    }
}
