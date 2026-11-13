package com.skladscan.warehouse.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.skladscan.warehouse.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "skladscan_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val serverUrl = stringPreferencesKey("server_url")
        val deviceName = stringPreferencesKey("device_name")
        val warehouseName = stringPreferencesKey("warehouse_name")
        val demoMode = booleanPreferencesKey("demo_mode")
        val continuousScan = booleanPreferencesKey("continuous_scan")
        val sound = booleanPreferencesKey("sound")
        val vibration = booleanPreferencesKey("vibration")
        val theme = stringPreferencesKey("theme")
        val lastSync = longPreferencesKey("last_sync")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            serverUrl = p[Keys.serverUrl] ?: "https://demo.skladscan.app",
            deviceName = p[Keys.deviceName] ?: "Кладовщик-01",
            warehouseName = p[Keys.warehouseName] ?: "Основной склад",
            demoMode = p[Keys.demoMode] ?: true,
            continuousScan = p[Keys.continuousScan] ?: false,
            soundEnabled = p[Keys.sound] ?: true,
            vibrationEnabled = p[Keys.vibration] ?: true,
            theme = p[Keys.theme] ?: "dark",
            lastSyncAt = p[Keys.lastSync] ?: 0L,
        )
    }

    suspend fun setServerUrl(value: String) = context.dataStore.edit { it[Keys.serverUrl] = value.trim() }
    suspend fun setDeviceName(value: String) = context.dataStore.edit { it[Keys.deviceName] = value.trim() }
    suspend fun setDemoMode(value: Boolean) = context.dataStore.edit { it[Keys.demoMode] = value }
    suspend fun setContinuousScan(value: Boolean) = context.dataStore.edit { it[Keys.continuousScan] = value }
    suspend fun setSound(value: Boolean) = context.dataStore.edit { it[Keys.sound] = value }
    suspend fun setVibration(value: Boolean) = context.dataStore.edit { it[Keys.vibration] = value }
    suspend fun setTheme(value: String) = context.dataStore.edit { it[Keys.theme] = value }
    suspend fun setLastSyncAt(value: Long) = context.dataStore.edit { it[Keys.lastSync] = value }
}
