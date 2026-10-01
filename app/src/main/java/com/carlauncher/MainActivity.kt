package com.carlauncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.carlauncher.data.AppPrefs
import com.carlauncher.databinding.ActivityMainBinding
import com.carlauncher.location.SpeedProvider
import com.carlauncher.radio.RadioTuner
import com.carlauncher.radio.RadioTuners
import com.carlauncher.ui.ClockController
import com.carlauncher.ui.DockController
import com.carlauncher.ui.ProjectionController
import com.carlauncher.ui.QuickLaunchController
import com.carlauncher.ui.RadioController
import com.carlauncher.ui.SystemController
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: AppPrefs
    private lateinit var tuner: RadioTuner
    private lateinit var speedProvider: SpeedProvider

    private lateinit var clockController: ClockController
    private lateinit var radioController: RadioController
    private lateinit var dockController: DockController
    private lateinit var quickLaunchController: QuickLaunchController
    private lateinit var systemController: SystemController
    private lateinit var projectionController: ProjectionController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = AppPrefs(this)

        tuner = RadioTuners.create(prefs.useVendorRadio)
        speedProvider = SpeedProvider(this)

        clockController = ClockController(
            this,
            binding.clockTime,
            binding.clockSeconds,
            binding.clockDay,
            binding.clockDate,
            binding.clockSection,
        )
        radioController = RadioController(this, binding.radioWidget, tuner, prefs)
        dockController = DockController(this, binding, prefs)
        quickLaunchController = QuickLaunchController(this, binding, prefs)
        systemController = SystemController(this, binding, prefs)
        projectionController = ProjectionController(this, binding)

        binding.speedometer.useMph = prefs.useMph
        binding.speedometer.onUnitToggle = {
            prefs.useMph = !prefs.useMph
            binding.speedometer.useMph = prefs.useMph
        }
        binding.mapPortal.onSearchClick = { showSearchDialog() }

        requestLocationPermissionIfNeeded()
        observeFlows()
    }

    private fun observeFlows() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    tuner.state.collect { radioController.render(it) }
                }
                launch {
                    speedProvider.gps.collect { gps ->
                        binding.speedometer.setSpeed(gps.speedKmh, gps.hasFix)
                        binding.mapPortal.updateGps(gps.headingDegrees, gps.hasFix, gps.satellites)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        tuner.start()
        speedProvider.start()
        clockController.start()
    }

    override fun onStop() {
        tuner.stop()
        speedProvider.stop()
        clockController.stop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        dockController.refresh()
        quickLaunchController.refresh()
        projectionController.refresh()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
    }

    private fun requestLocationPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION,
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            speedProvider.start()
        }
    }

    private fun showSearchDialog() {
        val input = EditText(this)
        input.hint = getString(R.string.search_title)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.search_title)
            .setView(input)
            .setPositiveButton(R.string.search_go) { _, _ ->
                val query = input.text.toString().trim()
                if (query.isNotEmpty()) {
                    val uri = Uri.parse("geo:0,0?q=" + Uri.encode(query))
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (_: Exception) {
                        Toast.makeText(this, R.string.search_no_app, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private const val REQUEST_LOCATION = 100
    }
}
