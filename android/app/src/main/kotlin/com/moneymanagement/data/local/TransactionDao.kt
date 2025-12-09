package com.moneymanagement.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * TransactionDao - Data Access Object for TransactionEntity
 * 
 * Provides methods to interact with the transactions table in Room database.
 * Uses Flow for reactive queries that automatically update UI when data changes.
 */
@Dao
interface TransactionDao {
    
    /**
     * Insert a new transaction
     * Uses REPLACE strategy to update if transaction with same ID exists
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    /**
     * Insert multiple transactions
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
    
    /**
     * Update an existing transaction
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    /**
     * Delete a transaction
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    /**
     * Delete transaction by ID
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)
    
    /**
     * Get all transactions ordered by date (newest first)
     * Returns Flow for reactive updates
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    /**
     * Get transaction by ID
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?
    
    /**
     * Get transactions within a date range
     * Returns Flow for reactive updates
     */
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    
    /**
     * Get transactions by category
     * Returns Flow for reactive updates
     */
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>
    
    /**
     * Get transactions by type (income or expense)
     * Returns Flow for reactive updates
     */
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>
    
    /**
     * Get total spending (expenses) for a date range
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpending(startDate: Long, endDate: Long): Double
    
    /**
     * Get total income for a date range
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'income' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalIncome(startDate: Long, endDate: Long): Double
    
    /**
     * Get total balance (income - expenses) for a date range
     */
    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END), 0) - 
            COALESCE(SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END), 0)
        FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getBalance(startDate: Long, endDate: Long): Double
    
    /**
     * Get recent transactions (last N transactions)
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>
}
