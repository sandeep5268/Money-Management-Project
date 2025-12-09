package com.moneymanagement.navigation

/**
 * Screen - Sealed class defining all navigation routes in the app
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object TransactionList : Screen("transaction_list")
    object AddTransaction : Screen("add_transaction") {
        const val ARG_TRANSACTION_ID = "transaction_id"
        fun createRoute(transactionId: String? = null) = 
            if (transactionId != null) "add_transaction/$transactionId" else "add_transaction"
    }
    object Settings : Screen("settings")
    
    // Helper function to parse transaction ID from route
    fun parseTransactionId(route: String): String? {
        return route.substringAfterLast("/").takeIf { it != "add_transaction" }
    }
}

