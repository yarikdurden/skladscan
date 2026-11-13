package com.skladscan.warehouse.data.local

import com.skladscan.warehouse.domain.model.OpType
import com.skladscan.warehouse.domain.model.SyncStatus
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

/**
 * Демонстрационный каталог: реалистичные позиции склада и история операций.
 * Баркоды — валидные EAN-13, их можно распечатать и сканировать камерой.
 */
object DemoData {

    data class SeedProduct(
        val barcode: String,
        val name: String,
        val sku: String,
        val category: String,
        val unit: String,
        val quantity: Int,
        val minQuantity: Int,
        val location: String,
        val price: Double,
    )

    val products = listOf(
        SeedProduct("4601000012343", "Сканер штрихкодов Zebra DS2208", "ZBR-DS2208", "Оборудование", "шт.", 14, 5, "A-01-1", 18900.0),
        SeedProduct("4601000112715", "Принтер этикеток Xprinter XP-460B", "XPR-460B", "Оборудование", "шт.", 7, 3, "A-01-2", 24900.0),
        SeedProduct("4601000213085", "Этикет-лента 58×40 мм, 1000 шт", "LBL-5840", "Расходники", "рул.", 42, 20, "B-02-1", 590.0),
        SeedProduct("4601000313457", "Картридж Brother TN-2275", "BRT-TN2275", "Расходники", "шт.", 3, 6, "C-03-2", 3290.0),
        SeedProduct("4601000413829", "Перчатки нитриловые, размер M", "GLV-NTR-M", "Спецодежда", "упак.", 68, 30, "D-01-4", 690.0),
        SeedProduct("4601000514199", "Короб картонный 400×300×200", "BOX-4030", "Упаковка", "шт.", 120, 40, "E-02-3", 85.0),
        SeedProduct("4601000614561", "Скотч упаковочный 48 мм × 150 м", "SCT-48150", "Упаковка", "шт.", 9, 12, "E-02-4", 140.0),
        SeedProduct("4601000714933", "Стеллаж металлический 2000×1000×400", "RCK-2010", "Оборудование", "шт.", 5, 2, "F-01-1", 15900.0),
        SeedProduct("4601000815302", "Поддон EUR 1200×800", "PLT-EUR", "Упаковка", "шт.", 26, 10, "F-02-2", 1450.0),
        SeedProduct("4601000915675", "Маркер перманентный чёрный", "MRK-BLK", "Канцелярия", "шт.", 57, 25, "G-03-1", 95.0),
        SeedProduct("4601001016043", "Пломба номерная 300 мм", "PLB-300", "Расходники", "шт.", 0, 50, "H-01-5", 32.0),
        SeedProduct("4601001116415", "Wi-Fi точка доступа TP-Link EAP225", "TPL-EAP225", "Оборудование", "шт.", 4, 3, "A-02-2", 7900.0),
        SeedProduct("4601001216788", "Аккумуляторы AA 2500 мА·ч, 4 шт", "BAT-AA4", "Расходники", "упак.", 33, 15, "C-04-1", 1290.0),
        SeedProduct("4601001317157", "Стрейч-плёнка 500 мм, 2 кг", "STR-500", "Упаковка", "рул.", 18, 10, "E-03-1", 420.0),
        SeedProduct("4601001417529", "Термобумага 80 мм × 80 м", "THR-8080", "Расходники", "рул.", 24, 15, "B-01-3", 210.0),
        SeedProduct("4601001517892", "Тележка гидравлическая 2.5 т", "TRL-25", "Оборудование", "шт.", 2, 1, "F-03-1", 27900.0),
        SeedProduct("4601001618261", "Ящик пластиковый 600×400×220", "BIN-6040", "Упаковка", "шт.", 47, 20, "D-02-2", 760.0),
        SeedProduct("4601001718633", "Клей-карандаш 15 г", "GLU-15", "Канцелярия", "шт.", 11, 20, "G-01-2", 75.0),
        SeedProduct("4601001819002", "Плёнка воздушно-пузырчатая 1.2 × 50 м", "BUB-1250", "Упаковка", "рул.", 6, 4, "E-01-2", 980.0),
        SeedProduct("4601001919375", "Чековая лента 57 мм × 12 м", "RCP-5712", "Расходники", "рул.", 8, 10, "B-03-1", 60.0),
    )

    private data class SeedOp(
        val productIndex: Int,
        val type: OpType,
        val qty: Int,
        val hoursAgo: Double,
        val note: String,
        val pending: Boolean = false,
    )

