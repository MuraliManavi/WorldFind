import { WebhookEventRecord } from '../../../models/AliExpressWebhook';
import { logger } from '../../../utils/logger';

export class AliExpressMessagingWebhookService {
  /**
   * Processes Instant Messaging webhook events
   */
  public static async handle(event: WebhookEventRecord): Promise<Record<string, unknown>> {
    logger.info(`[MessagingWebhook] Processing messaging eventId=${event.eventId}`);

    return {
      processedAt: new Date().toISOString(),
    };
  }
}
