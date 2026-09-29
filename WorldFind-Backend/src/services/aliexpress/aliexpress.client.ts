import axios, { AxiosInstance } from 'axios';
import { env } from '../../config/env';
import { AliExpressSignatureService, SignMethod } from './aliexpress.signature.service';
import { AliExpressTokenService } from './aliexpress.token.service';
import { AliExpressOAuthService } from './aliexpress.oauth.service';
import { AliExpressApiError, BadRequestError } from '../../utils/errors';
import { logger } from '../../utils/logger';

export interface AliExpressProtocolConfig {
  baseUrl: string;
  signMethod: SignMethod;
  apiVersion: string;
  format: string;
  timestampFormat: 'timestamp' | 'datetime';
}

export interface AliExpressRequestOptions {
  method: string; // e.g. 'aliexpress.affiliate.product.query'
  params?: Record<string, string | number | boolean | undefined | null>;
  requireSession?: boolean;
  protocolConfig?: Partial<AliExpressProtocolConfig>;
}

export class AliExpressClient {
  private axiosClient: AxiosInstance;

  constructor() {
    this.axiosClient = axios.create({
      baseURL: env.ALIEXPRESS_API_BASE_URL,
      timeout: 15000,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8',
        'User-Agent': 'WorldFind-Backend/1.0.0',
      },
    });
  }

  /**
   * Executes a signed call to AliExpress Open Platform
   */
  public async execute<T = unknown>(options: AliExpressRequestOptions): Promise<T> {
    const { method, params = {}, requireSession = false, protocolConfig } = options;

    const config: AliExpressProtocolConfig = {
      baseUrl: env.ALIEXPRESS_API_BASE_URL,
      signMethod: 'sha256',
      apiVersion: '2.0',
      format: 'json',
      timestampFormat: 'datetime',
      ...protocolConfig,
    };

    const appKey = env.ALIEXPRESS_APP_KEY;
    const appSecret = env.ALIEXPRESS_APP_SECRET;

    if (!appKey || !appSecret || appKey === 'your_app_key_here' || appSecret === 'your_app_secret_here') {
      throw new BadRequestError('ALIEXPRESS_APP_KEY and ALIEXPRESS_APP_SECRET must be configured in Render Environment Variables.');
    }

    // Resolve active token if required by endpoint
    let sessionToken: string | undefined = undefined;
    if (requireSession) {
      const tokenData = await AliExpressTokenService.getToken();
      if (tokenData) {
        if (AliExpressTokenService.isTokenExpired(tokenData) && tokenData.refresh_token) {
          logger.info('Access token is expiring or expired. Attempting token refresh before API call...');
          try {
            const refreshedToken = await AliExpressOAuthService.refreshToken(tokenData.refresh_token);
            sessionToken = refreshedToken.access_token;
          } catch (err) {
            logger.warn('Failed to auto-refresh token, proceeding with current token:', err);
            sessionToken = tokenData.access_token;
          }
        } else {
          sessionToken = tokenData.access_token;
        }
      }

      if (!sessionToken) {
        throw new BadRequestError('This AliExpress API endpoint requires an active authorized session. Please complete OAuth flow.');
      }
    }

    // Build system parameters
    const timestampStr = config.timestampFormat === 'datetime'
      ? AliExpressSignatureService.getFormattedTimestamp()
      : Date.now().toString();

    const systemParams: Record<string, string> = {
      app_key: appKey,
      method: method,
      timestamp: timestampStr,
      format: config.format,
      v: config.apiVersion,
      sign_method: config.signMethod,
    };

    if (sessionToken) {
      systemParams.session = sessionToken;
    }

    // Merge system params and business parameters
    const mergedParams: Record<string, string> = { ...systemParams };

    for (const [key, value] of Object.entries(params)) {
      if (value !== undefined && value !== null && value !== '') {
        mergedParams[key] = String(value);
      }
    }

    // Calculate signature (excludes 'sign' key)
    const sign = AliExpressSignatureService.generateSignature(mergedParams, {
      appSecret,
      signMethod: config.signMethod,
    });

    mergedParams.sign = sign;

    const startTime = Date.now();

    try {
      logger.info(`[AliExpressClient] Executing signed API request:`, {
        method,
        baseUrl: config.baseUrl,
        signMethod: config.signMethod,
        timestamp: timestampStr,
        signedParamKeys: Object.keys(mergedParams).filter((k) => k !== 'sign').sort(),
      });

      const formParams = new URLSearchParams(mergedParams);
      const response = await this.axiosClient.post(config.baseUrl, formParams.toString());
      const data = response.data;
      const duration = Date.now() - startTime;

      logger.info(`[AliExpressClient] Response received for method=${method} in ${duration}ms`);

      if (data.error_response) {
        const err = data.error_response;
        logger.error(`AliExpress Open Platform Error: method=${method}, code=${err.code}, msg=${err.msg}`);

        let safeMsg = err.msg || 'AliExpress API Call Failed';
        if (err.code === 'IncompleteSignature' || (err.msg && err.msg.includes('signature'))) {
          safeMsg = 'The request signature does not conform to platform standards. Please verify your ALIEXPRESS_APP_KEY and ALIEXPRESS_APP_SECRET in Render Environment Variables.';
        }

        throw new AliExpressApiError(
          safeMsg,
          String(err.code || 'UNKNOWN'),
          err.sub_code
        );
      }

      const responseKey = `${method.replace(/\./g, '_')}_response`;
      const resultPayload = data[responseKey] || data;

      return resultPayload as T;
    } catch (error) {
      if (error instanceof AliExpressApiError) {
        throw error;
      }

      if (axios.isAxiosError(error)) {
        logger.error(`HTTP request to AliExpress failed: ${error.message}`);
        const userMsg = error.code === 'ECONNABORTED' || error.message.includes('timeout')
          ? 'Connection to AliExpress Open Platform timed out. Please check network connectivity and backend credentials.'
          : `Failed to connect to AliExpress Open Platform: ${error.message}`;
        throw new AliExpressApiError(userMsg, error.response?.status?.toString() || 'TIMEOUT');
      }

      throw error;
    }
  }
}

export const aliExpressClient = new AliExpressClient();
