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
