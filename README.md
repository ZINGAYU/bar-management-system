package com.zingayu.barmanagement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zingayu.barmanagement.data.BarRepository
import com.zingayu.barmanagement.data.OrderEntity
import com.zingayu.barmanagement.data.OrderItemEntity
import com.zingayu.barmanagement.data.ProductEntity
import com.zingayu.barmanagement.data.SaleEntity
import com.zingayu.barmanagement.data.SupplierEntity
import kotlinx.coroutines.flow.map

@Composable
fun BarManagementApp(repository: BarRepository) {
    val tabs = listOf("Dashboard", "Inventory", "Orders", "Sales", "Suppliers", "Reports")
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        repository.seedIfEmpty()
    }

    val products by repository.products.collectAsState(initial = emptyList())
    val orders by repository.orders.collectAsState(initial = emptyList())
    val sales by repository.sales.collectAsState(initial = emptyList())
    val suppliers by repository.suppliers.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bar Manager Pro") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, label ->
                    Button(
                        onClick = { selectedTab = index },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label)
                    }
                }
            }

            when (selectedTab) {
                0 -> DashboardScreen(products, orders, sales)
                1 -> InventoryScreen(products, repository)
                2 -> OrdersScreen(orders, repository)
                3 -> SalesScreen(sales)
                4 -> SuppliersScreen(suppliers)
                5 -> ReportsScreen(products, sales)
            }
        }
    }
}

@Composable
fun DashboardScreen(
    products: List<ProductEntity>,
    orders: List<OrderEntity>,
    sales: List<SaleEntity>
) {
    val revenue = sales.sumOf { it.amount }
    val lowStock = products.count { it.stock <= 5 }
    val totalStockValue = products.sumOf { it.stock * it.costPrice }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Operations overview", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Revenue", "\$${"%.2f".format(revenue)}")
                SummaryCard("Orders", orders.size.toString())
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Stock value", "\$${"%.2f".format(totalStockValue)}")
                SummaryCard("Low stock", lowStock.toString())
            }
        }

        item {
            Text("Recent sales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        items(sales.take(6)) { sale ->
            SaleCard(sale)
        }
    }
}

@Composable
fun InventoryScreen(products: List<ProductEntity>, repository: BarRepository) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Inventory", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        items(products) { product ->
            InventoryCard(product = product, onRestock = {
                androidx.compose.runtime.LaunchedEffect(product.id) {
                    repository.addStock(product.id, 10)
                }
            })
        }
    }
}

@Composable
fun OrdersScreen(orders: List<OrderEntity>, repository: BarRepository) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Orders", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        items(orders) { order ->
            OrderCard(order, repository)
        }
    }
}

@Composable
fun SalesScreen(sales: List<SaleEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Sales register", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total sales")
                    Text("\$${"%.2f".format(sales.sumOf { it.amount })}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
        items(sales) { sale -> SaleCard(sale) }
    }
}

@Composable
fun SuppliersScreen(suppliers: List<SupplierEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Suppliers", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        items(suppliers) { supplier ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(supplier.name, fontWeight = FontWeight.Bold)
                    Text("Contact: ${supplier.contactName}")
                    Text("Phone: ${supplier.phone}")
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(products: List<ProductEntity>, sales: List<SaleEntity>) {
    val topProducts = products.sortedByDescending { it.stock * it.sellingPrice }.take(5)
    val profitEstimate = sales.sumOf { it.amount } - products.sumOf { it.stock * it.costPrice }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Reports", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estimated margin")
                    Text("\$${"%.2f".format(profitEstimate)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text("Top inventory value", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        items(topProducts) { product ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(product.name)
                Text("\$${"%.2f".format(product.stock * product.costPrice)}")
            }
            Divider()
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InventoryCard(product: ProductEntity, onRestock: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = onRestock) { Text("Restock") }
            }
            Text(product.category)
            Text("Stock: ${product.stock} units")
            Text("Cost: \$${"%.2f".format(product.costPrice)} | Sell: \$${"%.2f".format(product.sellingPrice)}")
            Text("Supplier: ${product.supplier}")
        }
    }
}

@Composable
fun OrderCard(order: OrderEntity, repository: BarRepository) {
    val items by repository.orderDetailsFor(order.id).collectAsState(initial = emptyList())

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(order.customer, fontWeight = FontWeight.Bold)
                Text(order.status)
            }
            Text("Table: ${order.tableName}")
            items.forEach { item ->
                Text("- ${item.name} x${item.quantity} @ \$${"%.2f".format(item.price)}")
            }
            Text("Total: \$${"%.2f".format(order.total)}")
        }
    }
}

@Composable
fun SaleCard(sale: SaleEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(sale.productName, fontWeight = FontWeight.Bold)
                Text("Qty: ${sale.quantity}")
            }
            Text("\$${"%.2f".format(sale.amount)}")
        }
    }
}
