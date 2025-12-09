package com.moneymanagement.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moneymanagement.ui.screens.*
import com.moneymanagement.viewmodel.AuthViewModel

/**
 * NavGraph - Main navigation graph for the app
 * 
 * Defines all navigation routes and their corresponding screens.
 * Handles navigation flow between login, home, transactions, and settings.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegistrationSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTransactionList = {
                    navController.navigate(Screen.TransactionList.route)
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.TransactionList.route) {
            TransactionListScreen(
                onNavigateToAddTransaction = { transactionId ->
                    if (transactionId != null) {
                        navController.navigate("${Screen.AddTransaction.route}/$transactionId")
                    } else {
                        navController.navigate(Screen.AddTransaction.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "${Screen.AddTransaction.route}/{${Screen.AddTransaction.ARG_TRANSACTION_ID}}"
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString(Screen.AddTransaction.ARG_TRANSACTION_ID)
            AddTransactionScreen(
                transactionId = transactionId,
                onTransactionSaved = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                transactionId = null,
                onTransactionSaved = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

