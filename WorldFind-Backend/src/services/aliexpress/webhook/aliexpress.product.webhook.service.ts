import { WebhookEventRecord } from '../../../models/AliExpressWebhook';
import { logger } from '../../../utils/logger';

export class AliExpressProductWebhookService {
  /**
   * Processes Product Information webhook events
   */
  public static async handle(event: WebhookEventRecord): Promise<Record<string, unknown>> {
    logger.info(`[ProductWebhook] Processing product eventId=${event.eventId}`);

    const payload = event.rawPayload as Record<string, unknown>;
    const productId = payload?.product_id || payload?.item_id;

    const normalizedData: Record<string, unknown> = {
      productId: productId ? String(productId) : undefined,
      processedAt: new Date().toISOString(),
    };

    return normalizedData;
  }
}
