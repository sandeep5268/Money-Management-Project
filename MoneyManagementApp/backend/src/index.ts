/**
 * Main Entry Point - Money Management Backend API
 * 
 * Initializes Express server with:
 * - Database connection
 * - Middleware setup (CORS, Authentication, Error handling)
 * - Route mounting
 * - Environment configuration
 * 
 * Runs on: http://localhost:3000
 */

import express from 'express';
import dotenv from 'dotenv';
import cors from 'cors';
import { connectDatabase } from './config/database';
import authRoutes from './routes/auth';
import transactionRoutes from './routes/transaction';
import { errorHandler } from './middleware/errorHandler';
import { authMiddleware } from './middleware/auth';

// Load environment variables from .env file
dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

// ============ MIDDLEWARE ============

// Enable CORS for Android client communication
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || '*',
  credentials: true
}));

// Parse incoming JSON requests
app.use(express.json());

// Parse URL-encoded request bodies
app.use(express.urlencoded({ extended: true }));

// ============ ROUTES ============

// Public routes (no authentication required)
app.use('/api/auth', authRoutes); // Login, Register, Verify OTP

// Protected routes (require JWT token)
app.use('/api/transactions', authMiddleware, transactionRoutes); // Create, Read, Update, Delete transactions

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'API is running', timestamp: new Date() });
});

// ============ ERROR HANDLING ============

// Catch 404 errors
app.use((req, res) => {
  res.status(404).json({ error: 'Route not found' });
});

// Global error handler
app.use(errorHandler);

// ============ DATABASE & SERVER ============

// Initialize database connection and start server
const start = async () => {
  try {
    // Connect to PostgreSQL database
    await connectDatabase();
    console.log('✓ Database connected');

    // Start listening on specified port
    app.listen(PORT, () => {
      console.log(`✓ Server running on http://localhost:${PORT}`);
    });
  } catch (error) {
    console.error('✗ Failed to start server:', error);
    process.exit(1);
  }
};

start();

export default app;
