package com.carlauncher.radio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Skeleton for the real radio integration on Allwinner / NWD (K2401P) head units.
 *
 * These units expose the tuner through vendor-specific mechanisms that vary
 * per firmware build. Common mechanisms found on such units:
 *
 *  1. Broadcast intents - actions containing "radio" / "fm" / "band", carrying
 *     the current frequency in kHz (and RDS PS/RT text) in the extras.
 *  2. A vendor service - some firmwares ship a hidden service
 *     (e.g. com.syu.radio.* / com.woits.radio.*) that accepts commands.
 *  3. Serial / CAN commands - the MCU owns the tuner; the Android side talks
 *     to the MCU over the serial port or a vendor socket.
 *
 * To wire this class up for your unit:
 *  - Discover the mechanism on your firmware (logcat while the stock radio
 *    app changes stations is a good starting point).
 *  - Fill in the TODO sections below.
 *  - Flip `useVendorRadio` to true (app preferences).
 */
class VendorRadioTuner : RadioTuner {

    private val _state = MutableStateFlow(RadioState())
    override val state: StateFlow<RadioState> = _state

    override fun stepUp() = sendStep(+1)
    override fun stepDown() = sendStep(-1)
    override fun seekUp() = sendSeek(+1)
    override fun seekDown() = sendSeek(-1)

    override fun tuneTo(frequencyKHz: Int) {
        // TODO: send the exact tune command to the tuner
        // (broadcast intent / vendor service call / serial command).
    }

    override fun setBand(band: RadioBand) {
        // TODO: send the band switch command to the tuner.
    }

    override fun start() {
        // TODO: register broadcast receivers / bind the vendor service and
        // push incoming updates into _state, e.g.:
        //   _state.value = RadioState(
        //       band = ...,
        //       frequencyKHz = ...,
        //       stationName = rdsPs,
        //       radioText = rdsRt,
        //       signalStrength = ...,
        //   )
    }

    override fun stop() {
        // TODO: unregister receivers / unbind the vendor service.
    }

    private fun sendStep(direction: Int) {
        // TODO: send a step command to the tuner.
    }

    private fun sendSeek(direction: Int) {
        // TODO: send a seek command to the tuner.
    }
}
