package com.zingayu.barmanagement.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val password: String,
    val role: String, // "admin" or "staff"
    val name: String
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val stock: Int,
    val costPrice: Double,
    val sellingPrice: Double,
    val supplier: String,
    val barcode: String = ""
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val contactName: String,
    val address: String
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey val id: String,
    val supplierId: String,
    val supplierName: String,
    val totalAmount: Double,
    val status: String, // "pending", "delivered", "paid"
    val purchasedAt: String,
    val deliveredAt: String = ""
)

@Entity(tableName = "purchase_items")
data class PurchaseItemEntity(
    @PrimaryKey val id: String,
    val purchaseId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitCost: Double,
    val totalCost: Double
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val customer: String,
    val tableName: String,
    val status: String, // "pending", "preparing", "served", "closed"
    val total: Double,
    val paymentMethod: String = "cash",
    val createdAt: String,
    val closedAt: String = ""
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val productName: String,
    val amount: Double,
    val quantity: Int,
    val costPrice: Double,
    val soldAt: String
)

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val totalAmount: Double,
    val itemCount: Int,
    val paymentMethod: String,
    val issuedAt: String,
    val receiptNumber: String
)

@Dao
interface BarDao {
    // User queries
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun getUser(username: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Product queries
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun productCount(): Int

    @Query("SELECT * FROM products WHERE barcode = :barcode")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    // Supplier queries
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun observeSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    // Purchase queries
    @Query("SELECT * FROM purchases ORDER BY purchasedAt DESC")
    fun observePurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id = :purchaseId")
    suspend fun getPurchase(purchaseId: String): PurchaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun observePurchaseItems(purchaseId: String): Flow<List<PurchaseItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)

    @Query("UPDATE purchases SET status = :status, deliveredAt = :deliveredAt WHERE id = :purchaseId")
    suspend fun updatePurchaseStatus(purchaseId: String, status: String, deliveredAt: String)

    // Order queries
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id ASC")
    fun observeOrderItems(orderId: String): Flow<List<OrderItemEntity>>

    @Query("UPDATE orders SET status = :status, closedAt = :closedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, closedAt: String)

    // Sales queries
    @Query("SELECT * FROM sales ORDER BY soldAt DESC")
    fun observeSales(): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Query("SELECT SUM(amount) FROM sales WHERE soldAt > :startTime")
    fun observeTotalSales(startTime: String): Flow<Double?>

    // Receipt queries
    @Query("SELECT * FROM receipts ORDER BY issuedAt DESC")
    fun observeReceipts(): Flow<List<ReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    // Stock management
    @Query("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
    suspend fun addStock(productId: String, quantity: Int)

    @Query("UPDATE products SET stock = MAX(0, stock - :quantity) WHERE id = :productId")
    suspend fun reduceStock(productId: String, quantity: Int)

    // Analytics
    @Query("SELECT SUM(amount) FROM sales WHERE soldAt LIKE :datePattern")
    suspend fun getSalesToday(datePattern: String): Double?

    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    suspend fun getOrderCount(status: String): Int
}

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        SupplierEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        SaleEntity::class,
        ReceiptEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BarDatabase : RoomDatabase() {
    abstract fun barDao(): BarDao
}
