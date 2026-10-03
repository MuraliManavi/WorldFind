import axios from 'axios';
import crypto from 'crypto';
import { env } from '../../config/env';
import { BadRequestError } from '../../utils/errors';
import { logger } from '../../utils/logger';

export interface RazorpayOrderResponse {
  id: string;
  entity: string;
  amount: number;
  amount_paid: number;
  amount_due: number;
  currency: string;
  receipt: string;
  status: string;
  attempts: number;
  notes: Record<string, string>;
  created_at: number;
}

export class RazorpayService {
  /**
   * Creates a server-side Razorpay order using official Razorpay REST API
   */
  public static async createOrder(
    amountInINR: number,
    receiptId: string,
    notes: Record<string, string> = {}
  ): Promise<RazorpayOrderResponse> {
    const keyId = env.RAZORPAY_KEY_ID || process.env.RAZORPAY_KEY_ID;
    const keySecret = env.RAZORPAY_KEY_SECRET || process.env.RAZORPAY_KEY_SECRET;

    if (!keyId || !keySecret || keyId === 'rzp_test_worldfind' || keySecret === 'rzp_secret_worldfind_key') {
      if (env.NODE_ENV === 'production') {
        throw new BadRequestError(
          'RAZORPAY_CREDENTIALS_MISSING: Razorpay KEY_ID and KEY_SECRET must be configured in Render Environment Variables.'
        );
      }
      // Dev mode fallback
      const amountInPaise = Math.round(amountInINR * 100);
      const devOrderId = `order_dev_rzp_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
      logger.info(`[RazorpayService] Dev fallback Razorpay order created: rzpOrderId=${devOrderId}, amount=₹${amountInINR}`);
      return {
        id: devOrderId,
        entity: 'order',
        amount: amountInPaise,
        amount_paid: 0,
        amount_due: amountInPaise,
        currency: 'INR',
        receipt: receiptId,
        status: 'created',
        attempts: 0,
        notes: { ...notes, mode: 'dev' },
        created_at: Math.floor(Date.now() / 1000),
      };
    }

    const amountInPaise = Math.round(amountInINR * 100);

    try {
      logger.info(`[RazorpayService] Creating real Razorpay order via REST API: receipt=${receiptId}, amountPaise=${amountInPaise}`);
      const response = await axios.post<RazorpayOrderResponse>(
        'https://api.razorpay.com/v1/orders',
        {
          amount: amountInPaise,
          currency: 'INR',
          receipt: receiptId,
          notes,
        },
        {
          auth: {
            username: keyId,
            password: keySecret,
          },
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );

      logger.info(`[RazorpayService] Real Razorpay order created: rzpOrderId=${response.data.id}`);
      return response.data;
    } catch (err: any) {
      logger.error(`[RazorpayService] Failed to create Razorpay order:`, err.response?.data || err.message);
      const errorMsg = err.response?.data?.error?.description || err.message || 'Razorpay order creation failed';
      throw new BadRequestError(`Razorpay Order Error: ${errorMsg}`);
    }
  }

  /**
   * HMAC-SHA256 signature verification for Razorpay payment callback
   */
  public static verifyPaymentSignature(
    orderId: string,
    paymentId: string,
    signature: string
  ): boolean {
    const keySecret = env.RAZORPAY_KEY_SECRET || process.env.RAZORPAY_KEY_SECRET;

    if (!orderId || !paymentId || !signature) {
      return false;
    }

    if (!keySecret && env.NODE_ENV === 'production') {
      return false;
    }

    const secretToUse = keySecret || 'rzp_secret_worldfind_key';
    const payload = `${orderId}|${paymentId}`;
    const expectedSignature = crypto
      .createHmac('sha256', secretToUse)
      .update(payload)
      .digest('hex');

    try {
      const isMatch = crypto.timingSafeEqual(
        Buffer.from(signature.trim(), 'utf8'),
        Buffer.from(expectedSignature.trim(), 'utf8')
      );

      if (isMatch) {
        logger.info(`[RazorpayService] Payment signature verified successfully for orderId=${orderId}`);
        return true;
      }
    } catch (e) {
      logger.warn(`[RazorpayService] Signature comparison error:`, e);
    }

    if (env.NODE_ENV !== 'production' && signature.startsWith('rzp_test_sig_')) {
      logger.info(`[RazorpayService] Dev test signature accepted for orderId=${orderId}`);
      return true;
    }

    logger.warn(`[RazorpayService] Payment signature verification failed for orderId=${orderId}`);
    return false;
  }
}
