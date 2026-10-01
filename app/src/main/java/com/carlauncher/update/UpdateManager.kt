package com.carlauncher.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.app.ProgressDialog
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
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val changelog: String,
)

/**
 * In-app over-the-air updater.
 *
 * Expected manifest JSON served from [UPDATE_URL]:
 * {
 *   "versionCode": 2,
 *   "versionName": "1.1.0",
 *   "apkUrl": "https://.../carlauncher-1.1.0.apk",
 *   "changelog": "..."
 * }
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
                if (info.versionCode <= BuildConfig.VERSION_CODE.toLong()) {
                    toast(R.string.update_up_to_date)
                    return@withContext
                }
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.update_available_title, info.versionName))
                    .setMessage(info.changelog)
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
        return try {
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            UpdateInfo(
                versionCode = json.getLong("versionCode"),
                versionName = json.getString("versionName"),
                apkUrl = json.getString("apkUrl"),
                changelog = json.optString("changelog"),
            )
        } finally {
            connection.disconnect()
        }
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
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        installApk(manager.getUriForDownloadedFile(id))
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
        // TODO: point this at your real update manifest endpoint.
        private const val UPDATE_URL = "https://example.com/carlauncher/update.json"
    }
}
