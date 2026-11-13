package com.skladscan.warehouse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.domain.model.Settings
import com.skladscan.warehouse.domain.model.StockSummary
import com.skladscan.warehouse.sync.SyncUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val container: AppContainer) : ViewModel() {

    val settings: StateFlow<Settings> = container.settings.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        Settings(),
    )

    val summary: StateFlow<StockSummary> = container.repository.summary.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        StockSummary(),
    )

    val online: StateFlow<Boolean> = container.networkMonitor.online.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        container.networkMonitor.isOnline(),
    )

    val syncState: StateFlow<SyncUiState> = container.syncManager.state

    fun syncNow() {
        viewModelScope.launch { container.syncManager.sync() }
    }

    fun clearSyncMessage() = container.syncManager.clearMessage()
}

data class AppUiState(
    val settings: Settings = Settings(),
    val summary: StockSummary = StockSummary(),
    val online: Boolean = true,
    val syncing: Boolean = false,
    val syncMessage: String? = null,
    val syncError: String? = null,
)

inline fun <reified VM : ViewModel> simpleViewModelFactory(crossinline create: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
    }
