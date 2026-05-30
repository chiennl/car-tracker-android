package vn.chiennl.cartracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                val state by viewModel.state.collectAsState()
                Dashboard(
                    state = state,
                    onStartRequested = { requestTrackingPermissionThenStart() },
                    onStop = viewModel::stopTracking,
                    onAutoStart = viewModel::setAutoStart,
                    onOpenSettings = ::openAppSettings,
                    onSignIn = { viewModel.signIn(this) },
                    onSignOut = viewModel::signOut,
                    onSync = viewModel::syncLatestTrip,
                    onDismissMessage = viewModel::clearMessage
                )
            }
        }
    }

    private fun requestTrackingPermissionThenStart() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        trackingPermissionLauncher.launch(permissions.toTypedArray())
    }

    private val trackingPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val hasLocation = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasLocation) viewModel.startTracking()
    }

    private fun openAppSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$packageName")
            )
        )
    }
}

@Composable
private fun Dashboard(
    state: DashboardState,
    onStartRequested: () -> Unit,
    onStop: () -> Unit,
    onAutoStart: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSync: () -> Unit,
    onDismissMessage: () -> Unit
) {
    Scaffold { padding ->
        Row(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1.05f)) {
                Text("CarTracker", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (state.tracking.isTracking) "ĐANG GHI HÀNH TRÌNH" else "ĐÃ DỪNG",
                    color = if (state.tracking.isTracking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(16.dp))
                MetricsCard(state)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.tracking.isTracking) {
                        Button(onClick = onStop) { Text("Dừng ghi") }
                    } else {
                        Button(onClick = onStartRequested) { Text("Bắt đầu ghi") }
                    }
                    TextButton(onClick = onOpenSettings) { Text("Quyền vị trí nền") }
                }
                Spacer(Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Tự khởi động cùng Android Box", modifier = Modifier.weight(1f))
                            Switch(checked = state.autoStart, onCheckedChange = onAutoStart)
                        }
                        Text(
                            "Trên Android 13: có thể tự chạy khi đã cấp quyền vị trí nền. " +
                                "Trên Android 14 trở lên: ứng dụng hiển thị yêu cầu tiếp tục để tuân thủ giới hạn hệ thống.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(0.95f)) {
                FirebaseCard(state, onSignIn, onSignOut, onSync)
                Spacer(Modifier.height(12.dp))
                Text("Lịch sử hành trình", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.trips.take(12), key = { it.id }) { trip -> TripRow(trip) }
                    if (state.trips.isEmpty()) {
                        item { Text("Chưa có dữ liệu hành trình.") }
                    }
                }
                state.message?.let {
                    Spacer(Modifier.height(10.dp))
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(it, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = onDismissMessage) { Text("Đóng") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricsCard(state: DashboardState) {
    val tracking = state.tracking
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("%.1f km/h".format(tracking.speedKmh), style = MaterialTheme.typography.displaySmall)
            Text("Tốc độ hiện tại")
            Spacer(Modifier.height(14.dp))
            Row {
                Metric("Quãng đường", "%.2f km".format(tracking.distanceMeters / 1000), Modifier.weight(1f))
                Metric("Độ chính xác", tracking.accuracyMeters?.let { "${it.roundToInt()} m" } ?: "--", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Tọa độ: " + if (tracking.latitude != null && tracking.longitude != null) {
                    "%.6f, %.6f".format(tracking.latitude, tracking.longitude)
                } else "--"
            )
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FirebaseCard(
    state: DashboardState,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSync: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text("Đồng bộ Google / Firebase", fontWeight = FontWeight.Bold)
            Text(
                if (state.firebaseConfigured) "Firebase đã được cấu hình." else "Chưa có google-services.json; dữ liệu đang lưu cục bộ.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            if (state.signedInName == null) {
                Button(onClick = onSignIn, enabled = state.firebaseConfigured) { Text("Đăng nhập Google") }
            } else {
                Text("Tài khoản: ${state.signedInName}")
                Row {
                    Button(onClick = onSync) { Text("Đồng bộ gần nhất") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onSignOut) { Text("Đăng xuất") }
                }
            }
        }
    }
}

@Composable
private fun TripRow(trip: vn.chiennl.cartracker.data.TripEntity) {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
    Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(formatter.format(Date(trip.startedAt)), fontWeight = FontWeight.Medium)
                Text(
                    if (trip.endedAt == null) "Đang ghi" else "Đã kết thúc",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text("%.2f km".format(trip.distanceMeters / 1000))
        }
    }
}
