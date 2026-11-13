package com.skladscan.warehouse.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.domain.model.OpType
import com.skladscan.warehouse.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScanTarget {
    data class Found(val product: Product, val format: String) : ScanTarget
    data class Unknown(val barcode: String, val format: String) : ScanTarget
}

data class ScannerUiState(
    val target: ScanTarget? = null,
    val type: OpType = OpType.RECEIVE,
    val quantity: Int = 1,
    val busy: Boolean = false,
    val torch: Boolean = false,
    val snackbar: String? = null,
    val lookupBusy: Boolean = false,
)

class ScannerViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()

    fun onBarcode(value: String, format: String = "Штрихкод") {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return
        val current = _state.value
        if (current.lookupBusy || current.target != null) return
        _state.update { it.copy(lookupBusy = true) }
        viewModelScope.launch {
            val product = container.repository.findByBarcode(trimmed)
            _state.update {
                it.copy(
                    lookupBusy = false,
                    target = product?.let { p -> ScanTarget.Found(p, format) }
                        ?: ScanTarget.Unknown(trimmed, format),
                    type = OpType.RECEIVE,
                    quantity = 1,
                )
            }
        }
    }

    fun setType(type: OpType) {
        val product = (_state.value.target as? ScanTarget.Found)?.product
        val quantity = when (type) {
            OpType.INVENTORY -> product?.quantity ?: 0
            else -> 1
        }
        _state.update { it.copy(type = type, quantity = quantity) }
    }

    fun setQuantity(value: Int) {
        _state.update { it.copy(quantity = value.coerceIn(0, 99_999)) }
    }

    fun setTorch(value: Boolean) = _state.update { it.copy(torch = value) }

    fun confirm() {
        val current = _state.value
        val found = current.target as? ScanTarget.Found ?: return
        if (current.busy) return
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val result = container.repository.performOperation(
                productId = found.product.id,
                type = current.type,
                inputQuantity = current.quantity,
            )
            result.onSuccess { after ->
                _state.update {
                    it.copy(
                        target = null,
                        type = OpType.RECEIVE,
                        quantity = 1,
                        busy = false,
                        snackbar = successText(current.type, current.quantity, after, found.product),
                    )
                }
                container.syncScheduler.requestImmediateSync()
            }.onFailure { error ->
                _state.update {
                    it.copy(busy = false, snackbar = error.message ?: "Не удалось сохранить операцию")
                }
            }
        }
    }

    fun addProduct(
        name: String,
        category: String,
        price: Double,
        quantity: Int,
        location: String,
        minQuantity: Int,
        unit: String = "шт.",
    ) {
        val current = _state.value
        val unknown = current.target as? ScanTarget.Unknown ?: return
        if (current.busy) return
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val result = container.repository.createProduct(
                barcode = unknown.barcode,
                name = name,
                sku = "",
                category = category,
                unit = unit,
                minQuantity = minQuantity,
                location = location,
                price = price,
                initialQuantity = quantity,
            )
            result.onSuccess { product ->
                _state.update {
                    it.copy(
                        target = null,
                        busy = false,
                        snackbar = "«${product.name}» добавлен в каталог",
                    )
                }
                container.syncScheduler.requestImmediateSync()
            }.onFailure { error ->
                _state.update {
                    it.copy(busy = false, snackbar = error.message ?: "Не удалось добавить товар")
                }
            }
        }
    }

    fun dismiss() {
        if (_state.value.busy) return
        _state.update {
            it.copy(target = null, type = OpType.RECEIVE, quantity = 1)
        }
    }

    fun consumeSnackbar() = _state.update { it.copy(snackbar = null) }

    private fun successText(type: OpType, inputQty: Int, after: Int, product: Product): String = when (type) {
        OpType.RECEIVE -> "Принято $inputQty ${product.unit} · ${product.name}"
        OpType.WRITE_OFF -> "Списано $inputQty ${product.unit} · ${product.name}"
        OpType.INVENTORY -> "Остаток обновлён: $after ${product.unit}"
    }
}
