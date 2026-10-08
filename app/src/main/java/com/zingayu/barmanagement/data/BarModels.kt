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

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val stock: Int,
    val costPrice: Double,
    val sellingPrice: Double,
    val supplier: String
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val contactName: String
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val customer: String,
    val tableName: String,
    val status: String,
    val total: Double,
    val createdAt: String
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val name: String,
    val quantity: Int,
    val price: Double
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val productName: String,
    val amount: Double,
    val quantity: Int,
    val soldAt: String
)

@Dao
interface BarDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun productCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun observeSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id ASC")
    fun observeOrderItems(orderId: String): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM sales ORDER BY soldAt DESC")
    fun observeSales(): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Query("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
    suspend fun addStock(productId: String, quantity: Int)

    @Query("UPDATE products SET stock = MAX(0, stock - :quantity) WHERE id = :productId")
    suspend fun reduceStock(productId: String, quantity: Int)
}

@Database(
    entities = [
        ProductEntity::class,
        SupplierEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        SaleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BarDatabase : RoomDatabase() {
    abstract fun barDao(): BarDao
}
