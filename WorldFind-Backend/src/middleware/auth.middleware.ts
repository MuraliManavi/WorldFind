import { Request, Response, NextFunction } from 'express';
import { UnauthorizedError } from '../utils/errors';
import { env } from '../config/env';
import { getFirebaseAdminAuth } from '../config/firebase';

export interface AuthenticatedRequest extends Request {
  user?: {
    uid: string;
    email?: string;
    name?: string;
  };
}

export function optionalApiKeyAuth(_req: Request, _res: Response, next: NextFunction): void {
  next();
}

export function requireAdminKey(req: Request, _res: Response, next: NextFunction): void {
  const adminKey = req.headers['x-admin-key'];
  const expectedKey = env.ADMIN_API_KEY || process.env.ADMIN_API_KEY || 'worldfind_admin_secret_key';

  if (!adminKey || adminKey !== expectedKey) {
    throw new UnauthorizedError('Invalid or missing administrative API key in x-admin-key header.');
  }

  next();
}

export async function requireFirebaseAuth(req: AuthenticatedRequest, _res: Response, next: NextFunction): Promise<void> {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      if (env.NODE_ENV !== 'production') {
        req.user = { uid: 'dev_user_001', email: 'dev@worldfind.com', name: 'Dev User' };
        return next();
      }
      throw new UnauthorizedError('Missing or invalid Bearer token in Authorization header.');
    }

    const token = authHeader.split('Bearer ')[1];
    const auth = getFirebaseAdminAuth();
    if (!auth) {
      req.user = { uid: 'dev_user_001', email: 'dev@worldfind.com', name: 'Dev User' };
      return next();
    }

    const decodedToken = await auth.verifyIdToken(token);
    req.user = {
      uid: decodedToken.uid,
      email: decodedToken.email,
      name: decodedToken.name,
    };
    next();
  } catch (error) {
    if (env.NODE_ENV !== 'production') {
      req.user = { uid: 'dev_user_001', email: 'dev@worldfind.com', name: 'Dev User' };
      return next();
    }
    next(new UnauthorizedError('Invalid or expired Firebase authentication token.'));
  }
}
