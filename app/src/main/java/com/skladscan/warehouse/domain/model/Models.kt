package com.skladscan.warehouse.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

enum class OpType(val code: String, val label: String, val action: String) {
    RECEIVE("RECEIVE", "Приход", "принято"),
    WRITE_OFF("WRITE_OFF", "Списание", "списано"),
    INVENTORY("INVENTORY", "Инвентаризация", "пересчитано");

    companion object {
        fun fromCode(code: String): OpType = entries.firstOrNull { it.code == code } ?: RECEIVE
    }
}

enum class SyncStatus { PENDING, SYNCING, SYNCED, FAILED }

data class Product(
    val id: Long,
    val barcode: String,
    val name: String,
    val sku: String,
    val category: String,
    val unit: String,
    val quantity: Int,
    val minQuantity: Int,
    val location: String,
    val price: Double,
    val updatedAt: Long,
    val version: Int,
) {
    val isOutOfStock: Boolean get() = quantity <= 0
    val isLowStock: Boolean get() = !isOutOfStock && quantity <= minQuantity
    val stockRatio: Float get() = if (minQuantity <= 0) 1f else (quantity.toFloat() / (minQuantity * 2f)).coerceIn(0f, 1f)
}

data class StockOperation(
    val id: Long,
    val clientOpId: String,
    val productId: Long,
    val productName: String,
    val barcode: String,
    val type: OpType,
    val quantityDelta: Int,
    val quantityBefore: Int,
    val quantityAfter: Int,
    val note: String,
    val createdAt: Long,
    val syncStatus: SyncStatus,
    val error: String? = null,
) {
    val isPending: Boolean get() = syncStatus == SyncStatus.PENDING || syncStatus == SyncStatus.FAILED
    val signedQuantity: String get() = when (type) {
        OpType.INVENTORY -> "= ${quantityAfter}"
        OpType.RECEIVE -> "+${abs(quantityDelta)}"
        OpType.WRITE_OFF -> "−${abs(quantityDelta)}"
    }
}

data class Settings(
    val serverUrl: String = "https://demo.skladscan.app",
    val deviceName: String = "Кладовщик-01",
    val warehouseName: String = "Основной склад",
    val demoMode: Boolean = true,
    val continuousScan: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val theme: String = "dark",
    val lastSyncAt: Long = 0L,
)

data class StockSummary(
    val totalSkus: Int = 0,
    val totalUnits: Int = 0,
    val lowStock: Int = 0,
    val outOfStock: Int = 0,
    val inventoryValue: Double = 0.0,
    val todayOperations: Int = 0,
    val pendingOperations: Int = 0,
    val lastSyncAt: Long = 0L,
)

data class DayBar(val label: String, val dateStart: Long, val incoming: Int, val outgoing: Int)

enum class StockFilter(val label: String) {
    ALL("Все"),
    LOW("Мало"),
    OUT("Нет в наличии"),
    PENDING("Не синхр."),
}

object StockMath {
    fun deltaFor(type: OpType, inputQty: Int, currentQty: Int): Int = when (type) {
        OpType.RECEIVE -> inputQty
        OpType.WRITE_OFF -> -inputQty
        OpType.INVENTORY -> inputQty - currentQty
    }

    fun resultQuantity(currentQty: Int, delta: Int): Int = max(0, currentQty + delta)

    fun validate(type: OpType, inputQty: Int, currentQty: Int): String? = when {
        inputQty < 0 -> "Количество не может быть отрицательным"
        type == OpType.RECEIVE && inputQty == 0 -> "Введите количество больше нуля"
        type == OpType.WRITE_OFF && inputQty == 0 -> "Введите количество больше нуля"
        type == OpType.WRITE_OFF && inputQty > currentQty -> "На складе только $currentQty"
        else -> null
    }

    fun inventoryValue(quantity: Int, price: Double): Double = quantity * price
}

object Formatters {
    private val time = SimpleDateFormat("HH:mm", Locale("ru"))
    private val day = SimpleDateFormat("d MMMM", Locale("ru"))
    private val dayShort = SimpleDateFormat("d MMM", Locale("ru"))
    private val weekday = SimpleDateFormat("EEEE", Locale("ru"))
    private val money = java.text.NumberFormat.getNumberInstance(Locale("ru")).apply {
        maximumFractionDigits = 0
    }
    private val money2 = java.text.NumberFormat.getNumberInstance(Locale("ru")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun time(ts: Long): String = time.format(Date(ts))
    fun day(ts: Long): String = day.format(Date(ts)).replaceFirstChar { it.uppercase() }
    fun dayShort(ts: Long): String = dayShort.format(Date(ts))
    fun weekday(ts: Long): String = weekday.format(Date(ts)).replaceFirstChar { it.uppercase() }
    fun money(value: Double): String = "₽ ${money.format(value)}"
    fun price(value: Double): String = if (value % 1.0 == 0.0) "₽ ${money.format(value)}" else "₽ ${money2.format(value)}"
    fun int(value: Int): String = money.format(value.toLong())
    fun barcode(value: String): String = value.chunked(4).joinToString(" ")
}
