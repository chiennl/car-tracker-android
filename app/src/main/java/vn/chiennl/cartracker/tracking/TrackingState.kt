package vn.chiennl.cartracker.tracking

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TrackingSnapshot(
    val isTracking: Boolean = false,
    val tripId: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val speedKmh: Float = 0f,
    val distanceMeters: Double = 0.0,
    val accuracyMeters: Float? = null,
    val updatedAt: Long? = null
)

object TrackingState {
    private val _snapshot = MutableStateFlow(TrackingSnapshot())
    val snapshot: StateFlow<TrackingSnapshot> = _snapshot

    fun publish(value: TrackingSnapshot) {
        _snapshot.value = value
    }

    fun stop() {
        _snapshot.value = _snapshot.value.copy(isTracking = false, speedKmh = 0f)
    }
}
