package com.zingayu.barmanagement.data

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val stock: Int,
    val costPrice: Double,
    val sellingPrice: Double,
    val supplier: String
)

data class OrderItem(
    val name: String,
    val quantity: Int,
    val price: Double
)

data class BarOrder(
    val orderId: String,
    val customer: String,
    val table: String,
    val status: String,
    val items: List<OrderItem>,
    val total: Double
)

data class SaleRecord(
    val productName: String,
    val amount: Double,
    val time: String
)
