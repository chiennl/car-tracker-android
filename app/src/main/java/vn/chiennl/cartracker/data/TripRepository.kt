package vn.chiennl.cartracker.data

import android.location.Location
import kotlinx.coroutines.flow.Flow

class TripRepository(private val dao: TripDao) {
    fun observeTrips(): Flow<List<TripEntity>> = dao.observeTrips()

    suspend fun beginTrip(): Long = dao.insertTrip(
        TripEntity(startedAt = System.currentTimeMillis())
    )

    suspend fun finishTrip(tripId: Long) {
        dao.completeTrip(tripId, System.currentTimeMillis())
    }

    suspend fun appendLocation(tripId: Long, location: Location): Double {
        val previous = dao.lastPoint(tripId)
        val delta = if (previous == null) {
            0.0
        } else {
            val result = FloatArray(1)
            Location.distanceBetween(
                previous.latitude,
                previous.longitude,
                location.latitude,
                location.longitude,
                result
            )
            result[0].toDouble()
        }

        dao.insertPoint(
            LocationPointEntity(
                tripId = tripId,
                recordedAt = location.time.takeIf { it > 0 } ?: System.currentTimeMillis(),
                latitude = location.latitude,
                longitude = location.longitude,
                speedMps = if (location.hasSpeed()) location.speed else 0f,
                accuracyMeters = if (location.hasAccuracy()) location.accuracy else 0f
            )
        )
        if (delta > 0) dao.addDistance(tripId, delta)
        return delta
    }

    suspend fun latestTrip(): TripEntity? = dao.latestTrip()
    suspend fun tripById(tripId: Long): TripEntity? = dao.tripById(tripId)
    suspend fun pointsForTrip(tripId: Long): List<LocationPointEntity> = dao.pointsForTrip(tripId)
    suspend fun markSynced(tripId: Long) = dao.markSynced(tripId, System.currentTimeMillis())
}
