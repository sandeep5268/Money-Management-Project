package com.moneymanagement.data.repository

import com.moneymanagement.data.local.TransactionDao
import com.moneymanagement.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TransactionRepositoryImpl - Implementation of TransactionRepository
 * 
 * Delegates all operations to TransactionDao.
 * Provides a clean abstraction layer between ViewModels and data layer.
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {
    
    override fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }
    
    override suspend fun getTransactionById(id: String): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }
    
    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }
    
    override fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByCategory(category)
    }
    
    override fun getTransactionsByType(type: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByType(type)
    }
    
    override fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentTransactions(limit)
    }
    
    override suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }
    
    override suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }
    
    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }
    
    override suspend fun deleteTransactionById(id: String) {
        transactionDao.deleteTransactionById(id)
    }
    
    override suspend fun getTotalSpending(startDate: Long, endDate: Long): Double {
        return transactionDao.getTotalSpending(startDate, endDate)
    }
    
    override suspend fun getTotalIncome(startDate: Long, endDate: Long): Double {
        return transactionDao.getTotalIncome(startDate, endDate)
    }
    
    override suspend fun getBalance(startDate: Long, endDate: Long): Double {
        return transactionDao.getBalance(startDate, endDate)
    }
}

