package vn.chiennl.cartracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert
    suspend fun insertTrip(trip: TripEntity): Long

    @Insert
    suspend fun insertPoint(point: LocationPointEntity): Long

    @Query("SELECT * FROM trips ORDER BY startedAt DESC")
    fun observeTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips ORDER BY startedAt DESC LIMIT 1")
    suspend fun latestTrip(): TripEntity?

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun tripById(tripId: Long): TripEntity?

    @Query("SELECT * FROM location_points WHERE tripId = :tripId ORDER BY recordedAt ASC")
    suspend fun pointsForTrip(tripId: Long): List<LocationPointEntity>

    @Query("SELECT * FROM location_points WHERE tripId = :tripId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun lastPoint(tripId: Long): LocationPointEntity?

    @Query("UPDATE trips SET distanceMeters = distanceMeters + :deltaMeters WHERE id = :tripId")
    suspend fun addDistance(tripId: Long, deltaMeters: Double)

    @Query("UPDATE trips SET endedAt = :endedAt WHERE id = :tripId")
    suspend fun completeTrip(tripId: Long, endedAt: Long)

    @Query("UPDATE trips SET syncedAt = :syncedAt WHERE id = :tripId")
    suspend fun markSynced(tripId: Long, syncedAt: Long)
}
