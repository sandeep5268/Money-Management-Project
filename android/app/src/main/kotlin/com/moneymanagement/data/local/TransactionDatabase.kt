package com.moneymanagement.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * TransactionDatabase - Room database for storing transactions locally.
 *
 * Security Note: Currently unencrypted. 
 * TODO: Integrate SQLCipher or SupportFactory for data-at-rest encryption.
 */
@Database(
    entities = [TransactionEntity::class],
    version = 1,
    // [CONFIGURATION] Set to false to avoid build errors if schema location 
    // is not configured in build.gradle. Set to true only when schema export is set up.
    exportSchema = false 
)
abstract class TransactionDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    
    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null
        
        private const val DATABASE_NAME = "transaction_database"
        
        fun getDatabase(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    DATABASE_NAME
                )
                    // [DATA SAFETY FIX] Removed fallbackToDestructiveMigration()
                    // This prevents the app from wiping user data on schema updates.
                    // When version increases, you MUST provide a Migration object.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

"""
/**
 * TransactionDatabase - Room database for storing transactions locally
 * 
 * This database stores all financial transactions locally using Room ORM.
 * Version 1 is the initial schema.
 */
@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class TransactionDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    
    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null
        
        fun getDatabase(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transaction_database"
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
"""
