TransactionEntity.kt - Data Model Guide

Purpose:
- Defines the local persistence model for a financial transaction stored in Room database.
- Contains the canonical fields required by the app to show transactions, perform aggregation, and sync with backend.

Recommended fields and types (descriptive names and rationale):
- `id` (Long / Int) — Primary key (prefer `Long` with `autoGenerate = true`). Uniquely identifies a transaction locally.
- `remoteId` (String?) — Optional ID from the backend; used to correlate local and remote records during sync.
- `amountMinor` (Long) — Amount stored in the minor currency unit (e.g., cents). Storing as integer avoids floating point precision issues.
- `currency` (String) — ISO currency code (e.g., `USD`, `INR`). Useful for multi-currency support.
- `date` (Long) — Epoch milliseconds (UTC) for the transaction time. Use `Long` for efficient queries and indexing.
- `category` (String) — Category identifier (e.g., `groceries`, `salary`). Keep it normalized to avoid typos.
- `type` (String) — Transaction type, e.g., `income` or `expense`. Consider an enum-like sealed class at app-layer.
- `notes` (String?) — Optional user notes.
- `createdAt` (Long) — Timestamp created locally or by backend.
- `updatedAt` (Long) — Timestamp of last update — helpful for conflict resolution during sync.
- `isDeleted` (Boolean) — Soft-delete flag used to keep tombstones for sync instead of hard-delete immediately.

Design and schema guidance:
- Use `exportSchema = true` in Room database for version tracking.
- Index frequently queried columns: `date`, `category`, `type`, and `remoteId`.
- Prefer storing amounts in integer minor-units to avoid Double precision errors.
- Keep the entity free of business logic — mapping and conversions should live in DTOs or domain models.

Mapping recommendations:
- Provide converters between `TransactionEntity` and domain model `Transaction` (value objects) which contain typed fields (e.g., `Money` wrapper) and formatting helpers.
- Implement `@TypeConverters` if any complex fields (e.g., lists, maps) are stored as JSON.

Migration guidance:
- When adding or removing columns, write Room `Migration` objects to migrate existing user data without loss.
- Add unit tests for migrations using an in-memory DB and schema snapshots.

Security & privacy:
- Treat `notes` and any personal fields as potentially sensitive; avoid logging them.
- If storing highly sensitive information, consider encryption at rest with SQLCipher or using Jetpack Security wrappers.

Testing:
- Test entity read/write round-trips using an in-memory DB.
- Validate mapping to/from network models and domain models.

Example developer tasks (to implement in Kotlin):
1. Create `@Entity(tableName = "transactions")` data class with the recommended fields.
2. Add `@Index` annotations on `date` and `category`.
3. Add `@TypeConverters` if needed.

This file contains schema and implementation guidance for `TransactionEntity`. Implement the actual Kotlin code following these recommendations.
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
