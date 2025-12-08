/**
 * Auth Routes - User authentication endpoints
 * 
 * POST /api/auth/register - Create new user account
 * POST /api/auth/login - Authenticate user and return JWT token
 * POST /api/auth/refresh - Refresh expired JWT token
 * POST /api/auth/logout - Invalidate user session
 */

import express from 'express';
import { registerUser, loginUser, refreshToken, logoutUser } from '../controllers/authController';

const router = express.Router();

/**
 * User Registration Endpoint
 * Request: { email: string, password: string, name: string }
 * Response: { user: User, token: string }
 */
router.post('/register', registerUser);

/**
 * User Login Endpoint
 * Request: { email: string, password: string }
 * Response: { user: User, token: string }
 */
router.post('/login', loginUser);

/**
 * Token Refresh Endpoint
 * Request: { refreshToken: string }
 * Response: { token: string }
 */
router.post('/refresh', refreshToken);

/**
 * Logout Endpoint
 * Invalidates refresh token
 */
router.post('/logout', logoutUser);

export default router;
