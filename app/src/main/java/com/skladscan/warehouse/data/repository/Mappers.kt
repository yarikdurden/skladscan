package com.skladscan.warehouse.data.repository

import com.skladscan.warehouse.data.local.OperationEntity
import com.skladscan.warehouse.data.local.ProductEntity
import com.skladscan.warehouse.domain.model.OpType
import com.skladscan.warehouse.domain.model.Product
import com.skladscan.warehouse.domain.model.StockOperation
import com.skladscan.warehouse.domain.model.SyncStatus

fun ProductEntity.toDomain() = Product(
    id = id,
    barcode = barcode,
    name = name,
    sku = sku,
    category = category,
    unit = unit,
    quantity = quantity,
    minQuantity = minQuantity,
    location = location,
    price = price,
    updatedAt = updatedAt,
    version = version,
)

fun OperationEntity.toDomain() = StockOperation(
    id = id,
    clientOpId = clientOpId,
    productId = productId,
    productName = productName,
    barcode = barcode,
    type = OpType.fromCode(type),
    quantityDelta = quantityDelta,
    quantityBefore = quantityBefore,
    quantityAfter = quantityAfter,
    note = note,
    createdAt = createdAt,
    syncStatus = runCatching { SyncStatus.valueOf(syncStatus) }.getOrElse { SyncStatus.PENDING },
    error = error,
)
