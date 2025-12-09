package com.moneymanagement.di

import android.content.Context
import androidx.room.Room
import com.moneymanagement.data.local.TransactionDao
import com.moneymanagement.data.local.TransactionDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DatabaseModule - Hilt module for providing database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideTransactionDatabase(
        @ApplicationContext context: Context
    ): TransactionDatabase {
        return Room.databaseBuilder(
            context,
            TransactionDatabase::class.java,
            "transaction_database"
        )
            .fallbackToDestructiveMigration() // For development
            .build()
    }
    
    @Provides
    fun provideTransactionDao(database: TransactionDatabase): TransactionDao {
        return database.transactionDao()
    }
}

