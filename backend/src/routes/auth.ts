auth.ts - Auth Routes Guide

Purpose:
- Define API endpoints for authentication: sign up, sign in (login), token refresh, sign out, and optionally password reset.

Recommended endpoints and responsibilities (descriptive):
- `POST /api/auth/signup` — Create a new user account.
  - Validate the request body (email, password, optional profile fields).
  - Hash passwords (bcrypt/argon2) before storing.
  - Return minimal user profile and optionally an access token.

- `POST /api/auth/login` — Authenticate a user.
  - Validate credentials and return an access token + refresh token pair on success.
  - Token should be signed securely and include minimal claims (user id, roles), with short expiration for access token.

- `POST /api/auth/refresh` — Exchange refresh token for a new access token.
  - Validate refresh token and optionally rotate it (issue new refresh token and revoke old one).

- `POST /api/auth/logout` — Revoke tokens and clear session.

- `POST /api/auth/password-reset` (optional) — Start password reset flow via email.

Design and validation guidance:
- Validate all incoming data with a schema validator (Zod, Joi) and return clear validation errors.
- Use HTTPS and `SameSite` secure cookie flags if tokens are stored in cookies.

Security notes:
- Store password hashes only; never store plaintext passwords.
- Implement refresh token revocation strategy (DB-stored tokens or a revocation list) to support logout and compromise handling.
- Rate-limit login endpoint to protect against brute-force attempts.

Testing:
- Add tests for success and failure cases: bad credentials, missing fields, expired refresh token, and token rotation.

Integration:
- Use `authService` for business logic (create user, verify password, issue tokens) and keep routes thin.

This is a descriptive guide for implementing `auth` routes. Implement the TypeScript router using Express Router or chosen framework.
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
