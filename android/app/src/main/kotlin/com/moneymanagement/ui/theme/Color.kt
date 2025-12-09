
package com.moneymanagement.ui.theme

import androidx.compose.ui.graphics.Color

// Money Management App - Brand Colors
val PrimaryGreen = Color(0xFF4CAF50)
val PrimaryGreenDark = Color(0xFF388E3C)
val PrimaryGreenLight = Color(0xFF81C784)

val AccentBlue = Color(0xFF2196F3)
val AccentBlueDark = Color(0xFF1976D2)
val AccentBlueLight = Color(0xFF64B5F6)

// Semantic Colors
val ExpenseRed = Color(0xFFF44336)

// [DRY FIX] Reference the primary brand color directly.
// Ensures that if PrimaryGreen changes, IncomeGreen updates automatically.
val IncomeGreen = PrimaryGreen 

// Neutral Colors
val BackgroundLight = Color(0xFFFAFAFA)
val BackgroundDark = Color(0xFF121212)

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)

// [CLEANUP] Removed unused 'Purple' and 'Pink' template colors.
// Note: Ensure your Theme.kt is updated to use PrimaryGreen instead of Purple40.

// package com.moneymanagement.ui.theme

// import androidx.compose.ui.graphics.Color

// // Primary color palette
// val Purple80 = Color(0xFFD0BCFF)
// val PurpleGrey80 = Color(0xFFCCC2DC)
// val Pink80 = Color(0xFFEFB8C8)

// val Purple40 = Color(0xFF6650a4)
// val PurpleGrey40 = Color(0xFF625b71)
// val Pink40 = Color(0xFF7D5260)

// // Money Management App Colors
// val PrimaryGreen = Color(0xFF4CAF50)
// val PrimaryGreenDark = Color(0xFF388E3C)
// val PrimaryGreenLight = Color(0xFF81C784)

// val AccentBlue = Color(0xFF2196F3)
// val AccentBlueDark = Color(0xFF1976D2)
// val AccentBlueLight = Color(0xFF64B5F6)

// val ExpenseRed = Color(0xFFF44336)
// val IncomeGreen = Color(0xFF4CAF50)

// val BackgroundLight = Color(0xFFFAFAFA)
// val BackgroundDark = Color(0xFF121212)

// val SurfaceLight = Color(0xFFFFFFFF)
// val SurfaceDark = Color(0xFF1E1E1E)

