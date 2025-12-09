transaction.ts - Transaction Routes Guide

Purpose:
- Define API endpoints for creating, reading, updating, and deleting user transactions as well as aggregation endpoints used by dashboards.

Recommended endpoints and behaviors:
- `GET /api/transactions` — List transactions for authenticated user.
  - Support query parameters for pagination (`page`, `limit`), filtering (`startDate`, `endDate`, `category`), and sorting.

- `GET /api/transactions/:id` — Get a specific transaction by ID (must belong to the authenticated user).

- `POST /api/transactions` — Create a transaction.
  - Validate payload, store transaction, set `createdAt` and `updatedAt`. Return created resource with `id`.

- `PUT /api/transactions/:id` — Update a transaction.
  - Validate ownership, apply changes, update `updatedAt`.

- `DELETE /api/transactions/:id` — Delete a transaction (soft-delete recommended to support sync and undo).

- `GET /api/transactions/summary` — Aggregation endpoint to return totals by date range or category for dashboards.

Design notes:
- Keep controllers thin; delegate business logic (validation, owner checks, aggregation) to `transactionService`.
- Use pagination and limits to avoid returning large datasets.
- Validate user ownership on mutate endpoints to prevent horizontal privilege escalation.

Sync considerations:
- Include `updatedAt` and `remoteId` fields to help clients reconcile local changes (for two-way sync).

Security & validation:
- Authenticate requests using the `auth` middleware.
- Validate payloads with a schema validator and return consistent error shapes.

Testing:
- Add unit tests for each endpoint including edge cases (empty results, bad filters, unauthorized access).

This file is a guide for implementing transaction API routes in TypeScript.
/**
 * Transaction Routes - Financial transaction endpoints
 * 
 * All routes require JWT authentication (authMiddleware)
 * 
 * GET    /api/transactions           - Get all transactions for user
 * GET    /api/transactions/:id       - Get single transaction
 * POST   /api/transactions           - Create new transaction
 * PUT    /api/transactions/:id       - Update transaction
 * DELETE /api/transactions/:id       - Delete transaction
 * GET    /api/transactions/stats     - Get spending statistics
 * POST   /api/transactions/sync      - Sync SMS transactions from Android
 */

import express from 'express';
import {
  getTransactions,
  getTransactionById,
  createTransaction,
  updateTransaction,
  deleteTransaction,
  getStatistics,
  syncSMSTransactions
} from '../controllers/transactionController';

const router = express.Router();

/**
 * Get all transactions for authenticated user
 * Query params: ?month=2024-01, ?category=food, ?type=expense
 */
router.get('/', getTransactions);

/**
 * Get single transaction by ID
 */
router.get('/:id', getTransactionById);

/**
 * Create new transaction (manual entry)
 * Body: { amount, type, category, description, date }
 */
router.post('/', createTransaction);

/**
 * Update existing transaction
 */
router.put('/:id', updateTransaction);

/**
 * Delete transaction
 */
router.delete('/:id', deleteTransaction);

/**
 * Get spending statistics for a period
 * Query: ?startDate=1701000000&endDate=1703600000
 * Response: { totalIncome, totalExpense, byCategory, dailyBreakdown }
 */
router.get('/stats', getStatistics);

/**
 * Sync transactions from Android device (SMS parsing)
 * Used when Android app sends SMS-parsed transactions to sync
 * Body: { transactions: Transaction[] }
 */
router.post('/sync', syncSMSTransactions);

export default router;
