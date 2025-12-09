package com.moneymanagement.data.repository

import com.moneymanagement.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * TransactionRepository - Interface for transaction data operations
 * 
 * Provides methods for CRUD operations on transactions.
 * All operations are local-only using Room database.
 */
interface TransactionRepository {
    /**
     * Get all transactions as Flow
     */
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    /**
     * Get transaction by ID
     */
    suspend fun getTransactionById(id: String): TransactionEntity?
    
    /**
     * Get transactions within date range
     */
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    
    /**
     * Get transactions by category
     */
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>
    
    /**
     * Get transactions by type (income/expense)
     */
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>
    
    /**
     * Get recent transactions
     */
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>
    
    /**
     * Insert a new transaction
     */
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    /**
     * Update an existing transaction
     */
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    /**
     * Delete a transaction
     */
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    /**
     * Delete transaction by ID
     */
    suspend fun deleteTransactionById(id: String)
    
    /**
     * Get total spending for date range
     */
    suspend fun getTotalSpending(startDate: Long, endDate: Long): Double
    
    /**
     * Get total income for date range
     */
    suspend fun getTotalIncome(startDate: Long, endDate: Long): Double
    
    /**
     * Get balance (income - expenses) for date range
     */
    suspend fun getBalance(startDate: Long, endDate: Long): Double
}

