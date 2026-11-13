package com.skladscan.warehouse.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.domain.model.OpType
import com.skladscan.warehouse.domain.model.Product
import com.skladscan.warehouse.domain.model.StockOperation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    private val container: AppContainer,
    private val productId: Long,
) : ViewModel() {

    val product: StateFlow<Product?> = container.repository.observeProduct(productId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    val operations: StateFlow<List<StockOperation>> = container.repository
        .observeProductOperations(productId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun perform(type: OpType, quantity: Int) {
        if (_busy.value) return
        _busy.value = true
        viewModelScope.launch {
            val result = container.repository.performOperation(productId, type, quantity)
            _busy.value = false
            result.onSuccess { after ->
                _message.value = when (type) {
                    OpType.RECEIVE -> "Принято $quantity шт. Остаток: $after"
                    OpType.WRITE_OFF -> "Списано $quantity шт. Остаток: $after"
                    OpType.INVENTORY -> "Остаток обновлён: $after"
                }
                container.syncScheduler.requestImmediateSync()
            }.onFailure {
                _message.value = it.message ?: "Не удалось выполнить операцию"
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
