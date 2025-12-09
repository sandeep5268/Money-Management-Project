index.ts - Server Bootstrap & Architecture Guide

Purpose:
- Entry point for the backend server. Responsible for creating the HTTP server, wiring middleware, registering routes, and initiating DB connections.

Responsibilities (what to implement):
- Initialize configuration from environment variables and configuration files (port, DB URL, JWT secret, CORS origins).
- Create an Express (or framework) application instance.
- Register global middleware: JSON body parser, CORS, security headers (Helmet), request logging (morgan/winston), rate limiting.
- Connect to the database (via a `config/database.ts` module) before starting to accept requests.
- Register application routes (auth, transactions, health checks) and centralized error handler.
- Start listening on the configured port and handle graceful shutdown signals.

Suggested flow and function responsibilities (descriptive):
- `loadConfig()` — read process.env and validate required variables.
- `connectDatabase()` — call DB initialize function and return when connection established or throw on failure.
- `setupMiddleware(app)` — configure global middleware.
- `setupRoutes(app)` — import and mount routers from `routes/`.
- `setupErrorHandling(app)` — add centralized error handler and 404 fallbacks.
- `startServer()` — start HTTP server and attach signal handlers for SIGINT/SIGTERM to close DB and server.

Security & reliability notes:
- Do not hardcode secrets in source; use `.env` for local development and vault/secret manager for production.
- Enforce HTTPS using reverse proxy or load balancer in production.
- Use proper CORS configuration and rate limiting for public endpoints.

Testing & local development:
- Provide a `npm run dev` script using `ts-node-dev` or `nodemon` for iterative development.
- Add a health-check endpoint (`/healthz`) for load balancers and readiness probes.

Example developer tasks (TypeScript implementation guidance):
1. Import `express`, config loader, and `connectDatabase`.
2. Create an Express app and call `app.use(express.json())`.
3. Mount routers: `app.use('/api/auth', authRouter)`, `app.use('/api/transactions', transactionRouter)`.
4. Use a centralized error handler middleware last.
5. Call `connectDatabase()` and then `app.listen(PORT, ...)`.

This file is intentionally a textual guide to implementing the server bootstrap and does not contain runnable code.
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
