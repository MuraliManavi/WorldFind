import request from 'supertest';
import { createApp } from '../src/app';

const app = createApp();

describe('AliExpress OAuth Endpoints Security Tests', () => {
  it('GET /api/aliexpress/auth-url should generate a URL containing a 64-char state parameter', async () => {
    const res = await request(app).get('/api/aliexpress/auth-url');
    if (res.status === 200) {
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('authUrl');
      expect(res.body.data).toHaveProperty('state');
      expect(res.body.data.state.length).toBe(64);
      expect(res.body.data.authUrl).toContain('state=');
    } else {
      expect(res.status).toBe(400); // If APP_KEY is unconfigured in test env
    }
  });

  it('GET /api/aliexpress/token-status should require admin authorization', async () => {
    const res = await request(app).get('/api/aliexpress/token-status');
    expect(res.status).toBe(401);
    expect(res.body.success).toBe(false);
  });
});
