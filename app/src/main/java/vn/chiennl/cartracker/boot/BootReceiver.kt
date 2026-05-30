package vn.chiennl.cartracker.boot

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ContextCompat
import androidx.core.app.NotificationManagerCompat
import vn.chiennl.cartracker.settings.SettingsRepository
import vn.chiennl.cartracker.tracking.LocationTrackingService
import vn.chiennl.cartracker.tracking.NotificationHelper

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED)) return
        if (!SettingsRepository(context).isAutoStartEnabled()) return

        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasBackgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        /*
         * Android 14+ kiểm tra chặt quyền vị trí "while-in-use" khi dịch vụ được
         * khởi chạy từ nền. Thay vì gây lỗi hệ thống, bản đầu yêu cầu người dùng
         * mở ứng dụng để tiếp tục. Android Box chạy Android 13 vẫn tự khởi động.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            !hasFineLocation || !hasBackgroundLocation
        ) {
            NotificationHelper.createChannels(context)
            NotificationManagerCompat.from(context).notify(
                NotificationHelper.RESUME_NOTIFICATION_ID,
                NotificationHelper.resumeTrackingNotification(context)
            )
            return
        }

        ContextCompat.startForegroundService(
            context,
            Intent(context, LocationTrackingService::class.java)
                .setAction(LocationTrackingService.ACTION_START)
        )
    }
}
