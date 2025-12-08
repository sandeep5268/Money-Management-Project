/**
 * Error Handler Middleware
 * 
 * Centralized error handling for all API endpoints
 * Catches and formats all application errors
 * Returns consistent error responses to client
 * Logs errors for debugging
 */

import { Request, Response, NextFunction } from 'express';

// Custom error class for application errors
export class AppError extends Error {
  statusCode: number;
  
  constructor(message: string, statusCode: number) {
    super(message);
    this.statusCode = statusCode;
  }
}

/**
 * Global error handler middleware
 * 
 * Catches errors thrown by route handlers and responds appropriately
 * Logs errors to console for debugging
 * Returns safe error messages to client (no stack traces in production)
 */
export const errorHandler = (
  error: Error | AppError,
  req: Request,
  res: Response,
  next: NextFunction
) => {
  // Determine status code
  const statusCode = error instanceof AppError ? error.statusCode : 500;
  
  // Log error with context
  console.error(`[${new Date().toISOString()}] ${statusCode} - ${error.message}`);
  console.error('Stack:', error.stack);
  
  // Return error response
  res.status(statusCode).json({
    error: {
      message: error.message,
      statusCode,
      timestamp: new Date().toISOString(),
      // Only include stack trace in development
      ...(process.env.NODE_ENV === 'development' && { stack: error.stack })
    }
  });
};

/**
 * Async error wrapper
 * Wraps async route handlers to catch errors automatically
 * 
 * Usage: router.post('/', asyncHandler(async (req, res) => { ... }))
 */
export const asyncHandler = (fn: Function) => {
  return (req: Request, res: Response, next: NextFunction) => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };
};
