package com.moneymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.data.local.TransactionEntity
import com.moneymanagement.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * TransactionViewModel - Manages transaction list and operations
 * 
 * Responsibilities:
 * - Display list of transactions
 * - Filter transactions by date, category, type
 * - Add, edit, delete transactions
 * - Handle transaction form state
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    // UI State for transaction list
    data class TransactionListUiState(
        val transactions: List<TransactionEntity> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val filterType: String? = null, // "income", "expense", or null for all
        val filterCategory: String? = null,
        val startDate: Long? = null,
        val endDate: Long? = null
    )
    
    // UI State for add/edit transaction form
    data class TransactionFormUiState(
        val id: String? = null,
        val amount: String = "",
        val type: String = "expense", // "income" or "expense"
        val category: String = "",
        val description: String = "",
        val date: Long = System.currentTimeMillis(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    private val _listUiState = MutableStateFlow(TransactionListUiState())
    val listUiState: StateFlow<TransactionListUiState> = _listUiState.asStateFlow()
    
    private val _formUiState = MutableStateFlow(TransactionFormUiState())
    val formUiState: StateFlow<TransactionFormUiState> = _formUiState.asStateFlow()
    
    init {
        // Observe transactions flow and apply filters reactively
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactions(),
                _listUiState
            ) { transactions, state ->
                val filtered = transactions.filter { transaction ->
                    val matchesType = state.filterType == null || transaction.type == state.filterType
                    val matchesCategory = state.filterCategory == null || transaction.category == state.filterCategory
                    val matchesDateRange = if (state.startDate != null && state.endDate != null) {
                        transaction.date >= state.startDate!! && transaction.date <= state.endDate!!
                    } else {
                        true
                    }
                    matchesType && matchesCategory && matchesDateRange
                }
                _listUiState.value = state.copy(
                    transactions = filtered,
                    isLoading = false,
                    error = null
                )
            }.collect()
        }
    }
    
    /**
     * Load all transactions
     */
    fun loadTransactions() {
        _listUiState.value = _listUiState.value.copy(isLoading = true)
    }
    
    /**
     * Set filter for transaction type
     */
    fun setFilterType(type: String?) {
        _listUiState.value = _listUiState.value.copy(filterType = type)
    }
    
    /**
     * Set filter for category
     */
    fun setFilterCategory(category: String?) {
        _listUiState.value = _listUiState.value.copy(filterCategory = category)
    }
    
    /**
     * Set date range filter
     */
    fun setDateRange(startDate: Long?, endDate: Long?) {
        _listUiState.value = _listUiState.value.copy(
            startDate = startDate,
            endDate = endDate
        )
    }
    
    /**
     * Load transaction for editing
     */
    fun loadTransactionForEdit(id: String) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(id)
                if (transaction != null) {
                    _formUiState.value = TransactionFormUiState(
                        id = transaction.id,
                        amount = transaction.amount.toString(),
                        type = transaction.type,
                        category = transaction.category,
                        description = transaction.description ?: "",
                        date = transaction.date
                    )
                }
            } catch (e: Exception) {
                _formUiState.value = _formUiState.value.copy(
                    error = "Failed to load transaction: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update form fields
     */
    fun updateAmount(amount: String) {
        _formUiState.value = _formUiState.value.copy(amount = amount)
    }
    
    fun updateType(type: String) {
        _formUiState.value = _formUiState.value.copy(type = type)
    }
    
    fun updateCategory(category: String) {
        _formUiState.value = _formUiState.value.copy(category = category)
    }
    
    fun updateDescription(description: String) {
        _formUiState.value = _formUiState.value.copy(description = description)
    }
    
    fun updateDate(date: Long) {
        _formUiState.value = _formUiState.value.copy(date = date)
    }
    
    /**
     * Save transaction (add or update)
     */
    fun saveTransaction() {
        viewModelScope.launch {
            try {
                val formState = _formUiState.value
                
                // Validate
                if (formState.amount.isBlank()) {
                    _formUiState.value = formState.copy(error = "Amount is required")
                    return@launch
                }
                
                val amount = formState.amount.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    _formUiState.value = formState.copy(error = "Invalid amount")
                    return@launch
                }
                
                if (formState.category.isBlank()) {
                    _formUiState.value = formState.copy(error = "Category is required")
                    return@launch
                }
                
                _formUiState.value = formState.copy(isLoading = true, error = null)
                
                val transaction = TransactionEntity(
                    id = formState.id ?: UUID.randomUUID().toString(),
                    amount = amount,
                    type = formState.type,
                    category = formState.category,
                    description = formState.description.takeIf { it.isNotBlank() },
                    date = formState.date,
                    source = "manual",
                    synced = false
                )
                
                if (formState.id != null) {
                    transactionRepository.updateTransaction(transaction)
                } else {
                    transactionRepository.insertTransaction(transaction)
                }
                
                // Reset form
                _formUiState.value = TransactionFormUiState()
                
            } catch (e: Exception) {
                _formUiState.value = _formUiState.value.copy(
                    isLoading = false,
                    error = "Failed to save transaction: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Delete transaction
     */
    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransactionById(id)
                loadTransactions()
            } catch (e: Exception) {
                _listUiState.value = _listUiState.value.copy(
                    error = "Failed to delete transaction: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Reset form state
     */
    fun resetForm() {
        _formUiState.value = TransactionFormUiState()
    }
}

