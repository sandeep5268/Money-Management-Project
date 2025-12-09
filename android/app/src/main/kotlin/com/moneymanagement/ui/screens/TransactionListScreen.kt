// package com.moneymanagement.ui.screens

// import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.lazy.LazyColumn
// import androidx.compose.foundation.lazy.items
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.Add
// import androidx.compose.material3.*
// import androidx.compose.runtime.*
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.unit.dp
// import androidx.hilt.navigation.compose.hiltViewModel
// import com.moneymanagement.data.local.TransactionEntity
// import com.moneymanagement.ui.theme.ExpenseRed
// import com.moneymanagement.ui.theme.IncomeGreen
// import com.moneymanagement.viewmodel.TransactionViewModel
// import java.text.NumberFormat
// import java.util.*

// /**
//  * TransactionListScreen - Displays list of all transactions
//  * 
//  * Features:
//  * - Filter by type (income/expense)
//  * - Filter by category
//  * - Edit/Delete transactions
//  * - Add new transaction
//  */
// @OptIn(ExperimentalMaterial3Api::class)
// @Composable
// fun TransactionListScreen(
//     viewModel: TransactionViewModel = hiltViewModel(),
//     onNavigateToAddTransaction: (String?) -> Unit,
//     onNavigateBack: () -> Unit
// ) {
//     val uiState = viewModel.listUiState.collectAsState().value
//     var filterType by remember { mutableStateOf<String?>(null) }
    
//     LaunchedEffect(Unit) {
//         viewModel.loadTransactions()
//     }
    
//     LaunchedEffect(filterType) {
//         viewModel.setFilterType(filterType)
//     }
    
//     Scaffold(
//         topBar = {
//             TopAppBar(
//                 title = { Text("Transactions") },
//                 navigationIcon = {
//                     IconButton(onClick = onNavigateBack) {
//                         Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                     }
//                 },
//                 actions = {
//                     // Filter buttons
//                     FilterChip(
//                         selected = filterType == null,
//                         onClick = { filterType = null },
//                         label = { Text("All") }
//                     )
//                     Spacer(modifier = Modifier.width(8.dp))
//                     FilterChip(
//                         selected = filterType == "income",
//                         onClick = { filterType = if (filterType == "income") null else "income" },
//                         label = { Text("Income") }
//                     )
//                     Spacer(modifier = Modifier.width(8.dp))
//                     FilterChip(
//                         selected = filterType == "expense",
//                         onClick = { filterType = if (filterType == "expense") null else "expense" },
//                         label = { Text("Expense") }
//                     )
//                 }
//             )
//         },
//         floatingActionButton = {
//             FloatingActionButton(onClick = { onNavigateToAddTransaction(null) }) {
//                 Icon(Icons.Default.Add, contentDescription = "Add Transaction")
//             }
//         }
//     ) { padding ->
//         if (uiState.isLoading) {
//             Box(
//                 modifier = Modifier
//                     .fillMaxSize()
//                     .padding(padding),
//                 contentAlignment = androidx.compose.ui.Alignment.Center
//             ) {
//                 CircularProgressIndicator()
//             }
//         } else if (uiState.transactions.isEmpty()) {
//             Box(
//                 modifier = Modifier
//                     .fillMaxSize()
//                     .padding(padding),
//                 contentAlignment = androidx.compose.ui.Alignment.Center
//             ) {
//                 Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
//                     Text(
//                         text = "No transactions found",
//                         style = MaterialTheme.typography.bodyLarge
//                     )
//                     Spacer(modifier = Modifier.height(16.dp))
//                     Button(onClick = { onNavigateToAddTransaction(null) }) {
//                         Text("Add Transaction")
//                     }
//                 }
//             }
//         } else {
//             LazyColumn(
//                 modifier = Modifier
//                     .fillMaxSize()
//                     .padding(padding),
//                 contentPadding = PaddingValues(16.dp),
//                 verticalArrangement = Arrangement.spacedBy(8.dp)
//             ) {
//                 items(uiState.transactions) { transaction ->
//                     TransactionListItem(
//                         transaction = transaction,
//                         onEdit = { onNavigateToAddTransaction(transaction.id) },
//                         onDelete = { viewModel.deleteTransaction(transaction.id) }
//                     )
//                 }
//             }
//         }
        
//         uiState.error?.let { error ->
//             LaunchedEffect(error) {
//                 // Show error snackbar
//             }
//         }
//     }
// }

package com.moneymanagement.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneymanagement.data.local.TransactionEntity
import com.moneymanagement.ui.theme.ExpenseRed
import com.moneymanagement.ui.theme.IncomeGreen
import com.moneymanagement.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * TransactionListScreen - Displays list of all transactions.
 * Refactored for Layout Safety, Delete Confirmation, and Data Integrity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
    onNavigateToAddTransaction: (String?) -> Unit,
    onNavigateBack: () -> Unit
) {
    // [PERFORMANCE FIX] Lifecycle-aware state collection
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
    var filterType by remember { mutableStateOf<String?>(null) }
    
    // [SAFETY FIX] Delete Confirmation State
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<String?>(null) }
    
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddTransaction(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // [UI FIX] Moved Filter Chips out of TopAppBar to prevent overflow.
            // Placed in a scrollable row below the header.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { filterType = null },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filterType == "income",
                    onClick = { filterType = if (filterType == "income") null else "income" },
                    label = { Text("Income") }
                )
                FilterChip(
                    selected = filterType == "expense",
                    onClick = { filterType = if (filterType == "expense") null else "expense" },
                    label = { Text("Expense") }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.transactions) { transaction ->
                        TransactionListItem(
                            transaction = transaction,
                            onEdit = { onNavigateToAddTransaction(transaction.id) },
                            onDeleteClick = {
                                // [SAFETY FIX] Trigger dialog instead of immediate delete
                                transactionToDelete = transaction.id
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // [SAFETY FIX] Delete Confirmation Dialog
    if (showDeleteDialog && transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        showDeleteDialog = false
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDeleteClick: () -> Unit
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info Column
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
            
            // Amount & Delete Action
            Row(verticalAlignment = Alignment.CenterVertically) {
                // [DATA FIX] Handle Long (Minor Units) -> String
                val amountString = formatCurrency(transaction.amountMinor)
                
                Text(
                    text = if (transaction.type == "income") "+$amountString" else "-$amountString",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == "income") IncomeGreen else ExpenseRed
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // [UX FIX] Added Explicit Delete Button
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete transaction",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Formats currency from minor units (e.g., cents/paise).
 * [DESIGN FIX] Input changed to Long to match Entity refactor.
 */
fun formatCurrency(amountMinor: Long): String {
    val amountMajor = amountMinor / 100.0
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amountMajor)
}

/**
 * Formats timestamp using modern java.time API.
 */
fun formatDate(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

"""
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
"""
