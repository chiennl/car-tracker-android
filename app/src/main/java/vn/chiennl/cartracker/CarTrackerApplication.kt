package vn.chiennl.cartracker

import android.app.Application
import com.google.firebase.FirebaseApp
import vn.chiennl.cartracker.data.AppDatabase
import vn.chiennl.cartracker.data.TripRepository
import vn.chiennl.cartracker.sync.FirebaseSyncRepository
import vn.chiennl.cartracker.tracking.NotificationHelper

class CarTrackerApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val tripRepository: TripRepository by lazy { TripRepository(database.tripDao()) }
    val syncRepository: FirebaseSyncRepository by lazy { FirebaseSyncRepository(this, tripRepository) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        // Trả về null khi chưa có app/google-services.json; ứng dụng vẫn chạy ở chế độ cục bộ.
        FirebaseApp.initializeApp(this)
    }
}
