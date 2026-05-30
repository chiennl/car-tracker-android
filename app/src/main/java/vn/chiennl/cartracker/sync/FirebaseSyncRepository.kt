package vn.chiennl.cartracker.sync

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import vn.chiennl.cartracker.data.TripRepository

class FirebaseSyncRepository(
    private val context: Context,
    private val repository: TripRepository
) {
    suspend fun syncLatestTrip(): Result<Unit> = runCatching {
        check(FirebaseApp.getApps(context).isNotEmpty()) {
            "Chưa cấu hình Firebase."
        }
        val user = requireNotNull(FirebaseAuth.getInstance().currentUser) {
            "Cần đăng nhập Google trước khi đồng bộ."
        }
        val trip = requireNotNull(repository.latestTrip()) {
            "Chưa có hành trình để đồng bộ."
        }
        val points = repository.pointsForTrip(trip.id)
        val tripRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("trips")
            .document(trip.id.toString())

        tripRef.set(
            mapOf(
                "startedAt" to trip.startedAt,
                "endedAt" to trip.endedAt,
                "distanceMeters" to trip.distanceMeters,
                "pointCount" to points.size,
                "syncedAt" to System.currentTimeMillis()
            )
        ).await()

        points.chunked(450).forEach { chunk ->
            val batch = FirebaseFirestore.getInstance().batch()
            chunk.forEach { point ->
                val pointRef = tripRef.collection("points").document(point.id.toString())
                batch.set(
                    pointRef,
                    mapOf(
                        "recordedAt" to point.recordedAt,
                        "latitude" to point.latitude,
                        "longitude" to point.longitude,
                        "speedMps" to point.speedMps,
                        "accuracyMeters" to point.accuracyMeters
                    )
                )
            }
            batch.commit().await()
        }
        repository.markSynced(trip.id)
    }
}
