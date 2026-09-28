import request from 'supertest';
import { createApp } from '../src/app';

const app = createApp();

describe('Health API Routes', () => {
  it('GET /health should return 200 and healthy status', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('success', true);
    expect(res.body).toHaveProperty('service', 'WorldFind Backend');
    expect(res.body).toHaveProperty('status', 'healthy');
  });

  it('GET / should return 200 with API information', async () => {
    const res = await request(app).get('/');
    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('success', true);
    expect(res.body).toHaveProperty('service', 'WorldFind Backend API');
    expect(res.body).toHaveProperty('version', '1.0.0');
  });

  it('GET /non-existent-route should return 404 with standard error format', async () => {
    const res = await request(app).get('/non-existent-route');
    expect(res.status).toBe(404);
    expect(res.body).toHaveProperty('success', false);
    expect(res.body.error).toHaveProperty('code', 'NOT_FOUND');
    expect(res.body.error).toHaveProperty('requestId');
  });
});
