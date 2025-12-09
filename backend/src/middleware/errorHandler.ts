errorHandler.ts - Centralized Error Handling Guide

Purpose:
- Provide consistent, centralized error handling for the backend API. Transform internal errors into safe, structured HTTP responses for clients.

Responsibilities:
- Catch synchronous and asynchronous errors from route handlers and middleware.
- Map common error types to HTTP status codes (e.g., validation -> 400, unauthorized -> 401, forbidden -> 403, not found -> 404, internal -> 500).
- Optionally hide internal error details in production while providing helpful error IDs and logs for troubleshooting.
- Ensure stack traces and sensitive internal data are not leaked to clients.

Design guidance:
- Use an error class hierarchy or typed error results (e.g., `BadRequestError`, `UnauthorizedError`, `NotFoundError`) to simplify mapping to HTTP codes.
- Implement a middleware function signature `function errorHandler(err, req, res, next)` that logs the error and sends a JSON response like:
  - `{ success: false, error: { code: 'AUTH_INVALID', message: 'Authentication failed' }, requestId: 'uuid' }`.
- Include a `requestId` in logs and responses to correlate client reports to server logs.

Security and operational notes:
- In production, return minimal error messages and encourage clients to contact support with `requestId` for detailed investigation.
- In development, return full error details to speed debugging (toggle via NODE_ENV).
- Log errors to a structured logging service (winston, pino) and forward critical errors to Sentry/Datadog.

Testing:
- Unit test error handler mapping by simulating route errors and asserting the returned HTTP code and JSON shape.

This file provides guidance on centralized error handling. Implement the TypeScript middleware according to your project's error model.
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
