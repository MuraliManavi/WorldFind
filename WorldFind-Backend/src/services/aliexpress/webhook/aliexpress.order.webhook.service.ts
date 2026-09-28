import { WebhookEventRecord } from '../../../models/AliExpressWebhook';
import { logger } from '../../../utils/logger';

export class AliExpressOrderWebhookService {
  /**
   * Processes Order Information webhook events
   */
  public static async handle(event: WebhookEventRecord): Promise<Record<string, unknown>> {
    logger.info(`[OrderWebhook] Processing order eventId=${event.eventId}`);

    const payload = event.rawPayload as Record<string, unknown>;
    const orderId = payload?.order_id || payload?.trade_order_id || payload?.out_order_id;
    const orderStatus = payload?.order_status || payload?.status;

    const normalizedData: Record<string, unknown> = {
      orderId: orderId ? String(orderId) : undefined,
      orderStatus: orderStatus ? String(orderStatus) : undefined,
      processedAt: new Date().toISOString(),
    };

    logger.info(`[OrderWebhook] Normalized order event: orderId=${normalizedData.orderId || 'N/A'}, status=${normalizedData.orderStatus || 'N/A'}`);
    return normalizedData;
  }
}
