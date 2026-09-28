export type WebhookProcessingStatus = 'RECEIVED' | 'PROCESSED' | 'DUPLICATE' | 'FAILED';

export interface RawAliExpressWebhookPayload {
  msg_id?: string | number;
  event_id?: string | number;
  id?: string | number;
  message_type?: string;
  event_type?: string;
  topic?: string;
  category?: string;
  type?: string;
  app_key?: string;
  time?: number | string;
  timestamp?: number | string;
  gmt_create?: string;
  content?: Record<string, unknown> | string;
  data?: Record<string, unknown> | string;
  payload?: Record<string, unknown> | string;
  [key: string]: unknown;
}

export interface WebhookEventRecord {
  eventId: string;
  messageType: string;
  receivedAt: number;
  processingStatus: WebhookProcessingStatus;
  source: 'aliexpress_webhook';
  rawPayload: unknown;
  normalizedData?: Record<string, unknown>;
  errorMessage?: string;
}
