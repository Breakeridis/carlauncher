package com.carlauncher.radio

import kotlin.math.abs
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Simulated tuner used for development and for head units whose vendor
 * radio API is not wired up yet (see [VendorRadioTuner]).
 */
class StubRadioTuner : RadioTuner {

    private data class Station(
        val frequencyKHz: Int,
        val name: String?,
        val text: String?,
    )

    private val fmStations = listOf(
        Station(88_600, "RADIO 1", "The best of the 80s and 90s"),
        Station(95_200, "HIT FM", "Non-stop hits all day long"),
        Station(98_500, "CLASSIC", "Classic rock around the clock"),
        Station(103_700, "TALK", "News and talk radio"),
    )
    private val amStations = listOf(
        Station(531, "AM NEWS", "24/7 news"),
        Station(900, "AM SPORT", "Live sport coverage"),
        Station(1_440, "AM GOLD", "Golden oldies"),
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var seekJob: Job? = null

    private val _state = MutableStateFlow(RadioState())
    override val state: StateFlow<RadioState> = _state

    override fun stepUp() = step(+1)
    override fun stepDown() = step(-1)
    override fun seekUp() = seek(+1)
    override fun seekDown() = seek(-1)

    override fun setBand(band: RadioBand) {
        val stations = stationsFor(band)
        val nearest = stations.minByOrNull { abs(it.frequencyKHz - _state.value.frequencyKHz) }
        if (nearest != null) {
            tune(nearest, band)
        } else {
            _state.value = _state.value.copy(band = band)
        }
    }

    override fun tuneTo(frequencyKHz: Int) {
        tune(Station(frequencyKHz, null, null), _state.value.band)
    }

    override fun start() = Unit

    override fun stop() {
        seekJob?.cancel()
        seekJob = null
    }

    private fun stationsFor(band: RadioBand) = if (band == RadioBand.FM) fmStations else amStations

    private fun step(direction: Int) {
        val stations = stationsFor(_state.value.band)
        val index = indexOfNearest(stations, _state.value.frequencyKHz)
        tune(stations[(index + direction).mod(stations.size)], _state.value.band)
    }

    private fun seek(direction: Int) {
        seekJob?.cancel()
        _state.value = _state.value.copy(isSeeking = true)
        seekJob = scope.launch {
            delay(1_200)
            val stations = stationsFor(_state.value.band)
            val index = indexOfNearest(stations, _state.value.frequencyKHz)
            tune(stations[(index + direction).mod(stations.size)], _state.value.band)
        }
    }

    private fun tune(station: Station, band: RadioBand) {
        _state.value = RadioState(
            band = band,
            frequencyKHz = station.frequencyKHz,
            stationName = station.name,
            radioText = station.text,
            isSeeking = false,
            signalStrength = 60 + Random.nextInt(40),
        )
    }

    private fun indexOfNearest(stations: List<Station>, frequencyKHz: Int): Int {
        var best = 0
        var bestDelta = Int.MAX_VALUE
        stations.forEachIndexed { index, station ->
            val delta = abs(station.frequencyKHz - frequencyKHz)
            if (delta < bestDelta) {
                bestDelta = delta
                best = index
            }
        }
        return best
    }
}
