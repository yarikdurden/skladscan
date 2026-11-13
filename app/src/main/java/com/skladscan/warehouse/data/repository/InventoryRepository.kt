package com.skladscan.warehouse.data.repository

import androidx.room.withTransaction
import com.skladscan.warehouse.data.local.AppDatabase
import com.skladscan.warehouse.data.local.DemoData
import com.skladscan.warehouse.data.local.OperationDao
import com.skladscan.warehouse.data.local.OperationEntity
import com.skladscan.warehouse.data.local.ProductDao
import com.skladscan.warehouse.data.local.ProductEntity
import com.skladscan.warehouse.data.preferences.SettingsRepository
import com.skladscan.warehouse.data.remote.ProductDto
import com.skladscan.warehouse.domain.model.DayBar
import com.skladscan.warehouse.domain.model.OpType
import com.skladscan.warehouse.domain.model.Product
import com.skladscan.warehouse.domain.model.StockMath
import com.skladscan.warehouse.domain.model.StockOperation
import com.skladscan.warehouse.domain.model.StockSummary
import com.skladscan.warehouse.domain.model.SyncStatus
import com.skladscan.warehouse.util.Dates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.math.max

class InventoryRepository(
    private val db: AppDatabase,
    private val productDao: ProductDao,
    private val operationDao: OperationDao,
    private val settings: SettingsRepository,
) {

    val products: Flow<List<Product>> = productDao.observeAll().map { list -> list.map { it.toDomain() } }

    val operations: Flow<List<StockOperation>> =
        operationDao.observeAll().map { list -> list.map { it.toDomain() } }

    val pendingCount: Flow<Int> = operationDao.observePendingCount()

    val summary: Flow<StockSummary> = combine(products, operations, settings.settings) { products, operations, settings ->
        val today = Dates.startOfDay(System.currentTimeMillis())
        StockSummary(
            totalSkus = products.size,
            totalUnits = products.sumOf { it.quantity },
            lowStock = products.count { it.isLowStock },
            outOfStock = products.count { it.isOutOfStock },
            inventoryValue = products.sumOf { StockMath.inventoryValue(it.quantity, it.price) },
            todayOperations = operations.count { it.createdAt >= today },
            pendingOperations = operations.count { it.isPending },
            lastSyncAt = settings.lastSyncAt,
        )
    }

    fun observeProduct(id: Long): Flow<Product?> = productDao.observeById(id).map { it?.toDomain() }

    fun observeProductOperations(id: Long): Flow<List<StockOperation>> =
        operationDao.observeForProduct(id).map { list -> list.map { it.toDomain() } }

    fun observeRecentOperations(limit: Int = 6): Flow<List<StockOperation>> =
        operationDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    fun observeWeek(now: Long = System.currentTimeMillis()): Flow<List<DayBar>> =
        operations.map { list -> buildWeek(list, now) }

    suspend fun seedDemoIfEmpty(): Boolean = db.withTransaction {
        DemoData.seed(productDao, operationDao)
    }

    suspend fun findByBarcode(barcode: String): Product? =
        productDao.byBarcode(barcode.trim())?.toDomain()

    suspend fun productById(id: Long): Product? = productDao.byId(id)?.toDomain()

    suspend fun performOperation(
        productId: Long,
        type: OpType,
        inputQuantity: Int,
        note: String = "",
    ): Result<Int> {
        val product = productDao.byId(productId)
            ?: return Result.failure(IllegalArgumentException("Товар не найден"))
        StockMath.validate(type, inputQuantity, product.quantity)?.let {
            return Result.failure(IllegalArgumentException(it))
        }
        return runCatching {
            db.withTransaction {
                val fresh = productDao.byId(productId)
                    ?: error("Товар не найден")
                val delta = StockMath.deltaFor(type, inputQuantity, fresh.quantity)
                val after = StockMath.resultQuantity(fresh.quantity, delta)
                val now = System.currentTimeMillis()
                productDao.updateQuantity(productId, after, now)
                operationDao.insert(
                    OperationEntity(
                        clientOpId = UUID.randomUUID().toString(),
                        productId = productId,
                        productName = fresh.name,
                        barcode = fresh.barcode,
                        type = type.code,
                        quantityDelta = after - fresh.quantity,
                        quantityBefore = fresh.quantity,
                        quantityAfter = after,
                        note = note.ifBlank { defaultNote(type) },
                        createdAt = now,
                        syncStatus = SyncStatus.PENDING.name,
                    )
                )
                after
            }
        }
    }

    suspend fun createProduct(
        barcode: String,
        name: String,
        sku: String,
        category: String,
        unit: String,
        minQuantity: Int,
        location: String,
        price: Double,
        initialQuantity: Int,
    ): Result<Product> {
        if (barcode.isBlank()) return Result.failure(IllegalArgumentException("Пустой штрихкод"))
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Введите название"))
        if (productDao.byBarcode(barcode) != null) {
            return Result.failure(IllegalArgumentException("Такой штрихкод уже есть в каталоге"))
        }
        return runCatching {
            db.withTransaction {
                val now = System.currentTimeMillis()
                val id = productDao.insert(
                    ProductEntity(
                        barcode = barcode.trim(),
                        name = name.trim(),
                        sku = sku.ifBlank { generateSku(name) },
                        category = category.ifBlank { "Без категории" },
                        unit = unit.ifBlank { "шт." },
                        quantity = initialQuantity,
                        minQuantity = max(0, minQuantity),
                        location = location.ifBlank { "—" },
                        price = price,
                        updatedAt = now,
                        version = 1,
                    )
                )
                if (initialQuantity > 0) {
                    operationDao.insert(
                        OperationEntity(
                            clientOpId = UUID.randomUUID().toString(),
                            productId = id,
                            productName = name.trim(),
                            barcode = barcode.trim(),
                            type = OpType.RECEIVE.code,
                            quantityDelta = initialQuantity,
                            quantityBefore = 0,
                            quantityAfter = initialQuantity,
                            note = "Первичный ввод в каталог",
                            createdAt = now,
                            syncStatus = SyncStatus.PENDING.name,
                        )
                    )
                }
                productDao.byId(id)!!.toDomain()
            }
        }
    }

    suspend fun pendingOperations(): List<StockOperation> =
        operationDao.pending().map { it.toDomain() }

    suspend fun markSynced(clientOpIds: Collection<String>) {
        clientOpIds.forEach { operationDao.markSync(it, SyncStatus.SYNCED.name, null) }
    }

    suspend fun markFailed(clientOpIds: Collection<String>, reason: String) {
        clientOpIds.forEach { operationDao.markSync(it, SyncStatus.FAILED.name, reason) }
    }

    suspend fun markAllPendingSynced() = operationDao.markAllSynced()

    suspend fun applyRemoteProducts(remote: List<ProductDto>) = db.withTransaction {
        remote.forEach { dto ->
            val local = productDao.byBarcode(dto.barcode)
            if (local == null) {
                productDao.upsert(
                    ProductEntity(
                        barcode = dto.barcode,
                        name = dto.name,
                        sku = dto.sku,
                        category = dto.category,
                        unit = dto.unit,
                        quantity = dto.quantity,
                        minQuantity = dto.minQuantity,
                        location = dto.location,
                        price = dto.price,
                        updatedAt = dto.updatedAt,
                        version = dto.version,
                    )
                )
            } else {
                val hasPending = operationDao.pendingCountForBarcode(dto.barcode) > 0
                productDao.update(
                    local.copy(
                        name = dto.name,
                        sku = dto.sku,
                        category = dto.category,
                        unit = dto.unit,
                        quantity = if (hasPending) local.quantity else dto.quantity,
                        minQuantity = dto.minQuantity,
                        location = dto.location,
                        price = dto.price,
                        updatedAt = if (hasPending) local.updatedAt else dto.updatedAt,
                        version = max(local.version, dto.version),
                    )
                )
            }
        }
    }

    suspend fun resetDemoData() {
        db.withTransaction {
            operationDao.clear()
            productDao.clear()
            DemoData.seed(productDao, operationDao)
        }
    }

    private fun defaultNote(type: OpType): String = when (type) {
        OpType.RECEIVE -> "Приёмка на склад"
        OpType.WRITE_OFF -> "Списание со склада"
        OpType.INVENTORY -> "Пересчёт остатка"
    }

    private fun generateSku(name: String): String {
        val letters = name.filter { it.isLetterOrDigit() }.take(3).uppercase().ifBlank { "SKU" }
        return "$letters-${(1000..9999).random()}"
    }

    private fun buildWeek(operations: List<StockOperation>, now: Long): List<DayBar> {
        val starts = Dates.lastNDayStarts(now, 7)
        return starts.map { start ->
            val end = start + 86_400_000L
            val dayOps = operations.filter { it.createdAt in start until end }
            DayBar(
                label = Dates.weekdayShort(start),
                dateStart = start,
                incoming = dayOps.filter { it.type == OpType.RECEIVE }.sumOf { max(0, it.quantityDelta) },
                outgoing = dayOps.filter { it.type == OpType.WRITE_OFF }.sumOf { max(0, -it.quantityDelta) },
            )
        }
    }
}
