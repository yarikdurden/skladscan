package com.skladscan.warehouse.sync

import com.skladscan.warehouse.data.preferences.SettingsRepository
import com.skladscan.warehouse.data.remote.RemoteDataSource
import com.skladscan.warehouse.data.remote.RemoteOperationDto
import com.skladscan.warehouse.data.repository.InventoryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

data class SyncUiState(
    val syncing: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

class SyncManager(
    private val repository: InventoryRepository,
    private val settings: SettingsRepository,
    private val remote: RemoteDataSource,
) {
    private val _state = MutableStateFlow(SyncUiState())
    val state: StateFlow<SyncUiState> = _state.asStateFlow()

    suspend fun sync(): Boolean {
        if (_state.value.syncing) return false
        _state.value = SyncUiState(syncing = true)
        val current = settings.settings.first()
        return try {
            if (current.demoMode) {
                delay(950)
                val count = repository.pendingOperations().size
                repository.markAllPendingSynced()
                settings.setLastSyncAt(System.currentTimeMillis())
                _state.value = SyncUiState(
                    message = if (count > 0) {
                        "Синхронизировано операций: $count"
                    } else {
                        "Все данные уже актуальны"
                    }
                )
            } else {
                val pending = repository.pendingOperations()
                if (pending.isNotEmpty()) {
                    val response = remote.pushOperations(
                        baseUrl = current.serverUrl,
                        operations = pending.map { op ->
                            RemoteOperationDto(
                                clientOpId = op.clientOpId,
                                barcode = op.barcode,
                                type = op.type.code,
                                quantityDelta = op.quantityDelta,
                                quantityAfter = op.quantityAfter,
                                note = op.note,
                                createdAt = op.createdAt,
                                deviceId = current.deviceName,
                            )
                        },
                        deviceId = current.deviceName,
                    )
                    if (response.accepted.isNotEmpty()) {
                        repository.markSynced(response.accepted)
                    }
                    if (response.rejected.isNotEmpty()) {
                        repository.markFailed(
                            response.rejected.map { it.clientOpId },
                            response.rejected.joinToString("; ") { it.reason },
                        )
                    }
                    if (response.accepted.isEmpty() && response.rejected.isEmpty()) {
                        repository.markSynced(pending.map { it.clientOpId })
                    }
                }
                val products = remote.fetchProducts(current.serverUrl)
                if (products.isNotEmpty()) repository.applyRemoteProducts(products)
                settings.setLastSyncAt(System.currentTimeMillis())
                _state.value = SyncUiState(
                    message = if (pending.isEmpty()) "Каталог обновлён" else "Отправлено операций: ${pending.size}"
                )
            }
            true
        } catch (error: Exception) {
            _state.value = SyncUiState(
                error = "Нет связи с сервером. Операции сохранены и уйдут позже."
            )
            false
        }
    }

    suspend fun testConnection(): Result<Long> = runCatching {
        val current = settings.settings.first()
        if (current.demoMode) {
            delay(450)
            System.currentTimeMillis()
        } else {
            remote.health(current.serverUrl)
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, error = null)
    }
}
