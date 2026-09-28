import crypto from 'crypto';
import { env } from '../../config/env';
import { getFirestore, COLLECTIONS } from '../../config/firebase';
import { WebhookEventRecord, RawAliExpressWebhookPayload } from '../../models/AliExpressWebhook';
import { AliExpressWebhookDispatcher } from './webhook/aliexpress.webhook.dispatcher';
import { logger } from '../../utils/logger';

export class AliExpressWebhookService {
  private static inMemoryEventStore = new Map<string, WebhookEventRecord>();

  /**
   * Extracts or computes a unique event identifier from incoming webhook payload
   */
  public static extractEventId(payload: RawAliExpressWebhookPayload, rawBody: Buffer | string): string {
    if (payload) {
      if (payload.msg_id !== undefined && payload.msg_id !== null && payload.msg_id !== '') {
        return String(payload.msg_id);
      }
      if (payload.event_id !== undefined && payload.event_id !== null && payload.event_id !== '') {
        return String(payload.event_id);
      }
      if (payload.id !== undefined && payload.id !== null && payload.id !== '') {
        return String(payload.id);
      }
    }

    // Fallback: Compute deterministic SHA-256 hash of raw body
    return crypto.createHash('sha256').update(rawBody).digest('hex');
  }

  /**
   * Extracts message category/type from payload
   */
  public static extractMessageType(payload: RawAliExpressWebhookPayload): string {
    if (!payload) return 'UNKNOWN';
    return String(
      payload.message_type ||
      payload.event_type ||
      payload.topic ||
      payload.category ||
      payload.type ||
      'UNKNOWN'
    );
  }

  /**
   * Checks if event has already been stored (idempotency check)
   */
  public static async isDuplicateEvent(eventId: string): Promise<boolean> {
    const db = getFirestore();
    if (db) {
      try {
        const doc = await db.collection(COLLECTIONS.ALIEXPRESS_WEBHOOK_EVENTS).doc(eventId).get();
        if (doc.exists) {
          return true;
        }
      } catch (err) {
        logger.error(`Error checking duplicate eventId=${eventId} in Firestore:`, err);
      }
    }

    return this.inMemoryEventStore.has(eventId);
  }

  /**
   * Persists incoming raw webhook event record. Fails closed in production if Firestore is unavailable.
   */
  public static async saveEventRecord(record: WebhookEventRecord): Promise<void> {
    const db = getFirestore();
    if (db) {
      try {
        await db.collection(COLLECTIONS.ALIEXPRESS_WEBHOOK_EVENTS).doc(record.eventId).set(record);
        logger.info(`Webhook event persisted to Firestore: eventId=${record.eventId}`);
      } catch (err) {
        logger.error(`Failed to persist webhook eventId=${record.eventId} to Firestore:`, err);
        if (env.NODE_ENV === 'production') {
          throw new Error('[FATAL] Failed to persist webhook event in production.');
        }
        this.inMemoryEventStore.set(record.eventId, record);
      }
    } else {
      if (env.NODE_ENV === 'production') {
        throw new Error('[FATAL] Firestore database connection required for webhook event persistence in production.');
      }
      this.inMemoryEventStore.set(record.eventId, record);
    }
  }

  /**
   * Updates event processing status in storage
   */
  public static async updateEventStatus(
    eventId: string,
    status: 'PROCESSED' | 'FAILED',
    normalizedData?: Record<string, unknown>,
    errorMessage?: string
  ): Promise<void> {
    const db = getFirestore();
    if (db) {
      try {
        await db.collection(COLLECTIONS.ALIEXPRESS_WEBHOOK_EVENTS).doc(eventId).update({
          processingStatus: status,
          ...(normalizedData ? { normalizedData } : {}),
          ...(errorMessage ? { errorMessage } : {}),
        });
      } catch (err) {
        logger.error(`Failed to update eventStatus for eventId=${eventId} in Firestore:`, err);
      }
    }

    if (this.inMemoryEventStore.has(eventId)) {
      const existing = this.inMemoryEventStore.get(eventId)!;
      existing.processingStatus = status;
      if (normalizedData) existing.normalizedData = normalizedData;
      if (errorMessage) existing.errorMessage = errorMessage;
    }
  }

  /**
   * Asynchronously processes event without blocking the HTTP acknowledgment response
   */
  public static processEventAsync(record: WebhookEventRecord): void {
    setImmediate(async () => {
      try {
        const normalizedData = await AliExpressWebhookDispatcher.dispatch(record);
        await this.updateEventStatus(record.eventId, 'PROCESSED', normalizedData);
      } catch (err) {
        const errorMsg = err instanceof Error ? err.message : String(err);
        logger.error(`Error in async processing of webhook eventId=${record.eventId}:`, err);
        await this.updateEventStatus(record.eventId, 'FAILED', undefined, errorMsg);
      }
    });
  }

  /**
   * Clears in-memory store (used for tests)
   */
  public static clearMemoryStore(): void {
    this.inMemoryEventStore.clear();
  }
}
