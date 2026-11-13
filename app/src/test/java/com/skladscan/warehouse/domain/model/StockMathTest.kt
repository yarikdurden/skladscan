package com.skladscan.warehouse.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Бизнес-правила складского учёта: изменение остатка, валидация и стоимость запасов.
 * Эти правила одинаково применяются и в UI, и при синхронизации с сервером.
 */
class StockMathTest {

    @Test
    fun `receive increases stock`() {
        val delta = StockMath.deltaFor(OpType.RECEIVE, inputQty = 5, currentQty = 10)
        assertEquals(5, delta)
        assertEquals(15, StockMath.resultQuantity(10, delta))
    }

    @Test
    fun `write off decreases stock`() {
        val delta = StockMath.deltaFor(OpType.WRITE_OFF, inputQty = 4, currentQty = 10)
        assertEquals(-4, delta)
        assertEquals(6, StockMath.resultQuantity(10, delta))
    }

    @Test
    fun `inventory stores absolute quantity`() {
        val delta = StockMath.deltaFor(OpType.INVENTORY, inputQty = 7, currentQty = 10)
        assertEquals(-3, delta)
        assertEquals(7, StockMath.resultQuantity(10, delta))
    }

    @Test
    fun `stock never goes below zero`() {
        assertEquals(0, StockMath.resultQuantity(2, -10))
    }

    @Test
    fun `negative quantity is rejected`() {
        assertEquals(
            "Количество не может быть отрицательным",
            StockMath.validate(OpType.RECEIVE, -1, 10),
        )
    }

    @Test
    fun `zero receive is rejected`() {
        assertNotNull(StockMath.validate(OpType.RECEIVE, 0, 10))
    }

    @Test
    fun `write off more than available is rejected`() {
        assertEquals("На складе только 10", StockMath.validate(OpType.WRITE_OFF, 11, 10))
    }

    @Test
    fun `valid operation passes validation`() {
        assertNull(StockMath.validate(OpType.WRITE_OFF, 10, 10))
        assertNull(StockMath.validate(OpType.INVENTORY, 0, 10))
    }

    @Test
    fun `inventory value multiplies quantity by price`() {
        assertEquals(3870.0, StockMath.inventoryValue(3, 1290.0), 0.001)
    }

    @Test
    fun `product stock flags follow minimum threshold`() {
        val base = Product(
            id = 1,
            barcode = "4601000012343",
            name = "Сканер",
            sku = "SKU-1",
            category = "Оборудование",
            unit = "шт.",
            quantity = 4,
            minQuantity = 5,
            location = "A-01-1",
            price = 100.0,
            updatedAt = 0,
            version = 1,
        )
        assertTrue(base.isLowStock)
        assertFalse(base.isOutOfStock)
        assertTrue(base.copy(quantity = 0).isOutOfStock)
        assertFalse(base.copy(quantity = 20).isLowStock)
    }

    @Test
    fun `operation sign follows type`() {
        fun op(type: OpType, delta: Int, after: Int) = StockOperation(
            id = 1,
            clientOpId = "op",
            productId = 1,
            productName = "Товар",
            barcode = "4601000012343",
            type = type,
            quantityDelta = delta,
            quantityBefore = after - delta,
            quantityAfter = after,
            note = "",
            createdAt = 0,
            syncStatus = SyncStatus.PENDING,
        )

        assertEquals("+3", op(OpType.RECEIVE, 3, 13).signedQuantity)
        assertEquals("−3", op(OpType.WRITE_OFF, -3, 7).signedQuantity)
        assertEquals("= 8", op(OpType.INVENTORY, -2, 8).signedQuantity)
        assertTrue(op(OpType.RECEIVE, 1, 1).isPending)
    }
}
