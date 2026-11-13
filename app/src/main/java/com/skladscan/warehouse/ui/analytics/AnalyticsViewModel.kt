package com.skladscan.warehouse.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.domain.model.DayBar
import com.skladscan.warehouse.domain.model.OpType
import com.skladscan.warehouse.domain.model.Product
import com.skladscan.warehouse.domain.model.StockOperation
import com.skladscan.warehouse.ui.components.CategorySlice
import com.skladscan.warehouse.ui.theme.AmberBright
import com.skladscan.warehouse.ui.theme.CoralBright
import com.skladscan.warehouse.ui.theme.CyanBright
import com.skladscan.warehouse.ui.theme.Lime
import com.skladscan.warehouse.ui.theme.VioletBright
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AnalyticsUiState(
    val week: List<DayBar> = emptyList(),
    val categories: List<CategorySlice> = emptyList(),
    val categoryTotal: Double = 0.0,
    val lowStock: List<Product> = emptyList(),
    val recent: List<StockOperation> = emptyList(),
    val incoming: Int = 0,
    val outgoing: Int = 0,
    val todayOperations: Int = 0,
    val loading: Boolean = true,
)

class AnalyticsViewModel(container: AppContainer) : ViewModel() {

    private val palette = listOf(Lime, CyanBright, AmberBright, VioletBright, CoralBright)

    val state: StateFlow<AnalyticsUiState> = combine(
        container.repository.products,
        container.repository.operations,
        container.repository.observeWeek(),
    ) { products, operations, week ->
        val categoriesByValue = products
            .filter { it.quantity > 0 }
            .groupBy { it.category }
            .map { (category, items) ->
                category to items.sumOf { it.price * it.quantity }
            }
            .sortedByDescending { it.second }

        val top = categoriesByValue.take(4).mapIndexed { index, (category, value) ->
            CategorySlice(category, value, palette[index % palette.size])
        }
        val rest = categoriesByValue.drop(4).sumOf { it.second }
        val categories = if (rest > 0.0) {
            top + CategorySlice("Другое", rest, palette[4])
        } else {
            top
        }
        val categoryTotal = categoriesByValue.sumOf { it.second }
        val today = startOfToday()
        val lowStock = products
            .filter { it.isLowStock || it.isOutOfStock }
            .sortedWith(
                compareBy<Product> { if (it.isOutOfStock) 0 else 1 }
                    .thenBy { it.quantity.toFloat() / (it.minQuantity.coerceAtLeast(1)) }
            )
            .take(5)

        AnalyticsUiState(
            week = week,
            categories = categories,
            categoryTotal = categoryTotal,
            lowStock = lowStock,
            recent = operations.sortedByDescending { it.createdAt }.take(4),
            incoming = week.sumOf { it.incoming },
            outgoing = week.sumOf { it.outgoing },
            todayOperations = operations.count { it.createdAt >= today },
            loading = false,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AnalyticsUiState(),
    )

    private fun startOfToday(): Long = com.skladscan.warehouse.util.Dates.startOfDay(System.currentTimeMillis())
}
