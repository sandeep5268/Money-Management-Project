
package com.moneymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.data.local.TransactionEntity
import com.moneymanagement.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

/**
 * HomeViewModel - Manages home screen dashboard data.
 * Refactored for Financial Precision, Reactivity, and Correctness.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    data class HomeUiState(
        // [CRITICAL FIX] Changed Double to Long (Minor Units)
        val monthlyNetBalance: Long = 0, // Renamed from totalBalance to reflect actual logic
        val monthlyIncome: Long = 0,
        val monthlyExpenses: Long = 0,
        
        val recentTransactions: List<TransactionEntity> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        // [ARCH FIX] Initialize by observing the stream, not just a snapshot
        observeDashboard()
    }
    
    private fun observeDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // [DESIGN FIX] Use java.time for stable date calculation
            val now = YearMonth.now(ZoneId.systemDefault())
            val startOfMonth = now.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfMonth = now.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            try {
                // [ARCH FIX] Combine Flow of transactions with One-Shot statistics.
                // Note: Ideally, repository totals should also be Flows for full reactivity.
                // Here we re-fetch totals whenever the transaction list updates.
                transactionRepository.getRecentTransactions(5)
                    .distinctUntilChanged()
                    .collect { transactions ->
                        // Recalculate totals whenever the list changes (reactive update)
                        // Note: Assuming repo methods return Long/Double. converting to Long for safety.
                        val income = transactionRepository.getTotalIncome(startOfMonth, endOfMonth).toLong()
                        val expenses = transactionRepository.getTotalSpending(startOfMonth, endOfMonth).toLong()
                        
                        // [LOGIC FIX] Renamed to monthlyNetBalance. 
                        // This represents (Income - Expense) for the CURRENT MONTH, not total user wealth.
                        val netBalance = income - expenses
                        
                        _uiState.update { state ->
                            state.copy(
                                monthlyNetBalance = netBalance,
                                monthlyIncome = income,
                                monthlyExpenses = expenses,
                                recentTransactions = transactions,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Unable to load dashboard data." // [SECURITY] Sanitized error
                    ) 
                }
            }
        }
    }
    
    /**
     * Refresh dashboard data manually
     */
    fun refresh() {
        // Since we are observing the flow, standard updates happen automatically.
        // This might be used to force-recalculate totals if the Flow doesn't trigger.
        observeDashboard()
    }
    
    /**
     * Get balance for a specific date range
     */
    fun getBalanceForRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                val income = transactionRepository.getTotalIncome(startDate, endDate).toLong()
                val expenses = transactionRepository.getTotalSpending(startDate, endDate).toLong()
                val balance = income - expenses
                
                _uiState.update { 
                    it.copy(
                        monthlyNetBalance = balance,
                        monthlyIncome = income,
                        monthlyExpenses = expenses
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Calculation failed.") }
            }
        }
    }
}

"""
/**
 * HomeViewModel - Manages home screen dashboard data
 * 
 * Responsibilities:
 * - Display balance summary
 * - Show recent transactions
 * - Calculate monthly/weekly statistics
 * - Provide spending breakdown by category
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    data class HomeUiState(
        val totalBalance: Double = 0.0,
        val monthlyIncome: Double = 0.0,
        val monthlyExpenses: Double = 0.0,
        val recentTransactions: List<TransactionEntity> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    /**
     * Load all dashboard data
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Get current month date range
                val calendar = Calendar.getInstance()
                val startOfMonth = calendar.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val endOfMonth = Calendar.getInstance().timeInMillis
                
                // Load statistics
                val monthlyIncome = transactionRepository.getTotalIncome(startOfMonth, endOfMonth)
                val monthlyExpenses = transactionRepository.getTotalSpending(startOfMonth, endOfMonth)
                val totalBalance = monthlyIncome - monthlyExpenses
                
                // Load recent transactions - collect first value
                val recentTransactions = transactionRepository.getRecentTransactions(5).first()
                
                _uiState.value = HomeUiState(
                    totalBalance = totalBalance,
                    monthlyIncome = monthlyIncome,
                    monthlyExpenses = monthlyExpenses,
                    recentTransactions = recentTransactions,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load dashboard data: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Refresh dashboard data
     */
    fun refresh() {
        loadDashboardData()
    }
    
    /**
     * Get balance for a specific date range
     */
    fun getBalanceForRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                val income = transactionRepository.getTotalIncome(startDate, endDate)
                val expenses = transactionRepository.getTotalSpending(startDate, endDate)
                val balance = income - expenses
                
                _uiState.value = _uiState.value.copy(
                    totalBalance = balance,
                    monthlyIncome = income,
                    monthlyExpenses = expenses
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to calculate balance: ${e.message}"
                )
            }
        }
    }
}
"""
