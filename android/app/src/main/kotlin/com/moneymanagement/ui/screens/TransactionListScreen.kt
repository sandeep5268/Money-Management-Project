package com.moneymanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneymanagement.data.local.TransactionEntity
import com.moneymanagement.ui.theme.ExpenseRed
import com.moneymanagement.ui.theme.IncomeGreen
import com.moneymanagement.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.*

/**
 * TransactionListScreen - Displays list of all transactions
 * 
 * Features:
 * - Filter by type (income/expense)
 * - Filter by category
 * - Edit/Delete transactions
 * - Add new transaction
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
    onNavigateToAddTransaction: (String?) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.listUiState.collectAsState().value
    var filterType by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }
    
    LaunchedEffect(filterType) {
        viewModel.setFilterType(filterType)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Filter buttons
                    FilterChip(
                        selected = filterType == null,
                        onClick = { filterType = null },
                        label = { Text("All") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = filterType == "income",
                        onClick = { filterType = if (filterType == "income") null else "income" },
                        label = { Text("Income") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = filterType == "expense",
                        onClick = { filterType = if (filterType == "expense") null else "expense" },
                        label = { Text("Expense") }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddTransaction(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(
                        text = "No transactions found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onNavigateToAddTransaction(null) }) {
                        Text("Add Transaction")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.transactions) { transaction ->
                    TransactionListItem(
                        transaction = transaction,
                        onEdit = { onNavigateToAddTransaction(transaction.id) },
                        onDelete = { viewModel.deleteTransaction(transaction.id) }
                    )
                }
            }
        }
        
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show error snackbar
            }
        }
    }
}

@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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

