package vn.chiennl.cartracker

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.chiennl.cartracker.auth.GoogleAuthClient
import vn.chiennl.cartracker.data.TripEntity
import vn.chiennl.cartracker.settings.SettingsRepository
import vn.chiennl.cartracker.tracking.LocationTrackingService
import vn.chiennl.cartracker.tracking.TrackingSnapshot
import vn.chiennl.cartracker.tracking.TrackingState

data class DashboardState(
    val tracking: TrackingSnapshot = TrackingSnapshot(),
    val trips: List<TripEntity> = emptyList(),
    val autoStart: Boolean = false,
    val signedInName: String? = null,
    val firebaseConfigured: Boolean = false,
    val message: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as CarTrackerApplication
    private val settings = SettingsRepository(application)
    private val auth = GoogleAuthClient(application)
    private val message = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    val state: StateFlow<DashboardState> = combine(
        TrackingState.snapshot,
        app.tripRepository.observeTrips(),
        settings.autoStart,
        message
    ) { tracking, trips, autoStart, currentMessage ->
        DashboardState(
            tracking = tracking,
            trips = trips,
            autoStart = autoStart,
            signedInName = auth.currentUser()?.displayName ?: auth.currentUser()?.email,
            firebaseConfigured = auth.isConfigured(),
            message = currentMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    fun startTracking() {
        val context = getApplication<Application>()
        ContextCompat.startForegroundService(
            context,
            Intent(context, LocationTrackingService::class.java)
                .setAction(LocationTrackingService.ACTION_START)
        )
    }

    fun stopTracking() {
        val context = getApplication<Application>()
        context.startService(
            Intent(context, LocationTrackingService::class.java)
                .setAction(LocationTrackingService.ACTION_STOP)
        )
    }

    fun setAutoStart(enabled: Boolean) {
        settings.setAutoStart(enabled)
        message.value = if (enabled) {
            "Đã bật tự khởi động. Cần cấp quyền vị trí nền để tự ghi sau khi bật thiết bị."
        } else {
            "Đã tắt tự khởi động theo dõi."
        }
    }

    fun signIn(activity: Activity) {
        viewModelScope.launch {
            val result = auth.signIn(activity)
            message.value = result.fold(
                onSuccess = { "Đăng nhập thành công: ${it.displayName ?: it.email}" },
                onFailure = { it.message ?: "Không thể đăng nhập Google." }
            )
        }
    }

    fun signOut() {
        auth.signOut()
        message.value = "Đã đăng xuất."
    }

    fun syncLatestTrip() {
        viewModelScope.launch {
            message.value = app.syncRepository.syncLatestTrip().fold(
                onSuccess = { "Đã đồng bộ hành trình gần nhất lên Firebase Firestore." },
                onFailure = { it.message ?: "Đồng bộ không thành công." }
            )
        }
    }

    fun clearMessage() {
        message.value = null
    }
}
