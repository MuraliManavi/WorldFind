import { WebhookEventRecord } from '../../../models/AliExpressWebhook';
import { logger } from '../../../utils/logger';

export class AliExpressLogisticsWebhookService {
  /**
   * Processes Logistics Information webhook events
   */
  public static async handle(event: WebhookEventRecord): Promise<Record<string, unknown>> {
    logger.info(`[LogisticsWebhook] Processing logistics eventId=${event.eventId}`);

    const payload = event.rawPayload as Record<string, unknown>;
    const trackingNo = payload?.tracking_number || payload?.logistics_no || payload?.mail_no;
    const carrier = payload?.carrier_name || payload?.logistics_company || payload?.logistics_service;
    const status = payload?.logistics_status || payload?.status;

    const normalizedData: Record<string, unknown> = {
      trackingNumber: trackingNo ? String(trackingNo) : undefined,
      carrier: carrier ? String(carrier) : undefined,
      logisticsStatus: status ? String(status) : undefined,
      processedAt: new Date().toISOString(),
    };

    logger.info(`[LogisticsWebhook] Normalized logistics event: trackingNo=${normalizedData.trackingNumber || 'N/A'}, carrier=${normalizedData.carrier || 'N/A'}`);
    return normalizedData;
  }
}
