package com.carlauncher.ui

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.carlauncher.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * [1] Digital clock & calendar widget. Runs a lightweight ticker and
 * resynchronizes with the head unit system time on every tick.
 */
class ClockController(
    private val context: Context,
    private val timeText: TextView,
    private val secondsText: TextView,
    private val dayText: TextView,
    private val dateText: TextView,
    clockSection: View,
) {
    private val handler = Handler(Looper.getMainLooper())
    private val fullFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())

    private val ticker = object : Runnable {
        override fun run() {
            val now = Date()
            val parts = fullFormat.format(now).split(":")
            if (parts.size == 3) {
                timeText.text = "${parts[0]}:${parts[1]}"
                secondsText.text = ":${parts[2]}"
            }
            dayText.text = dayFormat.format(now)
            dateText.text = dateFormat.format(now)
            handler.postDelayed(this, 250L)
        }
    }

    init {
        clockSection.setOnClickListener { openAlarmApp() }
    }

    fun start() {
        handler.removeCallbacks(ticker)
        ticker.run()
    }

    fun stop() {
        handler.removeCallbacks(ticker)
    }

    private fun openAlarmApp() {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, R.string.clock_no_alarm_app, Toast.LENGTH_SHORT).show()
        }
    }
}
