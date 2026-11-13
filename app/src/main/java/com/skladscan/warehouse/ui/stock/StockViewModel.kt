package com.skladscan.warehouse.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.domain.model.DayBar
import com.skladscan.warehouse.domain.model.Product
import com.skladscan.warehouse.domain.model.StockFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StockUiState(
    val products: List<Product> = emptyList(),
    val counts: Map<StockFilter, Int> = emptyMap(),
    val loading: Boolean = true,
)

class StockViewModel(container: AppContainer) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(StockFilter.ALL)

    val queryFlow: StateFlow<String> = query
    val filterFlow: StateFlow<StockFilter> = filter

    val state: StateFlow<StockUiState> = combine(
        container.repository.products,
        container.repository.operations,
        query,
        filter,
    ) { products, operations, queryText, selectedFilter ->
        val pendingBarcodes = operations.filter { it.isPending }.map { it.barcode }.toSet()
        val counts = mapOf(
            StockFilter.ALL to products.size,
            StockFilter.LOW to products.count { it.isLowStock },
            StockFilter.OUT to products.count { it.isOutOfStock },
            StockFilter.PENDING to products.count { it.barcode in pendingBarcodes },
        )
        val normalized = queryText.trim().lowercase()
        val searched = products.filter { product ->
            normalized.isEmpty() ||
                product.name.lowercase().contains(normalized) ||
                product.sku.lowercase().contains(normalized) ||
                product.barcode.contains(normalized) ||
                product.category.lowercase().contains(normalized) ||
                product.location.lowercase().contains(normalized)
        }
        val filtered = when (selectedFilter) {
            StockFilter.ALL -> searched
            StockFilter.LOW -> searched.filter { it.isLowStock }
            StockFilter.OUT -> searched.filter { it.isOutOfStock }
            StockFilter.PENDING -> searched.filter { it.barcode in pendingBarcodes }
        }
        StockUiState(
            products = filtered,
            counts = counts,
            loading = false,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        StockUiState(),
    )

    val week: StateFlow<List<DayBar>> = container.repository.observeWeek().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    fun setQuery(value: String) {
        query.value = value
    }

    fun setFilter(value: StockFilter) {
        filter.value = value
    }
}
