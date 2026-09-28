import axios from 'axios';
import crypto from 'crypto';
import { env } from '../../config/env';
import { getFirestore, COLLECTIONS } from '../../config/firebase';
import { AliExpressTokenData, OAuthStateRecord } from '../../models/AliExpressToken';
import { AliExpressSignatureService } from './aliexpress.signature.service';
import { AliExpressTokenService } from './aliexpress.token.service';
import { AliExpressApiError, BadRequestError } from '../../utils/errors';
import { logger } from '../../utils/logger';

export class AliExpressOAuthService {
  private static inMemoryOAuthStates = new Map<string, OAuthStateRecord>();

  /**
   * Generates a cryptographically random OAuth state, stores it with 10-minute expiry, and returns it.
   */
  public static async generateAndSaveState(): Promise<string> {
    const state = crypto.randomBytes(32).toString('hex');
    const now = Date.now();
    const expiresAt = now + 10 * 60 * 1000; // 10 minutes

    const record: OAuthStateRecord = {
      state,
      createdAt: now,
      expiresAt,
    };

    const db = getFirestore();
    if (db) {
      try {
        await db.collection(COLLECTIONS.OAUTH_STATES).doc(state).set(record);
      } catch (err) {
        logger.error('Failed to save OAuth state to Firestore:', err);
        if (env.NODE_ENV === 'production') {
          throw new Error('[FATAL] Shared OAuth state storage in Firestore failed in production.');
        }
        this.inMemoryOAuthStates.set(state, record);
      }
    } else {
      if (env.NODE_ENV === 'production') {
        throw new Error('[FATAL] Firestore database connection required for shared OAuth state in production.');
      }
      this.inMemoryOAuthStates.set(state, record);
    }

    return state;
  }

  /**
   * Verifies an incoming OAuth state, ensures it is unexpired, and consumes (deletes) it to prevent replay attacks.
   */
  public static async verifyAndConsumeState(state: string): Promise<boolean> {
    if (!state || typeof state !== 'string') {
      return false;
    }

    const db = getFirestore();
    let record: OAuthStateRecord | undefined = undefined;

    if (db) {
      try {
        const docRef = db.collection(COLLECTIONS.OAUTH_STATES).doc(state);
        const doc = await docRef.get();
        if (doc.exists) {
          record = doc.data() as OAuthStateRecord;
          // Consume immediately
          await docRef.delete();
        }
      } catch (err) {
        logger.error('Failed to verify state in Firestore:', err);
      }
    }

    if (!record && this.inMemoryOAuthStates.has(state)) {
      record = this.inMemoryOAuthStates.get(state);
      this.inMemoryOAuthStates.delete(state);
    }

    if (!record) {
      return false;
    }

    if (Date.now() > record.expiresAt) {
      logger.warn('Expired OAuth state attempt rejected.');
      return false;
    }

    return true;
  }

  /**
   * Generates the official AliExpress OAuth authorization URL with secure state parameter
   */
  public static async getAuthorizationUrl(): Promise<{ authUrl: string; state: string }> {
    const appKey = env.ALIEXPRESS_APP_KEY;
    const redirectUri = env.ALIEXPRESS_CALLBACK_URL;

    if (!appKey) {
      throw new BadRequestError('ALIEXPRESS_APP_KEY is not configured in backend environment.');
    }

    const state = await this.generateAndSaveState();

    const params = new URLSearchParams({
      response_type: 'code',
      force_auth: 'true',
      client_id: appKey,
      redirect_uri: redirectUri,
      state: state,
    });

    const authUrl = `${env.ALIEXPRESS_OAUTH_URL}?${params.toString()}`;
    return { authUrl, state };
  }

