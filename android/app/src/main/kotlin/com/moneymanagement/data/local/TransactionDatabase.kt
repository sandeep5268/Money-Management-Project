package com.moneymanagement.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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

