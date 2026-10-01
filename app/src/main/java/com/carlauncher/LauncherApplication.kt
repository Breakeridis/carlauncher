package com.carlauncher

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.carlauncher.data.AppPrefs

class LauncherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val prefs = AppPrefs(this)
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.nightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