  /**
   * Exchanges authorization code for AliExpress access and refresh tokens using /auth/token/create
   */
  public static async exchangeCodeForToken(code: string, uuid?: string): Promise<AliExpressTokenData> {
    if (!code) {
      throw new BadRequestError('OAuth authorization code is required.');
    }

    const appKey = env.ALIEXPRESS_APP_KEY;
    const appSecret = env.ALIEXPRESS_APP_SECRET;

    if (!appKey || !appSecret) {
      throw new BadRequestError('AliExpress App Key and App Secret must be configured.');
    }

    const timestamp = Date.now().toString();
    const requestParams: Record<string, string> = {
      code,
      app_key: appKey,
      timestamp,
      sign_method: 'sha256',
    };

    if (uuid) {
      requestParams.uuid = uuid;
    }

    const sign = AliExpressSignatureService.generateSignature(requestParams, {
      appSecret,
      apiPath: '/auth/token/create',
    });

    requestParams.sign = sign;

    const tokenEndpoint = `${env.ALIEXPRESS_API_BASE_URL.replace(/\/sync$/, '')}/rest/auth/token/create`;

    try {
      logger.info('Exchanging OAuth code for token with AliExpress Open Platform...');
      const response = await axios.post(tokenEndpoint, new URLSearchParams(requestParams), {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8' },
        timeout: 10000,
      });

      const data = response.data;

      if (data.code && data.code !== '0' && data.code !== 0) {
        logger.error(`AliExpress token creation failed. Error Code: ${data.code}`);
        throw new AliExpressApiError(data.message || 'Failed to generate token from code', String(data.code), data.sub_code);
      }

      const tokenData: AliExpressTokenData = {
        access_token: data.access_token,
        refresh_token: data.refresh_token,
        expires_in: parseInt(data.expires_in || '2592000', 10),
        refresh_expires_in: parseInt(data.refresh_expires_in || '2592000', 10),
        expire_time: data.expire_time ? parseInt(data.expire_time, 10) : Date.now() + parseInt(data.expires_in || '2592000', 10) * 1000,
        refresh_token_valid_time: data.refresh_token_valid_time ? parseInt(data.refresh_token_valid_time, 10) : Date.now() + 2592000 * 1000,
        account_id: data.account_id || data.user_id,
        user_id: data.user_id,
        user_nick: data.user_nick,
        account: data.account,
        locale: data.locale,
        sp: data.sp,
        updatedAt: Date.now(),
      };

      await AliExpressTokenService.saveToken(tokenData);
      logger.info('OAuth exchange completed successfully and token stored securely.');
      return tokenData;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        logger.error('HTTP Error in AliExpress token exchange:', error.response?.data || error.message);
        throw new AliExpressApiError(
          `AliExpress HTTP Token Request Failed: ${error.message}`,
          error.response?.status?.toString()
        );
      }
      throw error;
    }
  }

  /**
   * Refreshes access token using refresh_token
   */
  public static async refreshToken(refreshTokenString: string): Promise<AliExpressTokenData> {
    const appKey = env.ALIEXPRESS_APP_KEY;
    const appSecret = env.ALIEXPRESS_APP_SECRET;

    if (!appKey || !appSecret) {
      throw new BadRequestError('AliExpress App Key and App Secret must be configured.');
    }

    const timestamp = Date.now().toString();
    const requestParams: Record<string, string> = {
      refresh_token: refreshTokenString,
      app_key: appKey,
      timestamp,
      sign_method: 'sha256',
    };

    const sign = AliExpressSignatureService.generateSignature(requestParams, {
      appSecret,
      apiPath: '/auth/token/refresh',
    });

    requestParams.sign = sign;

    const refreshEndpoint = `${env.ALIEXPRESS_API_BASE_URL.replace(/\/sync$/, '')}/rest/auth/token/refresh`;

    try {
      logger.info('Refreshing AliExpress OAuth access token...');
      const response = await axios.post(refreshEndpoint, new URLSearchParams(requestParams), {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8' },
        timeout: 10000,
      });

      const data = response.data;

      if (data.code && data.code !== '0' && data.code !== 0) {
        throw new AliExpressApiError(data.message || 'Token refresh failed', String(data.code));
      }

      const tokenData: AliExpressTokenData = {
        access_token: data.access_token,
        refresh_token: data.refresh_token || refreshTokenString,
        expires_in: parseInt(data.expires_in || '2592000', 10),
        expire_time: data.expire_time ? parseInt(data.expire_time, 10) : Date.now() + parseInt(data.expires_in || '2592000', 10) * 1000,
        account_id: data.account_id || data.user_id,
        user_id: data.user_id,
        user_nick: data.user_nick,
        updatedAt: Date.now(),
      };

      await AliExpressTokenService.saveToken(tokenData);
      logger.info('AliExpress access token refreshed successfully.');
      return tokenData;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new AliExpressApiError(`HTTP Refresh Token Request Failed: ${error.message}`);
      }
      throw error;
    }
  }
}
