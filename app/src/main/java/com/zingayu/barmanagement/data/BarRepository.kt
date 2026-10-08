package com.zingayu.barmanagement.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BarRepository(private val database: BarDatabase) {
    private val dao = database.barDao()

    val products: Flow<List<ProductEntity>> = dao.observeProducts()
    val suppliers: Flow<List<SupplierEntity>> = dao.observeSuppliers()
    val orders: Flow<List<OrderEntity>> = dao.observeOrders()
    val sales: Flow<List<SaleEntity>> = dao.observeSales()
    val purchases: Flow<List<PurchaseEntity>> = dao.observePurchases()
    val receipts: Flow<List<ReceiptEntity>> = dao.observeReceipts()

    suspend fun seedIfEmpty() {
        if (dao.productCount() > 0) return

        // Create default users
        val adminUser = UserEntity(
            id = "u1",
            username = "admin",
            password = "admin123",
            role = "admin",
            name = "Admin User"
        )
        val staffUser = UserEntity(
            id = "u2",
            username = "staff",
            password = "staff123",
            role = "staff",
            name = "Bar Staff"
        )
        dao.insertUser(adminUser)
        dao.insertUser(staffUser)

        val products = listOf(
            ProductEntity("p1", "Crown Royal", "Whisky", 12, 28.0, 42.0, "Urban Distillers", "4511001"),
            ProductEntity("p2", "Heineken", "Beer", 48, 3.2, 7.5, "Harbor Distributors", "4511002"),
            ProductEntity("p3", "Merlot", "Wine", 16, 12.5, 24.0, "Vine Hill Imports", "4511003"),
            ProductEntity("p4", "Smirnoff Vodka", "Spirits", 10, 18.0, 29.0, "North Valley Supply", "4511004"),
            ProductEntity("p5", "Coca Cola", "Mixers", 30, 1.8, 4.5, "City Beverage Co.", "4511005"),
            ProductEntity("p6", "Cashew Nuts", "Snacks", 24, 2.5, 6.0, "Quick Bites", "4511006"),
            ProductEntity("p7", "Jack Daniel's", "Whisky", 8, 26.0, 38.0, "Urban Distillers", "4511007"),
            ProductEntity("p8", "Red Bull", "Energy", 20, 2.2, 5.5, "Drift Supply", "4511008")
        )

        val suppliers = listOf(
            SupplierEntity("s1", "Urban Distillers", "+1-555-0101", "sales@distillers.com", "Alicia Gomez", "123 Bourbon St"),
            SupplierEntity("s2", "Harbor Distributors", "+1-555-0102", "info@harbor.com", "Ben Harris", "456 Port Ave"),
            SupplierEntity("s3", "Vine Hill Imports", "+1-555-0103", "wine@vinehill.com", "Carlos Dela", "789 Wine Valley"),
            SupplierEntity("s4", "North Valley Supply", "+1-555-0104", "supply@north.com", "Dana Cole", "321 Valley Rd")
        )

        val orders = listOf(
            OrderEntity("o1", "A. Smith", "Table 4", "served", 57.0, "cash", "2026-10-08T09:12:00", "2026-10-08T09:45:00"),
            OrderEntity("o2", "Group 8", "Bar Counter", "closed", 79.5, "card", "2026-10-08T11:30:00", "2026-10-08T12:15:00")
        )

        val orderItems = listOf(
            OrderItemEntity("oi1", "o1", "Heineken", 2, 7.5, 15.0),
            OrderItemEntity("oi2", "o1", "Crown Royal", 1, 42.0, 42.0),
            OrderItemEntity("oi3", "o2", "Merlot", 3, 24.0, 72.0),
            OrderItemEntity("oi4", "o2", "Coca Cola", 2, 4.5, 9.0)
        )

        val sales = listOf(
            SaleEntity("sa1", "p2", "Heineken", 15.0, 2, 3.2, "2026-10-08T09:10:00"),
            SaleEntity("sa2", "p1", "Crown Royal", 42.0, 1, 28.0, "2026-10-08T10:25:00"),
            SaleEntity("sa3", "p3", "Merlot", 24.0, 1, 12.5, "2026-10-08T11:40:00"),
            SaleEntity("sa4", "p4", "Smirnoff Vodka", 29.0, 1, 18.0, "2026-10-08T13:15:00"),
            SaleEntity("sa5", "p8", "Red Bull", 5.5, 1, 2.2, "2026-10-08T14:45:00")
        )

        val purchases = listOf(
            PurchaseEntity("pu1", "s1", "Urban Distillers", 560.0, "delivered", "2026-10-07T10:00:00", "2026-10-08T08:00:00"),
            PurchaseEntity("pu2", "s2", "Harbor Distributors", 250.0, "pending", "2026-10-08T14:30:00")
        )

        val purchaseItems = listOf(
            PurchaseItemEntity("pi1", "pu1", "p1", "Crown Royal", 20, 28.0, 560.0),
            PurchaseItemEntity("pi2", "pu2", "p2", "Heineken", 50, 3.2, 160.0),
            PurchaseItemEntity("pi3", "pu2", "p5", "Coca Cola", 30, 1.8, 54.0)
        )

        val receipts = listOf(
            ReceiptEntity("r1", "o1", 57.0, 3, "cash", "2026-10-08T09:45:00", "RCP-20261001"),
            ReceiptEntity("r2", "o2", 79.5, 5, "card", "2026-10-08T12:15:00", "RCP-20261002")
        )

        dao.insertProducts(products)
        suppliers.forEach { dao.insertSupplier(it) }
        orders.forEach { dao.insertOrder(it) }
        dao.insertOrderItems(orderItems)
        sales.forEach { dao.insertSale(it) }
        purchases.forEach { dao.insertPurchase(it) }
        dao.insertPurchaseItems(purchaseItems)
        receipts.forEach { dao.insertReceipt(it) }
    }

    // User management
    suspend fun loginUser(username: String, password: String): UserEntity? {
        return dao.getUser(username, password)
    }

    // Product management
    fun orderDetailsFor(orderId: String): Flow<List<OrderItemEntity>> = dao.observeOrderItems(orderId)

    fun purchaseDetailsFor(purchaseId: String): Flow<List<PurchaseItemEntity>> = dao.observePurchaseItems(purchaseId)

    suspend fun addStock(productId: String, quantity: Int) {
        dao.addStock(productId, quantity)
    }

    suspend fun reduceStock(productId: String, quantity: Int) {
        dao.reduceStock(productId, quantity)
    }

    suspend fun getProductByBarcode(barcode: String): ProductEntity? {
        return dao.getProductByBarcode(barcode)
    }

    // Sales management
    suspend fun sellProduct(productId: String, productName: String, amount: Double, quantity: Int, costPrice: Double) {
        dao.insertSale(
            SaleEntity(
                id = "sale-${UUID.randomUUID()}",
                productId = productId,
                productName = productName,
                amount = amount,
                quantity = quantity,
                costPrice = costPrice,
                soldAt = getCurrentTimestamp()
            )
        )
        reduceStock(productId, quantity)
    }

    // Order management
    suspend fun createOrder(customer: String, table: String, items: List<OrderItemEntity>, total: Double): String {
        val orderId = "order-${UUID.randomUUID()}"
        val order = OrderEntity(
            id = orderId,
            customer = customer,
            tableName = table,
            status = "pending",
            total = total,
            createdAt = getCurrentTimestamp()
        )
        dao.insertOrder(order)
        val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
        dao.insertOrderItems(itemsWithOrderId)
        return orderId
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        val closedAt = if (status == "closed") getCurrentTimestamp() else ""
        dao.updateOrderStatus(orderId, status, closedAt)
    }

    // Purchase management
    suspend fun createPurchase(supplierId: String, supplierName: String, items: List<PurchaseItemEntity>, total: Double): String {
        val purchaseId = "purchase-${UUID.randomUUID()}"
        val purchase = PurchaseEntity(
            id = purchaseId,
            supplierId = supplierId,
            supplierName = supplierName,
            totalAmount = total,
            status = "pending",
            purchasedAt = getCurrentTimestamp()
        )
        dao.insertPurchase(purchase)
        val itemsWithPurchaseId = items.map { it.copy(purchaseId = purchaseId) }
        dao.insertPurchaseItems(itemsWithPurchaseId)
        return purchaseId
    }

    suspend fun updatePurchaseStatus(purchaseId: String, status: String) {
        val deliveredAt = if (status == "delivered") getCurrentTimestamp() else ""
        dao.updatePurchaseStatus(purchaseId, status, deliveredAt)
    }

    // Receipt management
    suspend fun createReceipt(orderId: String, totalAmount: Double, itemCount: Int, paymentMethod: String): String {
        val receiptNumber = "RCP-${System.currentTimeMillis()}"
        val receipt = ReceiptEntity(
            id = "receipt-${UUID.randomUUID()}",
            orderId = orderId,
            totalAmount = totalAmount,
            itemCount = itemCount,
            paymentMethod = paymentMethod,
            issuedAt = getCurrentTimestamp(),
            receiptNumber = receiptNumber
        )
        dao.insertReceipt(receipt)
        return receiptNumber
    }

    // Analytics
    suspend fun getTodaysSales(): Double {
        val datePattern = getCurrentDate() + "%"
        return dao.getSalesToday(datePattern) ?: 0.0
    }

    suspend fun getPendingOrderCount(): Int {
        return dao.getOrderCount("pending")
    }

    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
