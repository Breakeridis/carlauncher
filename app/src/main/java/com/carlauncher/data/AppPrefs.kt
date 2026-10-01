package com.carlauncher.data

import android.content.Context

/**
 * Persistent launcher preferences (radio presets, dock pins, unit and
 * theme selection, app bindings). All state survives vehicle restarts.
 */
class AppPrefs(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var pinnedApps: List<String>
        get() = prefs.getString(KEY_PINNED, "")!!.split("|").filter { it.isNotBlank() }
        set(value) = prefs.edit().putString(KEY_PINNED, value.joinToString("|")).apply()

    var radioPresets: List<Int>
        get() = prefs.getString(KEY_RADIO_PRESETS, "")!!.split("|").mapNotNull { it.toIntOrNull() }
        set(value) = prefs.edit().putString(KEY_RADIO_PRESETS, value.joinToString("|")).apply()

    var useMph: Boolean
        get() = prefs.getBoolean(KEY_USE_MPH, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_MPH, value).apply()

    var nightMode: Boolean
        get() = prefs.getBoolean(KEY_NIGHT_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_NIGHT_MODE, value).apply()

    var navApp: String?
        get() = prefs.getString(KEY_NAV_APP, null)
        set(value) = prefs.edit().putString(KEY_NAV_APP, value).apply()

    var musicApp: String?
        get() = prefs.getString(KEY_MUSIC_APP, null)
        set(value) = prefs.edit().putString(KEY_MUSIC_APP, value).apply()

    var useVendorRadio: Boolean
        get() = prefs.getBoolean(KEY_VENDOR_RADIO, false)
        set(value) = prefs.edit().putBoolean(KEY_VENDOR_RADIO, value).apply()

    companion object {
        private const val PREFS_NAME = "car_launcher_prefs"
        private const val KEY_PINNED = "pinned_apps"
        private const val KEY_RADIO_PRESETS = "radio_presets"
        private const val KEY_USE_MPH = "use_mph"
        private const val KEY_NIGHT_MODE = "night_mode"
        private const val KEY_NAV_APP = "nav_app"
        private const val KEY_MUSIC_APP = "music_app"
        private const val KEY_VENDOR_RADIO = "vendor_radio"
    }
}
