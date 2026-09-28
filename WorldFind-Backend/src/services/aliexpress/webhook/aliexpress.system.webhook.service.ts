import { WebhookEventRecord } from '../../../models/AliExpressWebhook';
import { logger } from '../../../utils/logger';

export class AliExpressSystemWebhookService {
  /**
   * Processes System Notification webhook events
   */
  public static async handle(event: WebhookEventRecord): Promise<Record<string, unknown>> {
    logger.info(`[SystemWebhook] Processing system notification eventId=${event.eventId}`);

    return {
      processedAt: new Date().toISOString(),
    };
  }
}
