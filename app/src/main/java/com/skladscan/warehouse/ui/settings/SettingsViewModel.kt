package com.skladscan.warehouse.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skladscan.warehouse.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ConnectionState {
    data object Idle : ConnectionState
    data object Testing : ConnectionState
    data class Success(val serverTime: Long) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val _connection = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connection: StateFlow<ConnectionState> = _connection.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun saveServerUrl(url: String) {
        if (!url.trim().startsWith("http://") && !url.trim().startsWith("https://")) {
            _message.value = "Адрес должен начинаться с http:// или https://"
            return
        }
        viewModelScope.launch {
            container.settings.setServerUrl(url)
            _message.value = "Адрес сервера сохранён"
        }
    }

    fun setDemoMode(value: Boolean) {
        viewModelScope.launch {
            container.settings.setDemoMode(value)
            _connection.value = ConnectionState.Idle
        }
    }

    fun setContinuousScan(value: Boolean) {
        viewModelScope.launch { container.settings.setContinuousScan(value) }
    }

    fun setSound(value: Boolean) {
        viewModelScope.launch { container.settings.setSound(value) }
    }

    fun setVibration(value: Boolean) {
        viewModelScope.launch { container.settings.setVibration(value) }
    }

    fun setTheme(value: String) {
        viewModelScope.launch { container.settings.setTheme(value) }
    }

    fun testConnection() {
        if (_connection.value == ConnectionState.Testing) return
        _connection.value = ConnectionState.Testing
        viewModelScope.launch {
            val result = container.syncManager.testConnection()
            _connection.value = result.fold(
                onSuccess = { ConnectionState.Success(it) },
                onFailure = { ConnectionState.Error(it.message ?: "Сервер не отвечает") },
            )
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            container.repository.resetDemoData()
            _message.value = "Демо-данные восстановлены"
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
