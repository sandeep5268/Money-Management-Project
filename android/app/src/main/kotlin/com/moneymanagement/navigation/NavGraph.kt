
package com.moneymanagement.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moneymanagement.ui.screens.*

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
                        // [UX FIX] Clears entire back stack (0) to prevent 
                        // back-button returning to Login.
                        popUpTo(0) { inclusive = true } 
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
                        popUpTo(0) { inclusive = true } // [UX FIX] Same stack clearing
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
                    // Navigate without ID for "Create" mode
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
                        // [REFACTOR] Use query param syntax for optional arg
                        // Example: "add_transaction?transactionId=123"
                        navController.navigate(
                            "${Screen.AddTransaction.route}?${Screen.AddTransaction.ARG_TRANSACTION_ID}=$transactionId"
                        )
                    } else {
                        navController.navigate(Screen.AddTransaction.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // [DRY FIX] Merged two composables into one using Optional Arguments.
        // Route format: "add_transaction?transactionId={transactionId}"
        composable(
            route = "${Screen.AddTransaction.route}?${Screen.AddTransaction.ARG_TRANSACTION_ID}={${Screen.AddTransaction.ARG_TRANSACTION_ID}}",
            arguments = listOf(
                navArgument(Screen.AddTransaction.ARG_TRANSACTION_ID) {
                    type = NavType.StringType
                    nullable = true // Allows this argument to be missing (null)
                    defaultValue = null
                }
            )
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
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Clears history on logout
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

"""
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
"""
