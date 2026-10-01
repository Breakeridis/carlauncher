package com.carlauncher.update

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.carlauncher.BuildConfig
import com.carlauncher.R
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class UpdateInfo(
    val versionName: String,
    val apkUrl: String,
    val changelog: String,
)

/**
 * In-app over-the-air updater.
 *
 * The update source is this repository's GitHub "latest release": every
 * `vX.Y` tag pushed to the repo produces a release whose attached APK is
 * checked here. No extra hosting is required.
 */
class UpdateManager(private val context: Context) {

    private val appContext = context.applicationContext

    fun showUpdateDialog() {
        val progress = ProgressDialog(context)
        progress.setMessage(context.getString(R.string.update_checking))
        progress.setCancelable(false)
        progress.show()

        CoroutineScope(Dispatchers.IO).launch {
            val info = try {
                checkForUpdate()
            } catch (_: Exception) {
                null
            }
            withContext(Dispatchers.Main) {
                progress.dismiss()
                if (info == null) {
                    toast(R.string.update_check_failed)
                    return@withContext
                }
                if (!isNewerVersion(info.versionName, BuildConfig.VERSION_NAME)) {
                    toast(R.string.update_up_to_date)
                    return@withContext
                }
                val message = if (info.changelog.isBlank()) {
                    context.getString(R.string.update_available_title, info.versionName)
                } else {
                    info.changelog
                }
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.update_available_title, info.versionName))
                    .setMessage(message)
                    .setPositiveButton(R.string.update_download) { _, _ -> download(info) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
    }

    private fun checkForUpdate(): UpdateInfo {
        val connection = URL(UPDATE_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        return try {
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val assets = json.getJSONArray("assets")
            var apkUrl: String? = null
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                if (asset.optString("name").endsWith(".apk")) {
                    apkUrl = asset.getString("browser_download_url")
                    break
                }
            }
            UpdateInfo(
                versionName = json.getString("tag_name").trimStart('v'),
                apkUrl = apkUrl ?: throw IllegalStateException("No APK asset in latest release"),
                changelog = json.optString("body"),
            )
        } finally {
            connection.disconnect()
        }
    }

    /** Numeric dotted-version comparison (0.2 > 0.1.9 > 0.1 > 0.0.1). */
    private fun isNewerVersion(candidate: String, current: String): Boolean {
        val a = candidate.split(".").map { it.toIntOrNull() ?: 0 }
        val b = current.split(".").map { it.toIntOrNull() ?: 0 }
        val n = maxOf(a.size, b.size)
        for (i in 0 until n) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    private fun download(info: UpdateInfo) {
        val fileName = "carlauncher-v${info.versionName}.apk"
        val request = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle(context.getString(R.string.app_name) + " " + context.getString(R.string.updates_label))
            .setDescription(context.getString(R.string.update_downloading))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        val manager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)

        appContext.registerReceiver(
            downloadCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
        )
        pendingDownloadId = downloadId
        toast(R.string.update_downloading)
    }

    private var pendingDownloadId: Long = -1L

    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id != pendingDownloadId) return
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(id)
            manager.query(query).use { cursor ->
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> installApk(manager.getUriForDownloadedFile(id))
                        DownloadManager.STATUS_FAILED -> toast(R.string.update_download_failed)
                    }
                }
            }
            try {
                context.unregisterReceiver(this)
            } catch (_: Exception) {
                // Already unregistered.
            }
        }
    }

    private fun installApk(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            toast(R.string.settings_failed)
        }
    }

    private fun toast(resId: Int) {
        Toast.makeText(appContext, resId, Toast.LENGTH_SHORT).show()
    }

    companion object {
        // The updater uses this repository's "latest release" as the update
        // source. Update the owner/repo if the project moves.
        private const val UPDATE_URL =
            "https://api.github.com/repos/Breakeridis/carlauncher/releases/latest"
    }
}
