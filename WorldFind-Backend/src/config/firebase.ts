import * as admin from 'firebase-admin';
import { env } from './env';
import { logger } from '../utils/logger';

let db: admin.firestore.Firestore | null = null;
let firebaseInitialized = false;

export function initializeFirebase(): { db: admin.firestore.Firestore | null; isInitialized: boolean } {
  if (firebaseInitialized) {
    return { db, isInitialized: firebaseInitialized };
  }

  const projectId = env.FIREBASE_PROJECT_ID;
  const clientEmail = env.FIREBASE_CLIENT_EMAIL;
  let privateKey = env.FIREBASE_PRIVATE_KEY;

  if (projectId && clientEmail && privateKey) {
    try {
      privateKey = privateKey.replace(/\\n/g, '\n');

      if (!admin.apps.length) {
        admin.initializeApp({
          credential: admin.credential.cert({
            projectId,
            clientEmail,
            privateKey,
          }),
        });
      }

      db = admin.firestore();
      firebaseInitialized = true;
      logger.info(`Firebase Admin initialized successfully for project: ${projectId}`);
    } catch (error) {
      logger.error('Failed to initialize Firebase Admin SDK:', error);
      db = null;
      firebaseInitialized = false;
      if (env.NODE_ENV === 'production') {
        throw new Error('[FATAL] Failed to initialize Firebase Admin SDK in production mode.');
      }
    }
  } else {
    if (env.NODE_ENV === 'production') {
      throw new Error('[FATAL] Firebase Admin credentials missing in production environment.');
    }
    logger.warn('Firebase Admin credentials missing in environment variables. Operating in non-persistent/in-memory mode for development/testing.');
    db = null;
    firebaseInitialized = false;
  }

  return { db, isInitialized: firebaseInitialized };
}

export function getFirestore(): admin.firestore.Firestore | null {
  if (!firebaseInitialized) {
    initializeFirebase();
  }
  return db;
}

export function getFirestoreDb(): admin.firestore.Firestore | null {
  return getFirestore();
}

export function getFirebaseAdminAuth(): admin.auth.Auth | null {
  if (!firebaseInitialized) {
    initializeFirebase();
  }
  return firebaseInitialized && admin.apps.length ? admin.auth() : null;
}

export const COLLECTIONS = {
  USERS: 'users',
  PRODUCTS: 'products',
  ORDERS: 'orders',
  PAYMENTS: 'payments',
  ADDRESSES: 'addresses',
  ALIEXPRESS_TOKENS: 'aliExpressTokens',
  OAUTH_STATES: 'oauthStates',
  ALIEXPRESS_WEBHOOK_EVENTS: 'aliexpress_webhook_events',
} as const;
