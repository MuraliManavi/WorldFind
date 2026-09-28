import { Request, Response, NextFunction } from 'express';
import { AliExpressOAuthService } from '../services/aliexpress/aliexpress.oauth.service';
import { AliExpressTokenService } from '../services/aliexpress/aliexpress.token.service';
import { AliExpressWebhookSignatureService } from '../services/aliexpress/aliexpress.webhook.signature.service';
import { AliExpressWebhookService } from '../services/aliexpress/aliexpress.webhook.service';
import { WebhookEventRecord, RawAliExpressWebhookPayload } from '../models/AliExpressWebhook';
import { BadRequestError, UnauthorizedError } from '../utils/errors';
import { env } from '../config/env';
import { logger } from '../utils/logger';

function escapeHtml(text: string): string {
  return String(text || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

export class AliExpressController {
  /**
   * GET /api/aliexpress/health
   * Safe backend health/test endpoint reporting AliExpress configuration & status without exposing secrets
   */
  public static async getHealth(_req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const isConfigured = Boolean(env.ALIEXPRESS_APP_KEY && env.ALIEXPRESS_APP_SECRET);
      res.status(200).json({
        success: true,
        data: {
          configured: isConfigured,
          apiBaseUrl: env.ALIEXPRESS_API_BASE_URL,
          defaultShipToCountry: env.DEFAULT_SHIP_TO_COUNTRY,
          defaultTargetCurrency: env.DEFAULT_TARGET_CURRENCY,
          defaultTargetLanguage: env.DEFAULT_TARGET_LANGUAGE,
        },
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * POST & GET /api/aliexpress/webhook
   * Permanent, production-ready Webhook receiver for AliExpress Message Push Mechanism
   */
  public static async handleWebhook(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const payload = (req.body || {}) as RawAliExpressWebhookPayload;

      // Diagnostic Logging
      logger.info(`[Diagnostic Log] Webhook Request Arrived:`, {
        method: req.method,
        path: req.originalUrl,
        contentType: req.headers['content-type'],
        contentLength: req.headers['content-length'],
        headerKeys: Object.keys(req.headers),
        authorizationPresent: Boolean(req.headers.authorization),
        xSignaturePresent: Boolean(req.headers['x-aliexpress-signature']),
        querySignPresent: Boolean(req.query.sign),
        hasBody: Boolean(req.body && Object.keys(req.body).length > 0),
        bodyKeys: req.body && typeof req.body === 'object' ? Object.keys(req.body) : [],
        messageType: req.body?.message_type || req.body?.event_type || req.body?.topic || 'NONE',
        requestId: (req as unknown as { id?: string }).id,
      });

      const ackPayload = JSON.stringify({ code: 0, message: 'success' });

      // 1. GET requests serve as developer console test/verification callback handshake
      if (req.method === 'GET') {
        logger.info('AliExpress Webhook GET verification handshake request received.');
        res.status(200).setHeader('Content-Type', 'application/json; charset=utf-8').send(ackPayload);
        return;
      }

      // 2. Only POST method is valid for webhook message deliveries
      if (req.method !== 'POST') {
        res.status(405).json({
          code: 405,
          message: 'Method Not Allowed. Webhook deliveries must use POST.',
        });
        return;
      }

      // 3. Raw Body Buffer Extraction
      const rawBodyBuffer = (req as unknown as { rawBody?: Buffer }).rawBody ||
        Buffer.from(typeof req.body === 'string' ? req.body : JSON.stringify(req.body || {}), 'utf8');

      const authHeader = req.headers.authorization;
      const sigHeader = req.headers['x-aliexpress-signature'] as string | undefined;
      const querySign = req.query.sign as string | undefined;

      const hasMessagePayload = Boolean(payload.msg_id || payload.event_id || payload.message_type || payload.topic);

      // 4. Handle Developer Console Verification Probes
      if (!hasMessagePayload) {
        logger.info('AliExpress developer console verification test probe received. Acknowledging with HTTP 200.');
        res.status(200).setHeader('Content-Type', 'application/json; charset=utf-8').send(ackPayload);
        return;
      }

      // 5. Signature Verification for Real Message Deliveries
      const isSignatureValid = AliExpressWebhookSignatureService.verifyWebhookSignature(
        rawBodyBuffer,
        authHeader,
        sigHeader,
        querySign
      );

      if (!isSignatureValid) {
        throw new UnauthorizedError('Invalid or missing AliExpress webhook signature.');
      }

      const rawBodyStr = rawBodyBuffer.toString('utf8');

      // 6. Extract Event ID & Message Type
      const eventId = AliExpressWebhookService.extractEventId(payload, rawBodyStr);
      const messageType = AliExpressWebhookService.extractMessageType(payload);

      // 7. Idempotency Check (Deduplication)
      const isDuplicate = await AliExpressWebhookService.isDuplicateEvent(eventId);
      if (isDuplicate) {
        logger.info(`[Webhook] Duplicate event received and acknowledged: eventId=${eventId}`);
        res.status(200).setHeader('Content-Type', 'application/json; charset=utf-8').send(JSON.stringify({ code: 0, message: 'duplicate event acknowledged' }));
        return;
      }

      // 8. Persist Event Record
      const eventRecord: WebhookEventRecord = {
        eventId,
        messageType,
        receivedAt: Date.now(),
        processingStatus: 'RECEIVED',
        source: 'aliexpress_webhook',
        rawPayload: payload,
      };

      await AliExpressWebhookService.saveEventRecord(eventRecord);

      // 9. Trigger Async Processing (Non-blocking response)
      AliExpressWebhookService.processEventAsync(eventRecord);

      // 10. Quick Acknowledgment Response (HTTP 200)
      res.status(200).setHeader('Content-Type', 'application/json; charset=utf-8').send(ackPayload);
    } catch (error) {
      next(error);
    }
  }

  /**
   * GET /api/aliexpress/auth-url
   * Generate secure AliExpress OAuth authorization redirect URL
   */
  public static async getAuthUrl(_req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { authUrl, state } = await AliExpressOAuthService.getAuthorizationUrl();
      res.status(200).json({
        success: true,
        data: {
          authUrl,
          state,
        },
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * GET /api/aliexpress/callback
   * OAuth redirect callback endpoint registered in AliExpress App Console
   */
  public static async handleOAuthCallback(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const code = req.query.code as string;
      const state = req.query.state as string;
      const errorMsg = req.query.error_description || req.query.error;

      if (errorMsg) {
        logger.error(`OAuth callback received error from upstream.`);
        res.status(400).send(`
          <!DOCTYPE html>
          <html>
            <head><title>Authorization Failed</title></head>
            <body style="font-family: sans-serif; text-align: center; padding-top: 50px;">
              <h1 style="color: #d32f2f;">Authorization Failed</h1>
              <p>An error occurred during authentication with AliExpress.</p>
            </body>
          </html>
        `);
        return;
      }

      if (!state) {
        throw new BadRequestError('OAuth state parameter is required.');
      }

      const isStateValid = await AliExpressOAuthService.verifyAndConsumeState(state);
      if (!isStateValid) {
        throw new UnauthorizedError('OAuth state parameter is invalid, expired, or already consumed.');
      }

      if (!code) {
        throw new BadRequestError('Missing OAuth code in callback request.');
      }

      logger.info('OAuth authorization code and state verified successfully.');

      const tokenData = await AliExpressOAuthService.exchangeCodeForToken(code);

      const acceptHeader = req.headers.accept || '';
      if (acceptHeader.includes('text/html')) {
        const safeAccount = escapeHtml(tokenData.account_id || tokenData.user_nick || 'Authorized Account');
        res.status(200).send(`
          <!DOCTYPE html>
          <html>
            <head><title>Authorization Successful</title></head>
            <body style="font-family: system-ui, sans-serif; text-align: center; padding: 50px; background-color: #f9f9f9;">
              <div style="max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                <h1 style="color: #2e7d32;">Authorization Successful!</h1>
                <p>WorldFind Backend has successfully connected with your AliExpress Open Platform account.</p>
                <p><strong>Account ID / User:</strong> ${safeAccount}</p>
                <p style="color: #666; font-size: 0.9em;">Tokens have been encrypted and stored securely.</p>
              </div>
            </body>
          </html>
        `);
      } else {
        res.status(200).json({
          success: true,
          message: 'AliExpress OAuth authorization completed successfully.',
          data: {
            accountId: tokenData.account_id || tokenData.user_id,
            expiresIn: tokenData.expires_in,
          },
        });
      }
    } catch (error) {
      next(error);
    }
  }

  /**
   * GET /api/aliexpress/token-status
   * Protected Admin route to check status of stored token without exposing secrets
   */
  public static async getTokenStatus(_req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const tokenData = await AliExpressTokenService.getToken();
      if (!tokenData) {
        res.status(200).json({
          success: true,
          data: {
            hasActiveToken: false,
            message: 'No authorized AliExpress token found. Please run OAuth flow.',
          },
        });
        return;
      }

      const isExpired = AliExpressTokenService.isTokenExpired(tokenData);

      res.status(200).json({
        success: true,
        data: {
          hasActiveToken: !isExpired,
          isExpired,
          accountId: tokenData.account_id || tokenData.user_id,
          expiresInSeconds: Math.max(0, Math.floor(((tokenData.expire_time || Date.now()) - Date.now()) / 1000)),
        },
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * POST /api/aliexpress/refresh-token
   * Protected Admin route to trigger token refresh
   */
  public static async refreshToken(_req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const currentToken = await AliExpressTokenService.getToken();
      if (!currentToken || !currentToken.refresh_token) {
        throw new BadRequestError('No refresh token available to perform refresh.');
      }

      const refreshed = await AliExpressOAuthService.refreshToken(currentToken.refresh_token);

      res.status(200).json({
        success: true,
        message: 'Token refreshed successfully.',
        data: {
          expiresIn: refreshed.expires_in,
        },
      });
    } catch (error) {
      next(error);
    }
  }
}
