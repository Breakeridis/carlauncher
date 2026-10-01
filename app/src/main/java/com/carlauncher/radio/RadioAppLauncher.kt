package com.carlauncher.radio

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.carlauncher.R

/**
 * Launches the head unit's full-screen native radio application.
 * Package names vary per firmware - extend [candidatePackages] to match
 * the stock radio app on your unit.
 */
object RadioAppLauncher {

    private val candidatePackages = listOf(
        "com.syu.radio",       // common on NWD / K2401P units
        "com.android.fmradio", // stock Android radio
        "com.fmradio",         // generic
    )

    fun launch(context: Context): Boolean {
        val pm = context.packageManager
        for (packageName in candidatePackages) {
            pm.getLaunchIntentForPackage(packageName)?.let { intent ->
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return try {
                    context.startActivity(intent)
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }

        // Fallback: broadcast the widely used vendor action for opening the radio UI.
        context.sendBroadcast(Intent("com.syu.radio.action.SHOW"))

        Toast.makeText(context, R.string.radio_native_not_found, Toast.LENGTH_SHORT).show()
        return false
    }
}
