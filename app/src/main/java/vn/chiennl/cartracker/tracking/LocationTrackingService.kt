package vn.chiennl.cartracker.tracking

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import vn.chiennl.cartracker.CarTrackerApplication

class LocationTrackingService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var repository: vn.chiennl.cartracker.data.TripRepository
    private var tripId: Long? = null
    private var distanceMeters = 0.0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            scope.launch { saveLocation(location) }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = (application as CarTrackerApplication).tripRepository
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        NotificationHelper.createChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: ACTION_START) {
            ACTION_STOP -> stopTracking()
            ACTION_START -> startTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        if (tripId != null) return
        if (!hasLocationPermission()) {
            stopSelf()
            return
        }

        startForeground(
            NotificationHelper.TRACKING_NOTIFICATION_ID,
            NotificationHelper.trackingNotification(this, 0.0, 0f)
        )

        scope.launch {
            tripId = repository.beginTrip()
            TrackingState.publish(
                TrackingSnapshot(isTracking = true, tripId = tripId)
            )
            requestUpdates()
        }
    }

    private fun requestUpdates() {
        if (!hasLocationPermission()) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateDistanceMeters(5f)
            .setMinUpdateIntervalMillis(2_000L)
            .build()

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, mainLooper)
        } catch (_: SecurityException) {
            stopTracking()
        }
    }

    private suspend fun saveLocation(location: Location) {
        val activeTripId = tripId ?: return
        distanceMeters += repository.appendLocation(activeTripId, location)
        val speedKmh = location.speed.coerceAtLeast(0f) * 3.6f
        val snapshot = TrackingSnapshot(
            isTracking = true,
            tripId = activeTripId,
            latitude = location.latitude,
            longitude = location.longitude,
            speedKmh = speedKmh,
            distanceMeters = distanceMeters,
            accuracyMeters = location.accuracy,
            updatedAt = System.currentTimeMillis()
        )
        TrackingState.publish(snapshot)

        NotificationManagerCompat.from(this).notify(
            NotificationHelper.TRACKING_NOTIFICATION_ID,
            NotificationHelper.trackingNotification(this, distanceMeters, speedKmh)
        )
    }

    private fun stopTracking() {
        fusedClient.removeLocationUpdates(locationCallback)
        val activeTripId = tripId
        if (activeTripId != null) {
            scope.launch { repository.finishTrip(activeTripId) }
        }
        tripId = null
        TrackingState.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun hasLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        fusedClient.removeLocationUpdates(locationCallback)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "vn.chiennl.cartracker.START_TRACKING"
        const val ACTION_STOP = "vn.chiennl.cartracker.STOP_TRACKING"
    }
}
