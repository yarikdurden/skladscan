package com.skladscan.warehouse.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun byId(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun byBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products")
    suspend fun all(): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Query("UPDATE products SET quantity = :quantity, updatedAt = :updatedAt, version = version + 1 WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Int, updatedAt: Long)

    @Query("DELETE FROM products")
    suspend fun clear()

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface OperationDao {
    @Query("SELECT * FROM operations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations WHERE productId = :productId ORDER BY createdAt DESC LIMIT 30")
    fun observeForProduct(productId: Long): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations WHERE syncStatus IN ('PENDING', 'FAILED') ORDER BY createdAt ASC")
    suspend fun pending(): List<OperationEntity>

    @Query("SELECT COUNT(*) FROM operations WHERE syncStatus IN ('PENDING', 'FAILED')")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM operations WHERE syncStatus IN ('PENDING', 'FAILED')")
    suspend fun pendingCount(): Int

    @Query("SELECT COUNT(*) FROM operations WHERE createdAt >= :since")
    fun observeCountSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM operations WHERE createdAt >= :since")
    suspend fun countSince(since: Long): Int

    @Query("SELECT * FROM operations WHERE createdAt >= :since ORDER BY createdAt ASC")
    suspend fun since(since: Long): List<OperationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: OperationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(operations: List<OperationEntity>)

    @Query("UPDATE operations SET syncStatus = :status, error = :error WHERE clientOpId = :clientOpId")
    suspend fun markSync(clientOpId: String, status: String, error: String? = null)

    @Query("UPDATE operations SET syncStatus = 'SYNCED', error = NULL WHERE syncStatus IN ('PENDING', 'FAILED')")
    suspend fun markAllSynced()

    @Query("SELECT COUNT(*) FROM operations WHERE barcode = :barcode AND syncStatus IN ('PENDING', 'FAILED')")
    suspend fun pendingCountForBarcode(barcode: String): Int

    @Query("DELETE FROM operations")
    suspend fun clear()
}
