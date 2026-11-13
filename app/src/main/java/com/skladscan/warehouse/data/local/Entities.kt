package com.skladscan.warehouse.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"], unique = true), Index(value = ["name"])],
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    val version: Int = 1,
)

@Entity(
    tableName = "operations",
    indices = [Index("productId"), Index("createdAt"), Index("syncStatus"), Index(value = ["clientOpId"], unique = true)],
)
data class OperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientOpId: String,
    val productId: Long,
    val productName: String,
    val barcode: String,
    val type: String,
    val quantityDelta: Int,
    val quantityBefore: Int,
    val quantityAfter: Int,
    val note: String,
    val createdAt: Long,
    val syncStatus: String,
    val error: String? = null,
)
