package vn.chiennl.cartracker.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("cartracker_preferences", Context.MODE_PRIVATE)
    private val _autoStart = MutableStateFlow(prefs.getBoolean(KEY_AUTO_START, false))
    val autoStart: StateFlow<Boolean> = _autoStart

    fun setAutoStart(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_START, enabled).apply()
        _autoStart.value = enabled
    }

    fun isAutoStartEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_START, false)

    companion object {
        private const val KEY_AUTO_START = "auto_start_tracking_after_boot"
    }
}
