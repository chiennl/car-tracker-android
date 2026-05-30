package vn.chiennl.cartracker.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import vn.chiennl.cartracker.MainActivity
import vn.chiennl.cartracker.R

object NotificationHelper {
    const val TRACKING_CHANNEL_ID = "tracking"
    const val RESUME_CHANNEL_ID = "resume_tracking"
    const val TRACKING_NOTIFICATION_ID = 1101
    const val RESUME_NOTIFICATION_ID = 1102

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                TRACKING_CHANNEL_ID,
                context.getString(R.string.tracking_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = context.getString(R.string.tracking_channel_description) }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                RESUME_CHANNEL_ID,
                context.getString(R.string.resume_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.resume_channel_description) }
        )
    }

    fun trackingNotification(context: Context, distanceMeters: Double, speedKmh: Float): Notification {
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stop = PendingIntent.getService(
            context,
            1,
            Intent(context, LocationTrackingService::class.java).setAction(LocationTrackingService.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, TRACKING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("CarTracker đang ghi hành trình")
            .setContentText(
                "Quãng đường: %.2f km • Tốc độ: %.1f km/h".format(distanceMeters / 1000, speedKmh)
            )
            .setContentIntent(openApp)
            .setOngoing(true)
            .addAction(0, "Dừng", stop)
            .setOnlyAlertOnce(true)
            .build()
    }

    fun resumeTrackingNotification(context: Context): Notification {
        val openApp = PendingIntent.getActivity(
            context,
            2,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, RESUME_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("CarTracker sẵn sàng tiếp tục")
            .setContentText("Mở ứng dụng để tiếp tục theo dõi vị trí sau khi khởi động thiết bị.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
    }
}
