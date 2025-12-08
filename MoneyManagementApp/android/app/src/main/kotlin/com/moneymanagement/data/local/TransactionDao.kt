package com.moneymanagement.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * TransactionDao - Data Access Object for Transaction entity
 * 
 * Provides database operations for transaction management:
 * - Insert/Update/Delete transactions
 * - Query transactions by date, category, amount
 * - Real-time transaction updates via Flow
 * - Aggregate queries (sum, count, average)
 * 
 * Uses Room ORM for type-safe database access
 */
@Dao
interface TransactionDao {
    
    /**
     * Insert a new transaction
     * - Replaces if transaction with same ID exists
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    /**
     * Update existing transaction
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    /**
     * Delete transaction by ID
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    /**
     * Get all transactions as Flow
     * - Real-time updates to UI when data changes
     * - Automatically observed by Compose
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    /**
     * Get transactions for a specific date range
     * - Used for monthly/yearly analysis
     */
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    
    /**
     * Get transactions by category
     * - Used for category-wise breakdown
     */
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>
    
    /**
     * Get total spending for a date range
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = 'expense'")
    suspend fun getTotalSpending(startDate: Long, endDate: Long): Double
    
    /**
     * Get total income for a date range
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = 'income'")
    suspend fun getTotalIncome(startDate: Long, endDate: Long): Double
}
