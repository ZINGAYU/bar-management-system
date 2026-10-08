package com.zingayu.barmanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zingayu.barmanagement.data.BarRepository
import com.zingayu.barmanagement.data.BarOrder
import com.zingayu.barmanagement.data.Product
import com.zingayu.barmanagement.data.SaleRecord

@Composable
fun BarManagementApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Inventory", "Orders", "Sales", "Reports")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bar Manager",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(BarRepository)
                1 -> InventoryScreen(BarRepository)
                2 -> OrdersScreen(BarRepository)
                3 -> SalesScreen(BarRepository)
                4 -> ReportsScreen(BarRepository)
            }
        }
    }
}

@Composable
fun DashboardScreen(repository: BarRepository) {
    val totalRevenue = repository.sales.sumOf { it.amount }
    val totalOrders = repository.orders.size
    val inventoryValue = repository.products.sumOf { it.stock * it.costPrice }
    val lowStockItems = repository.products.filter { it.stock <= 5 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Operations overview",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(title = "Revenue", amount = "\$${"%.2f".format(totalRevenue)}")
                SummaryCard(title = "Orders", amount = totalOrders.toString())
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(title = "Stock value", amount = "\$${"%.2f".format(inventoryValue)}")
                SummaryCard(title = "Low stock", amount = lowStockItems.size.toString())
            }
        }

        item {
            Text(
                text = "Low stock alerts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(lowStockItems) { product ->
            ProductAlertCard(product)
        }

        item {
            Text(
                text = "Recent sales",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(repository.sales.take(5)) { sale ->
            SaleRow(sale)
        }
    }
}

@Composable
fun InventoryScreen(repository: BarRepository) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Inventory",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(repository.products) { product ->
            InventoryCard(product) {
                repository.addStock(product.id, 5)
            }
        }
    }
}

@Composable
fun OrdersScreen(repository: BarRepository) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Open orders",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(repository.orders) { order ->
            OrderCard(order)
        }
    }
}

@Composable
fun SalesScreen(repository: BarRepository) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Sales register",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Today's sales")
                    Text(
                        text = "\$${"%.2f".format(repository.sales.sumOf { it.amount })}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(repository.sales) { sale ->
            SaleRow(sale)
        }
    }
}

@Composable
fun ReportsScreen(repository: BarRepository) {
    val topProducts = repository.products.sortedByDescending { it.stock * it.sellingPrice }
    val grossMargin = repository.sales.sumOf { it.amount } - repository.products.sumOf { it.stock * it.costPrice }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Reports",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Margin estimate")
                    Text(
                        text = "\$${"%.2f".format(grossMargin)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Text("Top inventory value", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        items(topProducts.take(5)) { product ->
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
fun SummaryCard(title: String, amount: String) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProductAlertCard(product: Product) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text("${product.stock} units left")
            }
            Button(onClick = { BarRepository.addStock(product.id, 10) }) {
                Text("Restock")
            }
        }
    }
}

@Composable
fun InventoryCard(product: Product, onAddStock: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(product.category)
                Text("Stock: ${product.stock} units")
                Text("Price: \$${"%.2f".format(product.sellingPrice)}")
            }
            Button(onClick = onAddStock) {
                Text("Add 5")
            }
        }
    }
}

@Composable
fun OrderCard(order: BarOrder) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(order.customer, fontWeight = FontWeight.Bold)
                Text(order.status)
            }
            Text("Table: ${order.table}")
            order.items.forEach { item ->
                Text("- ${item.name} x${item.quantity} @ \$${"%.2f".format(item.price)}")
            }
            Text("Total: \$${"%.2f".format(order.total)}")
        }
    }
}

@Composable
fun SaleRow(sale: SaleRecord) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(sale.productName, fontWeight = FontWeight.Bold)
                Text(sale.time)
            }
            Text("\$${"%.2f".format(sale.amount)}")
        }
    }
}
