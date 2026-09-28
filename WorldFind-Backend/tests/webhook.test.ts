import request from 'supertest';
import { createApp } from '../src/app';
import { AliExpressWebhookSignatureService } from '../src/services/aliexpress/aliexpress.webhook.signature.service';
import { AliExpressWebhookService } from '../src/services/aliexpress/aliexpress.webhook.service';
import { env } from '../src/config/env';

const app = createApp();

describe('AliExpress Webhook Message Service Unit & Integration Tests', () => {
  const TEST_KEY = 'test_key_123';
  const TEST_SECRET = 'test_secret_123';

  beforeAll(() => {
    (env as { ALIEXPRESS_APP_KEY: string }).ALIEXPRESS_APP_KEY = TEST_KEY;
    (env as { ALIEXPRESS_APP_SECRET: string }).ALIEXPRESS_APP_SECRET = TEST_SECRET;
  });

  beforeEach(() => {
    AliExpressWebhookService.clearMemoryStore();
  });

  it('GET /api/aliexpress/webhook should succeed for developer console verification handshake', async () => {
    const res = await request(app).get('/api/aliexpress/webhook');
    expect(res.status).toBe(200);
    expect(res.body).toEqual({
      code: 0,
      message: 'success',
    });
  });

  it('POST /api/aliexpress/webhook should accept valid webhook signature', async () => {
    const payload = {
      msg_id: 'test_order_msg_001',
      message_type: 'ORDER_STATUS_CHANGE',
      order_id: '9988776655',
      order_status: 'WAIT_SELLER_SEND_GOODS',
    };
    const rawBody = JSON.stringify(payload);

    const validSig = AliExpressWebhookSignatureService.computeSignature(rawBody, TEST_SECRET, TEST_KEY);

    const res = await request(app)
      .post('/api/aliexpress/webhook')
      .set('Content-Type', 'application/json')
      .set('Authorization', `Digest ${validSig}`)
      .send(payload);

    expect(res.status).toBe(200);
    expect(res.body.code).toBe(0);
    expect(res.body.message).toBe('success');
  });

  it('POST /api/aliexpress/webhook should reject invalid or missing signature', async () => {
    const payload = { msg_id: 'test_msg_002', message_type: 'SYSTEM_NOTIFICATION' };

    // Missing signature
    const resNoSig = await request(app)
      .post('/api/aliexpress/webhook')
      .send(payload);

    expect(resNoSig.status).toBe(401);
    expect(resNoSig.body.success).toBe(false);

    // Invalid signature
    const resInvalidSig = await request(app)
      .post('/api/aliexpress/webhook')
      .set('Authorization', 'Digest INVALID_SIGNATURE_STRING_HEX')
      .send(payload);

    expect(resInvalidSig.status).toBe(401);
    expect(resInvalidSig.body.success).toBe(false);
  });

  it('Modified request body should fail signature verification', async () => {
    const originalPayload = { msg_id: 'test_msg_003', order_id: '100' };
    const rawBody = JSON.stringify(originalPayload);

    const sig = AliExpressWebhookSignatureService.computeSignature(rawBody, TEST_SECRET, TEST_KEY);

    // Send tampered body with same signature header
    const tamperedPayload = { msg_id: 'test_msg_003', order_id: '99999' };

    const res = await request(app)
      .post('/api/aliexpress/webhook')
      .set('Content-Type', 'application/json')
      .set('Authorization', `Digest ${sig}`)
      .send(tamperedPayload);

    expect(res.status).toBe(401);
  });

  it('Duplicate webhook events must be deduplicated and acknowledged without re-processing', async () => {
    const payload = {
      msg_id: 'duplicate_msg_id_100',
      message_type: 'LOGISTICS_UPDATE',
      tracking_number: 'IN123456789',
      carrier_name: 'India Post',
    };
    const rawBody = JSON.stringify(payload);

    const sig = AliExpressWebhookSignatureService.computeSignature(rawBody, TEST_SECRET, TEST_KEY);

    // First request
    const res1 = await request(app)
      .post('/api/aliexpress/webhook')
      .set('Content-Type', 'application/json')
      .set('Authorization', `Digest ${sig}`)
      .send(payload);

    expect(res1.status).toBe(200);
    expect(res1.body.message).toBe('success');

    // Duplicate request
    const res2 = await request(app)
      .post('/api/aliexpress/webhook')
      .set('Content-Type', 'application/json')
      .set('Authorization', `Digest ${sig}`)
      .send(payload);

    expect(res2.status).toBe(200);
    expect(res2.body.message).toBe('duplicate event acknowledged');
  });

  it('Webhook endpoint does NOT require admin API key (x-admin-key)', async () => {
    const payload = { msg_id: 'msg_no_admin_key_test', message_type: 'SYSTEM' };
    const rawBody = JSON.stringify(payload);

    const sig = AliExpressWebhookSignatureService.computeSignature(rawBody, TEST_SECRET, TEST_KEY);

    const res = await request(app)
      .post('/api/aliexpress/webhook')
      .set('Content-Type', 'application/json')
      .set('Authorization', `Digest ${sig}`)
      .send(payload);

    // No x-admin-key header provided, but valid webhook signature -> accepted!
    expect(res.status).toBe(200);
  });

  it('In production mode, Firestore unavailability must fail closed for event persistence', async () => {
    const originalEnv = env.NODE_ENV;

    try {
      (env as { NODE_ENV: string }).NODE_ENV = 'production';

      const record = {
        eventId: 'prod_test_001',
        messageType: 'ORDER',
        receivedAt: Date.now(),
        processingStatus: 'RECEIVED' as const,
        source: 'aliexpress_webhook' as const,
        rawPayload: {},
      };

      await expect(AliExpressWebhookService.saveEventRecord(record)).rejects.toThrow('[FATAL]');
    } finally {
      (env as { NODE_ENV: string }).NODE_ENV = originalEnv;
    }
  });
});
