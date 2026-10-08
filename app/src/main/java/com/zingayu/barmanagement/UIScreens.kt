package com.zingayu.barmanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.zingayu.barmanagement.data.BarRepository
import com.zingayu.barmanagement.data.OrderEntity
import com.zingayu.barmanagement.data.OrderItemEntity
import com.zingayu.barmanagement.data.ProductEntity
import com.zingayu.barmanagement.data.PurchaseEntity
import com.zingayu.barmanagement.data.PurchaseItemEntity
import com.zingayu.barmanagement.data.ReceiptEntity
import com.zingayu.barmanagement.data.SaleEntity
import com.zingayu.barmanagement.data.SupplierEntity
import com.zingayu.barmanagement.data.UserEntity
import kotlinx.coroutines.launch

@Composable
fun BarManagementApp(repository: BarRepository) {
    var loggedInUser by remember { mutableStateOf<UserEntity?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.seedIfEmpty()
    }

    if (loggedInUser == null) {
        LoginScreen(repository) { user ->
            loggedInUser = user
        }
    } else {
        MainAppScreen(repository, loggedInUser!!) {
            loggedInUser = null
        }
    }
}

@Composable
fun LoginScreen(repository: BarRepository, onLoginSuccess: (UserEntity) -> Unit) {
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("admin123") }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bar Manager Pro",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            val user = repository.loginUser(username, password)
                            if (user != null) {
                                onLoginSuccess(user)
                            } else {
                                errorMessage = "Invalid username or password"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }
            }
        }

        Text(
            text = "Demo: admin / admin123 or staff / staff123",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 32.dp)
        )
    }
}

@Composable
fun MainAppScreen(
    repository: BarRepository,
    user: UserEntity,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = if (user.role == "admin") {
        listOf("Dashboard", "POS", "Inventory", "Purchases", "Orders", "Reports", "Staff")
    } else {
        listOf("Dashboard", "POS", "Orders")
    }

    val products by repository.products.collectAsState(initial = emptyList())
    val orders by repository.orders.collectAsState(initial = emptyList())
    val sales by repository.sales.collectAsState(initial = emptyList())
    val suppliers by repository.suppliers.collectAsState(initial = emptyList())
    val purchases by repository.purchases.collectAsState(initial = emptyList())
    val receipts by repository.receipts.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bar Manager Pro")
                        Text("${user.name} (${user.role})", style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, label ->
                    Button(
                        onClick = { selectedTab = index },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(label, fontSize = MaterialTheme.typography.labelSmall.fontSize)
                    }
                }
            }

            when (selectedTab) {
                0 -> DashboardScreen(products, orders, sales, user)
                1 -> POSScreen(products, repository)
                2 -> InventoryScreen(products, repository)
                3 -> if (user.role == "admin") PurchasesScreen(purchases, suppliers, repository) else {}
                4 -> OrdersScreen(orders, repository)
                5 -> if (user.role == "admin") ReportsScreen(products, sales, purchases, receipts) else {}
                6 -> if (user.role == "admin") StaffScreen() else {}
            }
        }
    }
}

@Composable
fun DashboardScreen(
    products: List<ProductEntity>,
    orders: List<OrderEntity>,
    sales: List<SaleEntity>,
    user: UserEntity
) {
    val revenue = sales.sumOf { it.amount }
    val lowStock = products.count { it.stock <= 5 }
    val totalStockValue = products.sumOf { it.stock * it.costPrice }
    val pendingOrders = orders.count { it.status == "pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Welcome, ${user.name}!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard("Revenue", "\$${"%.2f".format(revenue)}")
                SummaryCard("Orders", orders.size.toString())
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard("Stock value", "\$${"%.2f".format(totalStockValue)}")
                SummaryCard("Low stock", lowStock.toString())
            }
        }

        item {
            Text("Pending: $pendingOrders orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Text("Recent sales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        items(sales.take(5)) { sale -> SaleCard(sale) }
    }
}

@Composable
fun POSScreen(products: List<ProductEntity>, repository: BarRepository) {
    var selectedProducts by remember { mutableStateOf<List<Pair<ProductEntity, Int>>>(emptyList()) }
    var customerName by remember { mutableStateOf("") }
    var tableNumber by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("cash") }
    val scope = rememberCoroutineScope()

    val total = selectedProducts.sumOf { (product, qty) -> product.sellingPrice * qty }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Point of Sale", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Customer") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = tableNumber,
                onValueChange = { tableNumber = it },
                label = { Text("Table") },
                modifier = Modifier.weight(0.5f)
            )
        }

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(products) { product ->
                POSProductRow(product) { qty ->
                    if (qty > 0) {
                        selectedProducts = selectedProducts.filter { it.first.id != product.id }
                        selectedProducts = selectedProducts + (product to qty)
                    } else {
                        selectedProducts = selectedProducts.filter { it.first.id != product.id }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                selectedProducts.forEach { (product, qty) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${product.name} x$qty")
                        Text("\$${"%.2f".format(product.sellingPrice * qty)}")
                    }
                }
                Divider(modifier = Modifier.padding(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL", fontWeight = FontWeight.Bold)
                    Text("\$${"%.2f".format(total)}", fontWeight = FontWeight.Bold)
                }
            }
        }

        Button(
            onClick = {
                scope.launch {
                    if (customerName.isNotEmpty() && selectedProducts.isNotEmpty()) {
                        val items = selectedProducts.mapIndexed { idx, (product, qty) ->
                            OrderItemEntity(
                                id = "oi-$idx",
                                orderId = "",
                                name = product.name,
                                quantity = qty,
                                price = product.sellingPrice,
                                subtotal = product.sellingPrice * qty
                            )
                        }
                        val orderId = repository.createOrder(customerName, tableNumber.ifEmpty { "Counter" }, items, total)
                        repository.createReceipt(orderId, total, selectedProducts.size, paymentMethod)
                        selectedProducts.forEach { (product, qty) ->
                            repository.sellProduct(product.id, product.name, product.sellingPrice * qty, qty, product.costPrice)
                        }
                        selectedProducts = emptyList()
                        customerName = ""
                        tableNumber = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Complete Order")
        }
    }
}

@Composable
fun POSProductRow(product: ProductEntity, onQuantityChanged: (Int) -> Unit) {
    var quantity by remember { mutableIntStateOf(0) }

    Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text("\$${"%.2f".format(product.sellingPrice)}")
                Text("Stock: ${product.stock}")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = {
                    quantity = maxOf(0, quantity - 1)
                    onQuantityChanged(quantity)
                }) { Text("-") }
                Text(quantity.toString(), modifier = Modifier.padding(8.dp))
                Button(onClick = {
                    if (quantity < product.stock) {
                        quantity++
                        onQuantityChanged(quantity)
                    }
                }) { Text("+") }
            }
        }
    }
}

