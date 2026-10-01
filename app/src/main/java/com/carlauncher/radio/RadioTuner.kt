package com.carlauncher.radio

import java.util.Locale
import kotlinx.coroutines.flow.StateFlow

enum class RadioBand { FM, AM }

data class RadioState(
    val band: RadioBand = RadioBand.FM,
    val frequencyKHz: Int = 88_600,
    val stationName: String? = null,
    val radioText: String? = null,
    val isSeeking: Boolean = false,
    val signalStrength: Int = 0,
) {
    val frequencyDisplay: String
        get() = if (band == RadioBand.FM) {
            String.format(Locale.US, "%.1f", frequencyKHz / 1000f)
        } else {
            frequencyKHz.toString()
        }

    val bandDisplay: String
        get() = band.name
}

/**
 * Abstraction over the head unit's native FM/AM tuner.
 *
 * Implementations:
 *  - [StubRadioTuner] - simulated tuner, used for development/demo.
 *  - [VendorRadioTuner] - template to fill in for your specific firmware.
 */
interface RadioTuner {
    val state: StateFlow<RadioState>

    fun stepUp()
    fun stepDown()
    fun seekUp()
    fun seekDown()
    fun tuneTo(frequencyKHz: Int)
    fun setBand(band: RadioBand)

    /** Begin monitoring the tuner (register receivers, bind services). */
    fun start()

    /** Stop monitoring the tuner. */
    fun stop()
}

object RadioTuners {
    fun create(useVendorApi: Boolean): RadioTuner =
        if (useVendorApi) VendorRadioTuner() else StubRadioTuner()
}
