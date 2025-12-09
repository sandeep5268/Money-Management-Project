auth.ts - Authentication Middleware Guide

Purpose:
- Middleware to protect authenticated endpoints by validating access tokens and attaching authenticated user information to the request context.

Responsibilities:
- Extract access token (e.g., JWT) from `Authorization` header (`Bearer <token>`) or alternative sources (cookie).
- Verify token signature and expiration using the server's JWT secret or public key (for asymmetric tokens).
- Optionally validate token claims (audience, issuer, scope) to enforce proper access.
- Attach `req.user` (or equivalent) with user id and minimal claims for downstream controllers.
- If token invalid or absent, respond with `401 Unauthorized` and a structured JSON error message.

Design and behavior guidance:
- Keep middleware small: delegate decoding/verification to a shared `authService`.
- For performance, do not fetch heavy user profiles in middleware; instead attach a minimal user identifier and let controllers/services load full profile when needed.
- Log suspicious activity (invalid tokens) but avoid logging token content.

Suggested implementation responsibilities (descriptive):
- `function authenticate(req, res, next)`:
  - Read `Authorization` header; if missing return 401.
  - Parse and verify the JWT using `jsonwebtoken.verify(token, secretOrPublicKey)` (or equivalent library).
  - If valid, set `req.user = { id: payload.sub, roles: payload.roles }` and call `next()`.
  - If invalid, call `next(new UnauthorizedError('Invalid token'))`.

Security notes:
- Validate token expiry strictly and reject refresh tokens at authentication middleware; refresh handling should be a separate endpoint.
- Prefer short access token lifetimes with refresh token rotation.
- Protect sensitive endpoints further by checking required roles/permissions in additional middleware.

Testing:
- Unit test middleware by passing a mock request with a valid token (signed with test key) and asserting `req.user` is set.
- Test invalid, expired, and malformed tokens produce 401 responses.

Integration:
- Use the same `authService` for token creation and verification to keep algorithms and keys centralized.

This file is a descriptive guide for implementing authentication middleware in TypeScript.
/**
 * Auth Middleware - JWT Token Verification
 * 
 * Verifies JWT token from request headers
 * Decodes token and attaches user info to request object
 * Rejects requests with missing or invalid tokens
 * 
 * Usage: app.use(authMiddleware) or router.use(authMiddleware)
 */

import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';

// Extend Express Request type to include user info
declare global {
  namespace Express {
    interface Request {
      userId?: string;
      userEmail?: string;
    }
  }
}

export const authMiddleware = (req: Request, res: Response, next: NextFunction) => {
  try {
    // Extract token from Authorization header
    // Expected format: "Bearer <token>"
    const authHeader = req.headers.authorization;
    
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ error: 'Missing or invalid authorization token' });
    }
    
    // Extract token from "Bearer <token>"
    const token = authHeader.substring(7);
    
    // Verify and decode token using secret key
    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'your-secret-key') as {
      userId: string;
      email: string;
    };
    
    // Attach user info to request for use in route handlers
    req.userId = decoded.userId;
    req.userEmail = decoded.email;
    
    // Continue to next middleware/handler
    next();
  } catch (error) {
    // Token verification failed or expired
    res.status(401).json({ error: 'Invalid or expired token' });
  }
};

/**
 * Optional middleware - Allows request to continue even without token
 * Useful for endpoints that can work with or without authentication
 */
export const optionalAuthMiddleware = (req: Request, res: Response, next: NextFunction) => {
  try {
    const authHeader = req.headers.authorization;
    
    if (authHeader && authHeader.startsWith('Bearer ')) {
      const token = authHeader.substring(7);
      const decoded = jwt.verify(token, process.env.JWT_SECRET || 'your-secret-key') as {
        userId: string;
        email: string;
      };
      
      req.userId = decoded.userId;
      req.userEmail = decoded.email;
    }
    
    next();
  } catch (error) {
    // Token is invalid but we don't reject, just continue without user info
    next();
  }
};
