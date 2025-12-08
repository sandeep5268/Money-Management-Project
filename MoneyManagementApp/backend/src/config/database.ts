database.ts - Database Configuration & Connection Guide

Purpose:
- Centralize database connection logic and configuration for the backend. Provide a single exported function to initialize and return the database client/ORM instance.

Responsibilities:
- Read DB connection details from environment variables (host, port, database name, username, password) or from a configured connection URL.
- Initialize chosen ORM/driver (Prisma, TypeORM, Sequelize, or native `pg` client for PostgreSQL).
- Apply migrations or ensure schema is up-to-date (migrations via Prisma Migrate, TypeORM migrations, or Flyway/liquibase externally).
- Export utility functions and the connected client/ORM instance for use in controllers/services.

Design choices and suggestions:
- Use Prisma for a modern developer experience with TypeScript types; otherwise use TypeORM or Sequelize depending on team familiarity.
- Keep migrations out-of-band in CI/CD and avoid running destructive migrations automatically in production.
- Handle connection errors explicitly and retry with exponential backoff at startup to handle transient DB availability.

Example exports (descriptive):
- `async function connectDatabase(): Promise<DbClient>` — connects to DB and resolves when ready.
- `function getDbClient(): DbClient` — returns the shared client instance.
- Optionally, `async function closeDatabase()` to gracefully shut down connections during termination.

Security & operational notes:
- Do not commit credentials; use environment variables and secret managers.
- Ensure DB user has least privilege — only required permissions for schema changes and app operations.

Testing:
- For tests, export a helper to create an in-memory or disposable test database (e.g., SQLite memory or test Postgres container using Testcontainers).

This file contains guidance only. Implement the actual TypeScript connection code according to chosen ORM and environment.
/**
 * Database Configuration
 * 
 * PostgreSQL connection setup using pg library
 * Manages connection pool and executes queries
 * Handles database migrations and schema initialization
 */

import { Pool, QueryResult } from 'pg';
import dotenv from 'dotenv';

dotenv.config();

// Create connection pool with environment variables
export const pool = new Pool({
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT || '5432'),
  database: process.env.DB_NAME || 'money_management',
  user: process.env.DB_USER || 'postgres',
  password: process.env.DB_PASSWORD || 'password',
  // Limit connections in pool
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
});

/**
 * Connect to database and initialize schema
 * Called when server starts
 */
export const connectDatabase = async () => {
  try {
    // Test connection
    const result = await pool.query('SELECT NOW()');
    console.log('Database connected at:', result.rows[0]);
    
    // Initialize database schema (create tables if not exist)
    await initializeSchema();
  } catch (error) {
    console.error('Database connection failed:', error);
    throw error;
  }
};

/**
 * Initialize database schema
 * Creates tables for users and transactions if they don't exist
 */
const initializeSchema = async () => {
  try {
    // Create users table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS users (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        email VARCHAR(255) UNIQUE NOT NULL,
        password VARCHAR(255) NOT NULL,
        name VARCHAR(255) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // Create transactions table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS transactions (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        amount DECIMAL(10, 2) NOT NULL,
        type VARCHAR(20) NOT NULL CHECK (type IN ('income', 'expense')),
        category VARCHAR(100) NOT NULL,
        description TEXT,
        date TIMESTAMP NOT NULL,
        source VARCHAR(50) DEFAULT 'manual',
        synced BOOLEAN DEFAULT false,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_user_date (user_id, date),
        INDEX idx_category (user_id, category)
      )
    `);

    // Create refresh tokens table for session management
    await pool.query(`
      CREATE TABLE IF NOT EXISTS refresh_tokens (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        token VARCHAR(500) NOT NULL,
        expires_at TIMESTAMP NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    console.log('Database schema initialized');
  } catch (error) {
    console.error('Schema initialization error:', error);
    throw error;
  }
};

/**
 * Execute a query with parameters
 * Prevents SQL injection through parameterized queries
 */
export const query = async (text: string, params?: any[]): Promise<QueryResult> => {
  return pool.query(text, params);
};

/**
 * Close database connection (called on server shutdown)
 */
export const closeDatabase = async () => {
  await pool.end();
};
