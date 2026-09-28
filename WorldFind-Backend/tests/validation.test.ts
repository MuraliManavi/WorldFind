import request from 'supertest';
import { createApp } from '../src/app';

const app = createApp();

describe('API Input Validation Unit & Integration Tests', () => {
  it('should reject requests where maxSalePrice is less than minSalePrice', async () => {
    const res = await request(app).get('/api/products?minSalePrice=500&maxSalePrice=100');
    expect(res.status).toBe(422);
    expect(res.body.success).toBe(false);
    expect(res.body.error.code).toBe('VALIDATION_ERROR');
  });

  it('should reject invalid country code formats', async () => {
    const res = await request(app).get('/api/products?shipToCountry=INVALID_COUNTRY');
    expect(res.status).toBe(422);
    expect(res.body.success).toBe(false);
  });

  it('should reject pageSize greater than 100', async () => {
    const res = await request(app).get('/api/products?pageSize=200');
    expect(res.status).toBe(422);
    expect(res.body.success).toBe(false);
  });

  it('should reject invalid currency codes', async () => {
    const res = await request(app).get('/api/products?targetCurrency=TOOLONG');
    expect(res.status).toBe(422);
    expect(res.body.success).toBe(false);
  });

  it('POST /api/products/affiliate-link should reject invalid URLs', async () => {
    const res = await request(app)
      .post('/api/products/affiliate-link')
      .send({ urls: ['invalid-url-string'] });

    expect(res.status).toBe(422);
    expect(res.body.success).toBe(false);
  });
});