@Composable
fun InventoryScreen(products: List<ProductEntity>, repository: BarRepository) {
    val scope = rememberCoroutineScope()
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
                scope.launch {
                    repository.addStock(product.id, 20)
                }
            })
        }
    }
}

@Composable
fun InventoryCard(product: ProductEntity, onRestock: () -> Unit) {
    val stockColor = when {
        product.stock > 20 -> Color.Green
        product.stock > 5 -> Color.Yellow
        else -> Color.Red
    }

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
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Stock: ${product.stock}", color = stockColor, fontWeight = FontWeight.Bold)
                Text("Cost: \$${"%.2f".format(product.costPrice)}")
            }
        }
    }
}

@Composable
fun PurchasesScreen(purchases: List<PurchaseEntity>, suppliers: List<SupplierEntity>, repository: BarRepository) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Purchase Orders", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        items(purchases) { purchase ->
            PurchaseCard(purchase, repository)
        }
    }
}

@Composable
fun PurchaseCard(purchase: PurchaseEntity, repository: BarRepository) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(purchase.supplierName, fontWeight = FontWeight.Bold)
                Text(purchase.status, fontWeight = FontWeight.Bold)
            }
            Text("Total: \$${"%.2f".format(purchase.totalAmount)}")
            if (purchase.status == "pending") {
                Button(onClick = { androidx.compose.runtime.LaunchedEffect(purchase.id) {
                    repository.updatePurchaseStatus(purchase.id, "delivered")
                }}, modifier = Modifier.fillMaxWidth()) {
                    Text("Mark as Delivered")
                }
            }
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
fun OrderCard(order: OrderEntity, repository: BarRepository) {
    val items by repository.orderDetailsFor(order.id).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(order.customer, fontWeight = FontWeight.Bold)
                Text(order.status, fontWeight = FontWeight.Bold)
            }
            Text("Table: ${order.tableName}")
            items.forEach { item ->
                Text("- ${item.name} x${item.quantity} @ \$${"%.2f".format(item.price)}")
            }
            Text("Total: \$${"%.2f".format(order.total)}", fontWeight = FontWeight.Bold)
            if (order.status != "closed") {
                Button(
                    onClick = {
                        scope.launch {
                            repository.updateOrderStatus(order.id, "closed")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Order")
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(
    products: List<ProductEntity>,
    sales: List<SaleEntity>,
    purchases: List<PurchaseEntity>,
    receipts: List<ReceiptEntity>
) {
    val revenue = sales.sumOf { it.amount }
    val costOfGoods = sales.sumOf { it.costPrice * it.quantity }
    val profit = revenue - costOfGoods
    val profitMargin = if (revenue > 0) (profit / revenue) * 100 else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Business Reports", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Financial Summary")
                    Divider(modifier = Modifier.padding(8.dp))
                    ReportRow("Total Revenue", "\$${"%.2f".format(revenue)}")
                    ReportRow("Cost of Goods", "\$${"%.2f".format(costOfGoods)}")
                    ReportRow("Profit", "\$${"%.2f".format(profit)}")
                    ReportRow("Profit Margin", "${"%.1f".format(profitMargin)}%")
                }
            }
        }

        item {
            Text("Top Selling Products", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        items(sales.groupBy { it.productName }.map { (name, sales) ->
            name to sales.sumOf { it.quantity }
        }.sortedByDescending { it.second }.take(5)) { (name, qty) ->
            ReportProductRow(name, qty.toString())
        }
    }
}

@Composable
fun ReportRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReportProductRow(name: String, quantity: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name)
        Text("$quantity units", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StaffScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Staff Management", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Admin feature - not available to staff users")
    }
}

@Composable
fun SummaryCard(title: String, value: String) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SaleCard(sale: SaleEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(sale.productName, fontWeight = FontWeight.Bold)
                Text("Qty: ${sale.quantity}")
            }
            Text("\$${"%.2f".format(sale.amount)}", fontWeight = FontWeight.Bold)
        }
    }
}
