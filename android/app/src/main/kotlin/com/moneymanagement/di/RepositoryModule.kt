package com.moneymanagement.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.moneymanagement.data.repository.AuthRepository
import com.moneymanagement.data.repository.AuthRepositoryImpl
import com.moneymanagement.data.repository.TransactionRepository
import com.moneymanagement.data.repository.TransactionRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * RepositoryModule - Hilt module for providing repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")
    
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        dataStore: DataStore<Preferences>
    ): AuthRepository {
        return AuthRepositoryImpl(dataStore)
    }
    
    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: com.moneymanagement.data.local.TransactionDao
    ): TransactionRepository {
        return TransactionRepositoryImpl(transactionDao)
    }
}

