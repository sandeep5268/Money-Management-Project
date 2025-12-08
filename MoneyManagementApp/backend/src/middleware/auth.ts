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
