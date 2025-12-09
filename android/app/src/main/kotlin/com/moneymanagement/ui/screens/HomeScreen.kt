package com.moneymanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneymanagement.ui.theme.ExpenseRed
import com.moneymanagement.ui.theme.IncomeGreen
import com.moneymanagement.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.*

/**
 * HomeScreen - Main dashboard screen
 * 
 * Displays:
 * - Total balance
 * - Monthly income and expenses
 * - Recent transactions
 * - Quick actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToTransactionList: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Money Management") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatCurrency(uiState.totalBalance),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Income and Expense Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Income Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = IncomeGreen.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = "Income",
                            tint = IncomeGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = formatCurrency(uiState.monthlyIncome),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }
                
                // Expense Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = ExpenseRed.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.TrendingDown,
                            contentDescription = "Expense",
                            tint = ExpenseRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = formatCurrency(uiState.monthlyExpenses),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }
            }
            
            // Recent Transactions Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToTransactionList) {
                    Text("View All")
                }
            }
            
            // Recent Transactions List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.recentTransactions.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = "No transactions",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.recentTransactions.take(5).forEach { transaction ->
                        TransactionItem(transaction = transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: com.moneymanagement.data.local.TransactionEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (!transaction.description.isNullOrBlank()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (transaction.type == "income") {
                    "+${formatCurrency(transaction.amount)}"
                } else {
                    "-${formatCurrency(transaction.amount)}"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == "income") {
                    IncomeGreen
                } else {
                    ExpenseRed
                }
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}

