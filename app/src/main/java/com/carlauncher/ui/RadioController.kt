package com.carlauncher.ui

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.carlauncher.R
import com.carlauncher.data.AppPrefs
import com.carlauncher.databinding.ViewRadioWidgetBinding
import com.carlauncher.radio.RadioAppLauncher
import com.carlauncher.radio.RadioBand
import com.carlauncher.radio.RadioState
import com.carlauncher.radio.RadioTuner
import java.util.Locale

/**
 * [2] Live FM/AM radio widget.
 *  - Tap [−]/[+] to step; long-press to seek.
 *  - Tap a preset chip to recall; long-press to save the current frequency.
 *  - Tap the frequency display to open the native radio app.
 */
class RadioController(
    private val context: Context,
    private val binding: ViewRadioWidgetBinding,
    private val tuner: RadioTuner,
    private val prefs: AppPrefs,
) {
    private val chips: List<TextView> =
        listOf(binding.preset1, binding.preset2, binding.preset3, binding.preset4)

    init {
        binding.seekDown.setOnClickListener { tuner.stepDown() }
        binding.seekUp.setOnClickListener { tuner.stepUp() }
        binding.seekDown.setOnLongClickListener { tuner.seekDown(); true }
        binding.seekUp.setOnLongClickListener { tuner.seekUp(); true }
        binding.frequencyContainer.setOnClickListener { RadioAppLauncher.launch(context) }
        chips.forEachIndexed { index, chip ->
            chip.setOnClickListener {
                prefs.radioPresets.getOrNull(index)?.let { tuner.tuneTo(it) }
            }
            chip.setOnLongClickListener {
                savePreset(index)
                true
            }
        }
    }

    fun render(state: RadioState) {
        binding.frequency.text = state.frequencyDisplay
        binding.band.text = state.bandDisplay
        binding.stationName.text = state.stationName ?: context.getString(R.string.radio_no_rds)
        binding.radioText.text = state.radioText.orEmpty()
        binding.seekIndicator.visibility = if (state.isSeeking) View.VISIBLE else View.INVISIBLE
        renderChips(state.band)
    }

    private fun renderChips(band: RadioBand) {
        val presets = prefs.radioPresets
        chips.forEachIndexed { index, chip ->
            val preset = presets.getOrNull(index)
            chip.text = preset?.let { formatFrequency(it, band) } ?: "+"
        }
    }

    private fun savePreset(slot: Int) {
        val presets = prefs.radioPresets.toMutableList()
        while (presets.size <= slot) presets.add(0)
        presets[slot] = tuner.state.value.frequencyKHz
        prefs.radioPresets = presets
        renderChips(tuner.state.value.band)
        Toast.makeText(context, R.string.radio_saved_preset, Toast.LENGTH_SHORT).show()
    }

    private fun formatFrequency(kHz: Int, band: RadioBand): String =
        if (band == RadioBand.FM) String.format(Locale.US, "%.1f", kHz / 1000f) else kHz.toString()
}
