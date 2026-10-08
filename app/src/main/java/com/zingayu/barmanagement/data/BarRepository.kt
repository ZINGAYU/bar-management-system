package com.zingayu.barmanagement.data

import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BarRepository(private val database: BarDatabase) {
    private val dao = database.barDao()

    val products: Flow<List<ProductEntity>> = dao.observeProducts()
    val suppliers: Flow<List<SupplierEntity>> = dao.observeSuppliers()
    val orders: Flow<List<OrderEntity>> = dao.observeOrders()
    val sales: Flow<List<SaleEntity>> = dao.observeSales()

    suspend fun seedIfEmpty() {
        if (dao.productCount() > 0) return

        val products = listOf(
            ProductEntity("p1", "Crown Royal", "Whisky", 12, 28.0, 42.0, "Urban Distillers"),
            ProductEntity("p2", "Heineken", "Beer", 48, 3.2, 7.5, "Harbor Distributors"),
            ProductEntity("p3", "Merlot", "Wine", 16, 12.5, 24.0, "Vine Hill Imports"),
            ProductEntity("p4", "Smirnoff Vodka", "Spirits", 10, 18.0, 29.0, "North Valley Supply"),
            ProductEntity("p5", "Coca Cola", "Mixers", 30, 1.8, 4.5, "City Beverage Co."),
            ProductEntity("p6", "Cashew Nuts", "Snacks", 24, 2.5, 6.0, "Quick Bites"),
            ProductEntity("p7", "Jack Daniel's", "Whisky", 8, 26.0, 38.0, "Urban Distillers"),
            ProductEntity("p8", "Red Bull", "Energy", 20, 2.2, 5.5, "Drift Supply")
        )

        val suppliers = listOf(
            SupplierEntity("s1", "Urban Distillers", "+1-555-0101", "Alicia Gomez"),
            SupplierEntity("s2", "Harbor Distributors", "+1-555-0102", "Ben Harris"),
            SupplierEntity("s3", "Vine Hill Imports", "+1-555-0103", "Carlos Dela"),
            SupplierEntity("s4", "North Valley Supply", "+1-555-0104", "Dana Cole")
        )

        val orders = listOf(
            OrderEntity("o1", "A. Smith", "Table 4", "Preparing", 57.0, "2026-10-08T09:12:00"),
            OrderEntity("o2", "Group 8", "Bar Counter", "Served", 79.5, "2026-10-08T11:30:00")
        )

        val orderItems = listOf(
            OrderItemEntity("oi1", "o1", "Heineken", 2, 7.5),
            OrderItemEntity("oi2", "o1", "Crown Royal", 1, 42.0),
            OrderItemEntity("oi3", "o2", "Merlot", 3, 24.0),
            OrderItemEntity("oi4", "o2", "Coca Cola", 2, 4.5)
        )

        val sales = listOf(
            SaleEntity("sa1", "Heineken", 15.0, 2, "2026-10-08T09:10:00"),
            SaleEntity("sa2", "Crown Royal", 42.0, 1, "2026-10-08T10:25:00"),
            SaleEntity("sa3", "Merlot", 24.0, 1, "2026-10-08T11:40:00"),
            SaleEntity("sa4", "Smirnoff Vodka", 29.0, 1, "2026-10-08T13:15:00"),
            SaleEntity("sa5", "Red Bull", 5.5, 1, "2026-10-08T14:45:00")
        )

        dao.insertProducts(products)
        suppliers.forEach { dao.insertSupplier(it) }
        orders.forEach { dao.insertOrder(it) }
        dao.insertOrderItems(orderItems)
        sales.forEach { dao.insertSale(it) }
    }

    fun orderDetailsFor(orderId: String): Flow<List<OrderItemEntity>> = database.barDao().observeOrderItems(orderId)

    suspend fun addStock(productId: String, quantity: Int) {
        dao.addStock(productId, quantity)
    }

    suspend fun sellProduct(productName: String, amount: Double, quantity: Int) {
        dao.insertSale(
            SaleEntity(
                id = "sale-${System.currentTimeMillis()}",
                productName = productName,
                amount = amount,
                quantity = quantity,
                soldAt = System.currentTimeMillis().toString()
            )
        )
    }
}