    private val operations = listOf(
        SeedOp(4, OpType.RECEIVE, 30, 1.5, "Поставка № 4821"),
        SeedOp(0, OpType.WRITE_OFF, 2, 2.2, "Выдача в цех № 2"),
        SeedOp(18, OpType.WRITE_OFF, 3, 3.1, "Списание: повреждение упаковки", pending = true),
        SeedOp(5, OpType.RECEIVE, 40, 4.0, "Поставка № 4822"),
        SeedOp(9, OpType.INVENTORY, 57, 4.6, "Плановый пересчёт"),
        SeedOp(8, OpType.RECEIVE, 10, 5.3, "Возврат от клиента", pending = true),
        SeedOp(14, OpType.WRITE_OFF, 4, 6.4, "Расход на смену"),
        SeedOp(3, OpType.WRITE_OFF, 1, 7.2, "Замена картриджа"),
        SeedOp(17, OpType.INVENTORY, 11, 8.0, "Плановый пересчёт", pending = true),
        SeedOp(6, OpType.RECEIVE, 6, 9.1, "Поставка № 4823"),
        SeedOp(12, OpType.RECEIVE, 12, 25.0, "Поставка № 4818"),
        SeedOp(10, OpType.WRITE_OFF, 5, 27.0, "Списание: брак"),
        SeedOp(1, OpType.RECEIVE, 2, 29.0, "Дозаказ оборудования"),
        SeedOp(15, OpType.WRITE_OFF, 1, 31.0, "Отгрузка № 778"),
        SeedOp(7, OpType.RECEIVE, 3, 33.0, "Поставка № 4816"),
        SeedOp(13, OpType.RECEIVE, 8, 49.0, "Поставка № 4812"),
        SeedOp(16, OpType.RECEIVE, 20, 52.0, "Поставка № 4811"),
        SeedOp(2, OpType.WRITE_OFF, 12, 55.0, "Расход на упаковку"),
        SeedOp(19, OpType.RECEIVE, 10, 58.0, "Поставка № 4810"),
        SeedOp(11, OpType.INVENTORY, 4, 73.0, "Плановый пересчёт"),
        SeedOp(3, OpType.RECEIVE, 4, 76.0, "Поставка № 4806"),
        SeedOp(18, OpType.RECEIVE, 15, 79.0, "Поставка № 4805"),
        SeedOp(4, OpType.WRITE_OFF, 10, 97.0, "Выдача в цех № 1"),
        SeedOp(5, OpType.WRITE_OFF, 25, 100.0, "Отгрузка № 771"),
        SeedOp(8, OpType.WRITE_OFF, 4, 121.0, "Отгрузка № 768"),
        SeedOp(6, OpType.RECEIVE, 15, 124.0, "Поставка № 4799"),
        SeedOp(14, OpType.RECEIVE, 6, 145.0, "Поставка № 4795"),
        SeedOp(13, OpType.WRITE_OFF, 5, 148.0, "Расход на упаковку"),
        SeedOp(17, OpType.RECEIVE, 12, 160.0, "Поставка № 4791"),
        SeedOp(0, OpType.RECEIVE, 4, 164.0, "Поставка № 4790"),
    )

    /**
     * Заполняет пустую базу. Возвращает true, если данные были добавлены.
     */
    suspend fun seed(productsDao: ProductDao, operationsDao: OperationDao, now: Long = System.currentTimeMillis()): Boolean {
        if (productsDao.count() > 0) return false

        val ids = mutableListOf<Long>()
        products.forEach { p ->
            ids += productsDao.insert(
                ProductEntity(
                    barcode = p.barcode,
                    name = p.name,
                    sku = p.sku,
                    category = p.category,
                    unit = p.unit,
                    quantity = p.quantity,
                    minQuantity = p.minQuantity,
                    location = p.location,
                    price = p.price,
                    updatedAt = now - (2 * 3_600_000L),
                    version = 3,
                )
            )
        }

        operations.forEachIndexed { index, op ->
            val product = products[op.productIndex]
            val id = ids[op.productIndex]
            val delta = when (op.type) {
                OpType.RECEIVE -> op.qty
                OpType.WRITE_OFF -> -op.qty
                OpType.INVENTORY -> 0
            }
            val before = when (op.type) {
                OpType.RECEIVE -> max(0, product.quantity - op.qty)
                OpType.WRITE_OFF -> product.quantity + op.qty
                OpType.INVENTORY -> product.quantity + 2
            }
            val after = when (op.type) {
                OpType.RECEIVE -> before + op.qty
                OpType.WRITE_OFF -> before - op.qty
                OpType.INVENTORY -> product.quantity
            }
            operationsDao.insert(
                OperationEntity(
                    clientOpId = "demo-${UUID.randomUUID()}",
                    productId = id,
                    productName = product.name,
                    barcode = product.barcode,
                    type = op.type.code,
                    quantityDelta = if (op.type == OpType.INVENTORY) after - before else delta,
                    quantityBefore = before,
                    quantityAfter = after,
                    note = op.note,
                    createdAt = now - (op.hoursAgo * 3_600_000L).toLong(),
                    syncStatus = if (op.pending) SyncStatus.PENDING.name else SyncStatus.SYNCED.name,
                    error = null,
                )
            )
        }
        return true
    }
}
