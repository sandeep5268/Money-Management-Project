
package com.moneymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.data.local.TransactionEntity
import com.moneymanagement.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * TransactionViewModel - Manages transaction list and operations.
 * Refactored to fix Infinite Loop, Financial Precision, and State Management.
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    // [ARCH FIX] Split Filters from UI State to prevent infinite loops
    data class FilterCriteria(
        val type: String? = null,
        val category: String? = null,
        val startDate: Long? = null,
        val endDate: Long? = null
    )

    data class TransactionListUiState(
        val transactions: List<TransactionEntity> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    data class TransactionFormUiState(
        val id: String? = null,
        val amount: String = "",
        val type: String = "expense",
        val category: String = "",
        val description: String = "",
        val date: Long = System.currentTimeMillis(),
        val isLoading: Boolean = false,
        val error: String? = null,
        // [UX FIX] Added signal for navigation
        val isSaved: Boolean = false
    )
    
    // Inputs
    private val _filterState = MutableStateFlow(FilterCriteria())
    val filterState = _filterState.asStateFlow()

    private val _formUiState = MutableStateFlow(TransactionFormUiState())
    val formUiState: StateFlow<TransactionFormUiState> = _formUiState.asStateFlow()
    
    // [ARCH FIX] Reactive Output State
    // Combines Repo Data + Filters -> Produces UI State.
    // Does NOT write back to itself, preventing the infinite loop.
    val listUiState: StateFlow<TransactionListUiState> = combine(
        transactionRepository.getAllTransactions(),
        _filterState
    ) { transactions, filters ->
        val filtered = transactions.filter { transaction ->
            val matchesType = filters.type == null || transaction.type == filters.type
            val matchesCategory = filters.category == null || transaction.category == filters.category
            val matchesDateRange = if (filters.startDate != null && filters.endDate != null) {
                transaction.date >= filters.startDate && transaction.date <= filters.endDate
            } else {
                true
            }
            matchesType && matchesCategory && matchesDateRange
        }
        TransactionListUiState(transactions = filtered)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionListUiState(isLoading = true)
    )
    
    /**
     * Set filter for transaction type
     */
    fun setFilterType(type: String?) {
        _filterState.update { it.copy(type = type) }
    }
    
    fun setFilterCategory(category: String?) {
        _filterState.update { it.copy(category = category) }
    }
    
    fun setDateRange(startDate: Long?, endDate: Long?) {
        _filterState.update { it.copy(startDate = startDate, endDate = endDate) }
    }
    
    /**
     * Load transaction for editing
     */
    fun loadTransactionForEdit(id: String) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(id)
                if (transaction != null) {
                    // [DATA FIX] Convert stored Long (minor units) back to Decimal String for UI
                    // e.g. 1050 -> "10.50"
                    val amountMajor = BigDecimal(transaction.amountMinor).movePointLeft(2).toString()
                    
                    _formUiState.value = TransactionFormUiState(
                        id = transaction.id,
                        amount = amountMajor,
                        type = transaction.type,
                        category = transaction.category,
                        description = transaction.description ?: "",
                        date = transaction.date
                    )
                }
            } catch (e: Exception) {
                _formUiState.update { it.copy(error = "Failed to load transaction") }
            }
        }
    }
    
    // --- Form Updates ---
    fun updateAmount(amount: String) = _formUiState.update { it.copy(amount = amount, error = null) }
    fun updateType(type: String) = _formUiState.update { it.copy(type = type) }
    fun updateCategory(category: String) = _formUiState.update { it.copy(category = category, error = null) }
    fun updateDescription(description: String) = _formUiState.update { it.copy(description = description) }
    fun updateDate(date: Long) = _formUiState.update { it.copy(date = date) }
    
    fun saveTransaction() {
        val formState = _formUiState.value
        
        // Validation
        if (formState.amount.isBlank()) {
            _formUiState.update { it.copy(error = "Amount is required") }
            return
        }
        
        // [DATA FIX] Parse String -> BigDecimal -> Long (Minor Units)
        // Prevents floating point errors.
        val amountMinor = try {
            val decimal = BigDecimal(formState.amount)
            if (decimal.signum() <= 0) throw NumberFormatException("Must be positive")
            decimal.movePointRight(2).longValueExact()
        } catch (e: Exception) {
            _formUiState.update { it.copy(error = "Invalid amount format") }
            return
        }
        
        if (formState.category.isBlank()) {
            _formUiState.update { it.copy(error = "Category is required") }
            return
        }
        
        viewModelScope.launch {
            _formUiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val transaction = TransactionEntity(
                    id = formState.id ?: UUID.randomUUID().toString(),
                    amountMinor = amountMinor, // [FIX] Using refactored Long field
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
                
                // [UX FIX] Signal success so UI can navigate back
                _formUiState.update { it.copy(isLoading = false, isSaved = true) }
                
            } catch (e: Exception) {
                _formUiState.update { 
                    it.copy(isLoading = false, error = "Save failed. Please try again.") 
                }
            }
        }
    }
    
    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransactionById(id)
                // Note: No need to call loadTransactions() manually.
                // The flow 'transactionRepository.getAllTransactions()' observed in listUiState
                // will emit the new list automatically.
            } catch (e: Exception) {
                // In a real app, use a separate event channel for one-off errors
            }
        }
    }
    
    fun resetForm() {
        _formUiState.value = TransactionFormUiState()
    }
    
    // Placeholder for loadTransactions used in UI (now handled by Flow)
    fun loadTransactions() {
        // No-op in reactive architecture, but kept for compatibility
    }
}

"""
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
"""
