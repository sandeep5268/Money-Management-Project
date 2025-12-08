package com.moneymanagement.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * TransactionEntity - Room database entity for storing transactions locally
 * 
 * Represents a financial transaction (income or expense)
 * Stored in encrypted local SQLite database via Room ORM
 * 
 * Fields:
 * - id: Unique identifier (UUID)
 * - amount: Transaction amount (in paise/cents)
 * - type: "income" or "expense"
 * - category: Transaction category (food, travel, salary, etc.)
 * - description: Optional description
 * - date: Timestamp of transaction
 * - source: SMS source or manual entry
 * - synced: Whether transaction is synced to backend
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String, // UUID generated locally or from server
    
    val amount: Double, // Amount in currency units
    
    val type: String, // "income" or "expense"
    
    val category: String, // Food, Travel, Salary, Entertainment, etc.
    
    val description: String?, // Optional transaction description
    
    val date: Long, // Timestamp in milliseconds
    
    val source: String, // "sms", "manual", "sync"
    
    val synced: Boolean = false, // Whether synced to backend
    
    val createdAt: Long = System.currentTimeMillis() // Local creation timestamp
)
