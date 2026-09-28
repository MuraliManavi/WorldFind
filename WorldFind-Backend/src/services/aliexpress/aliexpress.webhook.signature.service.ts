import crypto from 'crypto';
import { env } from '../../config/env';
import { logger } from '../../utils/logger';

export class AliExpressWebhookSignatureService {
  /**
   * Computes the expected HMAC-SHA256 or HMAC-MD5 webhook signature for raw body string
   */
  public static computeSignature(
    rawBody: string | Buffer,
    appSecret: string,
    appKey: string = env.ALIEXPRESS_APP_KEY,
    algorithm: 'sha256' | 'md5' = 'sha256'
  ): string {
    const bodyStr = typeof rawBody === 'string' ? rawBody : rawBody.toString('utf8');
    const payloadToSign = `${appKey}${bodyStr}${appSecret}`;

    const hmac = crypto.createHmac(algorithm, appSecret);
    hmac.update(payloadToSign, 'utf8');
    return hmac.digest('hex').toUpperCase();
  }

  /**
   * Constant-time comparison of two signature strings to prevent timing attacks
   */
  public static timingSafeCompare(sigA: string, sigB: string): boolean {
    if (!sigA || !sigB) return false;
    const bufA = Buffer.from(sigA.trim().toUpperCase(), 'utf8');
    const bufB = Buffer.from(sigB.trim().toUpperCase(), 'utf8');

    if (bufA.length !== bufB.length) {
      return false;
    }

    return crypto.timingSafeEqual(bufA, bufB);
  }

  /**
   * Extracts signature string from HTTP Authorization header or X-AliExpress-Signature / query param
   */
  public static extractSignatureFromRequest(
    authHeader?: string,
    sigHeader?: string,
    querySign?: string
  ): string | undefined {
    if (sigHeader) {
      return sigHeader.trim();
    }

    if (querySign) {
      return querySign.trim();
    }

    if (authHeader) {
      const parts = authHeader.trim().split(' ');
      if (parts.length === 2) {
        return parts[1];
      }
      return authHeader.trim();
    }

    return undefined;
  }

  /**
   * Verifies incoming webhook request signature using configured App Secret
   */
  public static verifyWebhookSignature(
    rawBody: string | Buffer,
    authHeader?: string,
    sigHeader?: string,
    querySign?: string
  ): boolean {
    const appSecret = env.ALIEXPRESS_APP_SECRET;
    const appKey = env.ALIEXPRESS_APP_KEY;

    const incomingSignature = this.extractSignatureFromRequest(authHeader, sigHeader, querySign);

    if (!incomingSignature) {
      logger.warn('Missing webhook signature in Authorization header, X-AliExpress-Signature header, or query sign.');
      return false;
    }

    const effectiveSecret = appSecret || 'test_secret_123';
    const effectiveKey = appKey || 'test_key_123';

    // Try HMAC-SHA256 first, then HMAC-MD5
    const expectedSha256 = this.computeSignature(rawBody, effectiveSecret, effectiveKey, 'sha256');
    if (this.timingSafeCompare(incomingSignature, expectedSha256)) {
      return true;
    }

    const expectedMd5 = this.computeSignature(rawBody, effectiveSecret, effectiveKey, 'md5');
    if (this.timingSafeCompare(incomingSignature, expectedMd5)) {
      return true;
    }

    // Fallback: Check if payload is signed as appSecret + body + appSecret
    const bodyStr = typeof rawBody === 'string' ? rawBody : rawBody.toString('utf8');
    const altPayload = `${effectiveSecret}${bodyStr}${effectiveSecret}`;
    const altSha256 = crypto.createHmac('sha256', effectiveSecret).update(altPayload, 'utf8').digest('hex').toUpperCase();
    if (this.timingSafeCompare(incomingSignature, altSha256)) {
      return true;
    }

    logger.warn('Webhook signature verification failed: signature mismatch.');
    return false;
  }
}
