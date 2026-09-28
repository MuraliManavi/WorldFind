import { WebhookEventRecord } from '../../../models/AliExpressWebhook';
import { AliExpressOrderWebhookService } from './aliexpress.order.webhook.service';
import { AliExpressLogisticsWebhookService } from './aliexpress.logistics.webhook.service';
import { AliExpressProductWebhookService } from './aliexpress.product.webhook.service';
import { AliExpressSystemWebhookService } from './aliexpress.system.webhook.service';
import { AliExpressMessagingWebhookService } from './aliexpress.messaging.webhook.service';
import { logger } from '../../../utils/logger';

export class AliExpressWebhookDispatcher {
  /**
   * Dispatches incoming webhook event to appropriate category handler
   */
  public static async dispatch(event: WebhookEventRecord): Promise<Record<string, unknown>> {
    const typeUpper = (event.messageType || '').toUpperCase();

    logger.info(`[WebhookDispatcher] Dispatching eventId=${event.eventId}, category=${typeUpper}`);

    if (typeUpper.includes('ORDER')) {
      return AliExpressOrderWebhookService.handle(event);
    } else if (typeUpper.includes('LOGISTIC') || typeUpper.includes('SHIPMENT')) {
      return AliExpressLogisticsWebhookService.handle(event);
    } else if (typeUpper.includes('PRODUCT') || typeUpper.includes('ITEM')) {
      return AliExpressProductWebhookService.handle(event);
    } else if (typeUpper.includes('SYSTEM')) {
      return AliExpressSystemWebhookService.handle(event);
    } else if (typeUpper.includes('MESSAGE') || typeUpper.includes('CHAT')) {
      return AliExpressMessagingWebhookService.handle(event);
    } else {
      logger.info(`[WebhookDispatcher] Unknown or generic message type: ${typeUpper}. Recording payload safely.`);
      return {
        category: 'GENERIC',
        processedAt: new Date().toISOString(),
      };
    }
  }
}
