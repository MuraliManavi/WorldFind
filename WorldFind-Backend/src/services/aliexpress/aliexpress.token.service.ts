import crypto from 'crypto';
import { env } from '../../config/env';
import { getFirestore, COLLECTIONS } from '../../config/firebase';
import { AliExpressTokenData, EncryptedTokenRecord } from '../../models/AliExpressToken';
import { logger } from '../../utils/logger';

const ALGORITHM = 'aes-256-gcm';
const DEFAULT_DOC_ID = 'main_account';

export class AliExpressTokenService {
  private static inMemoryTokenCache: AliExpressTokenData | null = null;

  /**
   * Derive a 32-byte key buffer from configured TOKEN_ENCRYPTION_KEY
   */
  public static getEncryptionKeyBuffer(): Buffer {
    const rawKey = env.TOKEN_ENCRYPTION_KEY;

    if (!rawKey) {
      if (env.NODE_ENV === 'production') {
        throw new Error('[FATAL] TOKEN_ENCRYPTION_KEY is required in production environment.');
      }
      return crypto.createHash('sha256').update('worldfind_dev_encryption_seed_2025').digest();
    }

    if (rawKey.length === 64 && /^[0-9a-fA-F]{64}$/.test(rawKey)) {
      return Buffer.from(rawKey, 'hex');
    }

    return crypto.createHash('sha256').update(rawKey).digest();
  }

  /**
   * Encrypt plaintext string using AES-256-GCM with fresh IV
   */
  public static encrypt(text: string): { encrypted: string; iv: string; authTag: string } {
    const key = this.getEncryptionKeyBuffer();
    const iv = crypto.randomBytes(12);
    const cipher = crypto.createCipheriv(ALGORITHM, key, iv);

    let encrypted = cipher.update(text, 'utf8', 'hex');
    encrypted += cipher.final('hex');
    const authTag = cipher.getAuthTag().toString('hex');

    return {
      encrypted,
      iv: iv.toString('hex'),
      authTag,
    };
  }

  /**
   * Decrypt AES-256-GCM ciphertext using provided IV and authTag
   */
  public static decrypt(encrypted: string, ivHex: string, authTagHex: string): string {
    const key = this.getEncryptionKeyBuffer();
    const iv = Buffer.from(ivHex, 'hex');
    const authTag = Buffer.from(authTagHex, 'hex');
    const decipher = crypto.createDecipheriv(ALGORITHM, key, iv);

    decipher.setAuthTag(authTag);
    let decrypted = decipher.update(encrypted, 'hex', 'utf8');
    decrypted += decipher.final('utf8');

    return decrypted;
  }

  /**
   * Save token response from AliExpress to Firestore encrypted with independent IVs.
   * In production, failures throw an error to prevent false "Authorization successful" responses.
   */
  public static async saveToken(tokenData: AliExpressTokenData): Promise<void> {
    const now = Date.now();
    tokenData.updatedAt = now;

    this.inMemoryTokenCache = { ...tokenData };

    const db = getFirestore();
    if (!db) {
      if (env.NODE_ENV === 'production') {
        throw new Error('[FATAL] Firestore database connection required for token persistence in production.');
      }
      logger.info('Firestore is not available. Token stored in memory cache only for development/testing.');
      return;
    }

    try {
      const encryptedAccess = this.encrypt(tokenData.access_token);
      const encryptedRefresh = this.encrypt(tokenData.refresh_token);

      const record: EncryptedTokenRecord = {
        encryptedAccessToken: encryptedAccess.encrypted,
        accessTokenIv: encryptedAccess.iv,
        accessTokenAuthTag: encryptedAccess.authTag,

        encryptedRefreshToken: encryptedRefresh.encrypted,
        refreshTokenIv: encryptedRefresh.iv,
        refreshTokenAuthTag: encryptedRefresh.authTag,

        expireTime: tokenData.expire_time || now + tokenData.expires_in * 1000,
        refreshTokenValidTime:
          tokenData.refresh_token_valid_time ||
          now + (tokenData.refresh_expires_in || 30 * 24 * 3600) * 1000,
        accountId: tokenData.account_id || tokenData.user_id,
        updatedAt: now,
      };

      await db.collection(COLLECTIONS.ALIEXPRESS_TOKENS).doc(DEFAULT_DOC_ID).set(record);
      logger.info('AliExpress OAuth token encrypted and persisted to Firestore successfully.');
    } catch (error) {
      logger.error('Failed to store token in Firestore:', error);
      if (env.NODE_ENV === 'production') {
        throw error;
      }
    }
  }

  /**
   * Retrieve active token. Firestore is the sole production source of truth.
   */
  public static async getToken(): Promise<AliExpressTokenData | null> {
    if (this.inMemoryTokenCache) {
      return this.inMemoryTokenCache;
    }

    const db = getFirestore();
    if (db) {
      try {
        const doc = await db.collection(COLLECTIONS.ALIEXPRESS_TOKENS).doc(DEFAULT_DOC_ID).get();
        if (doc.exists) {
          const record = doc.data() as EncryptedTokenRecord;
          const decryptedAccessToken = this.decrypt(
            record.encryptedAccessToken,
            record.accessTokenIv,
            record.accessTokenAuthTag
          );
          const decryptedRefreshToken = this.decrypt(
            record.encryptedRefreshToken,
            record.refreshTokenIv,
            record.refreshTokenAuthTag
          );

          const tokenData: AliExpressTokenData = {
            access_token: decryptedAccessToken,
            refresh_token: decryptedRefreshToken,
            expires_in: Math.max(0, Math.floor((record.expireTime - Date.now()) / 1000)),
            expire_time: record.expireTime,
            updatedAt: record.updatedAt,
            account_id: record.accountId,
          };

          this.inMemoryTokenCache = tokenData;
          return tokenData;
        }
      } catch (error) {
        logger.error('Failed to read or decrypt token from Firestore:', error);
      }
    }

    // Static environment tokens permitted ONLY in dev/test
    if (env.NODE_ENV !== 'production' && env.ALIEXPRESS_ACCESS_TOKEN) {
      logger.info('Using static ALIEXPRESS_ACCESS_TOKEN from environment for development/testing.');
      const staticTokenData: AliExpressTokenData = {
        access_token: env.ALIEXPRESS_ACCESS_TOKEN,
        refresh_token: env.ALIEXPRESS_REFRESH_TOKEN || '',
        expires_in: 3600 * 24 * 30,
        expire_time: Date.now() + 3600 * 24 * 30 * 1000,
        updatedAt: Date.now(),
      };
      this.inMemoryTokenCache = staticTokenData;
      return staticTokenData;
    }

    return null;
  }

  public static isTokenExpired(tokenData: AliExpressTokenData, bufferMs = 5 * 60 * 1000): boolean {
    if (!tokenData.expire_time) return false;
    return Date.now() + bufferMs >= tokenData.expire_time;
  }

  public static clearCache(): void {
    this.inMemoryTokenCache = null;
  }
}
