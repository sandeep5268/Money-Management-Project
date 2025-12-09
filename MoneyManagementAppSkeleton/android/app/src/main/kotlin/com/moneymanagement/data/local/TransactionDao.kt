This file originally implemented the Room `TransactionDao` interface for the local database.

Replacement (documentation):

- Purpose:
    - Describe responsibilities and expected behavior of the `TransactionDao` without including implementation code.

- Responsibilities (high level):
    - Insert, update, and delete transaction records in the local SQLite database (Room).
    - Provide reactive, observable query results to the UI layer (Compose) using Kotlin `Flow` or `LiveData`.
    - Provide filtered queries such as by date range and by category, with stable ordering (e.g., newest first).
    - Provide aggregate queries used by analytics and dashboard features (total spending, total income, category sums, averages).

- Expected method surface (descriptive, not code):
    - `insertTransaction(transaction)` — insert a transaction, using a conflict strategy that replaces or updates an existing row with the same primary key.
    - `updateTransaction(transaction)` — update fields of an existing transaction record.
    - `deleteTransaction(transaction)` — delete a specific transaction record.
    - `getAllTransactions()` — returns a stream (Flow) of `List<TransactionEntity>` ordered by date descending for live UI updates.
    - `getTransactionsByDateRange(startDate, endDate)` — returns a stream (Flow) of transactions whose `date` lies between the provided timestamps.
    - `getTransactionsByCategory(category)` — returns a stream (Flow) of transactions filtered by category.
    - `getTotalSpending(startDate, endDate)` — returns a numeric total (Double) of expense-type transactions for a given range.
    - `getTotalIncome(startDate, endDate)` — returns a numeric total (Double) of income-type transactions for a given range.

- Implementation notes / best practices to follow when re-adding code:
    - Use `@Query` annotations in Room for parametrized queries rather than building SQL strings at runtime.
    - Prefer `Flow<List<TransactionEntity>>` for read queries to allow Compose to collect and automatically update UI on changes.
    - Use `suspend` functions for single-shot write operations (insert/update/delete) to run on `Dispatchers.IO` from ViewModel scope.
    - Use `OnConflictStrategy.REPLACE` or `OnConflictStrategy.IGNORE` depending on whether you want to preserve existing ids or overwrite.
    - Add appropriate indices on frequently queried columns (e.g., `date`, `category`, `type`) to optimize query performance.
    - Consider adding tests for DAO queries using an in-memory Room database and JUnit (verify ordering, filtering, aggregates).

- Data and type considerations:
    - `TransactionEntity` should include fields such as `id` (primary key), `amount` (Double/Long depending on currency representation), `date` (Long timestamp), `category` (String), `type` (`income`/`expense`), and optional `notes`.
    - Use a consistent epoch millis representation (`Long`) for dates to ease queries and indexing.
    - For currency, decide on integer minor-unit storage (e.g., cents as `Long`) to avoid floating point precision issues, or use `BigDecimal`-backed conversions.

- Concurrency and threading:
    - All write operations should be `suspend` and called in a coroutine scope (ViewModelScope with `Dispatchers.IO`).
    - Reads returning `Flow` bridge to UI using `collectAsState()` in Compose or `stateIn`/`asLiveData` when needed.

- Testing guidance:
    - Write unit tests that create an in-memory Room database and the DAO; insert sample rows and assert query results and aggregate functions.
    - Test boundary cases: empty result sets, overlapping date ranges, categories with no transactions, and large numbers for sums.

- Migration & schema evolution:
    - When changing fields on `TransactionEntity`, add Room `Migration` objects to preserve user data during upgrades.
    - Use `exportSchema = true` on Room database for schema version tracking.

- Security & privacy:
    - Never store sensitive PII in plain text fields; for any personal notes consider encryption at rest if required by the product's privacy policy.
    - Limit logging of transaction contents in debug logs; omit personal identifiers.

- Offline sync considerations:
    - When integrating with remote sync, keep local DAO simple and push conflict resolution into the repository layer where network and local states are reconciled.
    - Store a sync state or `lastModified` timestamp per transaction to help with two-way sync conflict resolution.

This document replaces the previous Kotlin source and intentionally contains descriptive guidance only. If you want, I can generate a non-compiling `.md` sidecar file instead (recommended) or re-generate the DAO code stubs following these best practices.
