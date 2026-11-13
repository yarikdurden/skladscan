package com.skladscan.warehouse.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.domain.model.Formatters
import com.skladscan.warehouse.domain.model.OpType
import com.skladscan.warehouse.domain.model.StockOperation
import com.skladscan.warehouse.util.Dates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class HistoryFilter(val label: String) {
    ALL("Все"),
    RECEIVE("Приход"),
    WRITE_OFF("Списание"),
    INVENTORY("Пересчёт"),
}

sealed interface HistoryEntry {
    data class Header(val dateStart: Long, val title: String, val count: Int) : HistoryEntry
    data class Item(val operation: StockOperation) : HistoryEntry
}

data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val counts: Map<HistoryFilter, Int> = emptyMap(),
    val loading: Boolean = true,
)

class HistoryViewModel(container: AppContainer) : ViewModel() {

    val filter = MutableStateFlow(HistoryFilter.ALL)

    val state: StateFlow<HistoryUiState> = combine(
        container.repository.operations,
        filter,
    ) { operations, selectedFilter ->
        val counts = mapOf(
            HistoryFilter.ALL to operations.size,
            HistoryFilter.RECEIVE to operations.count { it.type == OpType.RECEIVE },
            HistoryFilter.WRITE_OFF to operations.count { it.type == OpType.WRITE_OFF },
            HistoryFilter.INVENTORY to operations.count { it.type == OpType.INVENTORY },
        )
        val filtered = when (selectedFilter) {
            HistoryFilter.ALL -> operations
            HistoryFilter.RECEIVE -> operations.filter { it.type == OpType.RECEIVE }
            HistoryFilter.WRITE_OFF -> operations.filter { it.type == OpType.WRITE_OFF }
            HistoryFilter.INVENTORY -> operations.filter { it.type == OpType.INVENTORY }
        }
        val today = Dates.startOfDay(System.currentTimeMillis())
        val yesterday = today - 86_400_000L
        val grouped = filtered.groupBy { Dates.startOfDay(it.createdAt) }
            .toSortedMap(compareByDescending { it })

        val entries = buildList {
            grouped.forEach { (dayStart, dayOperations) ->
                val title = when (dayStart) {
                    today -> "Сегодня"
                    yesterday -> "Вчера"
                    else -> Formatters.day(dayStart)
                }
                add(HistoryEntry.Header(dayStart, title, dayOperations.size))
                dayOperations.sortedByDescending { it.createdAt }.forEach { add(HistoryEntry.Item(it)) }
            }
        }
        HistoryUiState(entries = entries, counts = counts, loading = false)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HistoryUiState(),
    )

    fun setFilter(value: HistoryFilter) {
        filter.value = value
    }
}
