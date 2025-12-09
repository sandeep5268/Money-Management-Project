
package com.moneymanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneymanagement.viewmodel.TransactionViewModel

/**
 * AddTransactionScreen - Form to add or edit a transaction
 * Refactored for Navigation Stability and Input UX.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: String?,
    viewModel: TransactionViewModel = hiltViewModel(),
    onTransactionSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Note: detailed StateFlow collection should ideally use collectAsStateWithLifecycle()
    val formState = viewModel.formUiState.collectAsState().value
    
    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            viewModel.loadTransactionForEdit(transactionId)
        } else {
            viewModel.resetForm()
        }
    }
    
    // [CRITICAL FIX] Navigation Logic
    // Previously, this checked 'if (id != null)'. This caused the screen to close 
    // immediately when loading an existing transaction (Edit Mode).
    // The ViewModel State MUST expose a specific 'isSaved' or 'saveSuccess' flag 
    // that is only true after the Save button is clicked and processing finishes.
    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) {
            onTransactionSaved()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (transactionId != null) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Selection
            Text(
                text = "Type",
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

"""
/**
 * AddTransactionScreen - Form to add or edit a transaction
 * 
 * Features:
 * - Add new transaction
 * - Edit existing transaction
 * - Select transaction type (income/expense)
 * - Select category
 * - Enter amount and description
 * - Select date
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: String?,
    viewModel: TransactionViewModel = hiltViewModel(),
    onTransactionSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val formState = viewModel.formUiState.collectAsState().value
    
    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            viewModel.loadTransactionForEdit(transactionId)
        } else {
            viewModel.resetForm()
        }
    }
    
    LaunchedEffect(formState) {
        if (formState.id != null && formState.amount.isNotBlank() && 
            formState.category.isNotBlank() && !formState.isLoading && 
            formState.error == null) {
            // Transaction saved successfully, navigate back
            onTransactionSaved()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (transactionId != null) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Selection
            Text(
                text = "Type",
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = formState.type == "income",
                    onClick = { viewModel.updateType("income") },
                    label = { Text("Income") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = formState.type == "expense",
                    onClick = { viewModel.updateType("expense") },
                    label = { Text("Expense") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Amount Input
            OutlinedTextField(
                value = formState.amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    if (formState.error?.contains("amount", ignoreCase = true) == true) {
                        Text(formState.error, color = MaterialTheme.colorScheme.error)
                    }
                },
                isError = formState.error?.contains("amount", ignoreCase = true) == true
            )
            
            // Category Input
            OutlinedTextField(
                value = formState.category,
                onValueChange = { viewModel.updateCategory(it) },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., Food, Travel, Salary") },
                supportingText = {
                    if (formState.error?.contains("category", ignoreCase = true) == true) {
                        Text(formState.error, color = MaterialTheme.colorScheme.error)
                    }
                },
                isError = formState.error?.contains("category", ignoreCase = true) == true
            )
            
            // Description Input
            OutlinedTextField(
                value = formState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            // Error Message
            if (formState.error != null && 
                !formState.error.contains("amount", ignoreCase = true) &&
                !formState.error.contains("category", ignoreCase = true)) {
                Text(
                    text = formState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save Button
            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !formState.isLoading
            ) {
                if (formState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(if (transactionId != null) "Update Transaction" else "Save Transaction")
                }
            }
        }
    }
}
"""
