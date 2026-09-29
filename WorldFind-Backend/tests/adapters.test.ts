import { AliExpressProtocolAdapter } from '../src/services/aliexpress/adapters/aliexpress.protocol.adapter';
import { AliExpressProductAdapter } from '../src/services/aliexpress/adapters/aliexpress.product.adapter';
import { AliExpressProductService } from '../src/services/aliexpress/aliexpress.product.service';
import { AliExpressTokenService } from '../src/services/aliexpress/aliexpress.token.service';
import { AliExpressOAuthService } from '../src/services/aliexpress/aliexpress.oauth.service';
import { BadRequestError } from '../src/utils/errors';

describe('AliExpress Protocol Adapters & Production Safety Tests', () => {
  it('AliExpressProtocolAdapter should attach app_signature if provided in payload or environment', () => {
    const params = AliExpressProtocolAdapter.buildRequestParams({
      apiName: 'aliexpress.affiliate.product.query',
      parameters: { keywords: 'test' },
      appSignature: 'test_app_sig_123',
    });

    expect(params).toHaveProperty('keywords', 'test');
    expect(params).toHaveProperty('app_signature', 'test_app_sig_123');
  });

  it('AliExpressProductAdapter methods should execute via protocol adapter without erroring during construction', () => {
    expect(typeof AliExpressProductAdapter.queryProducts).toBe('function');
    expect(typeof AliExpressProductAdapter.getProductDetail).toBe('function');
    expect(typeof AliExpressProductAdapter.generateAffiliateLinks).toBe('function');
    expect(typeof AliExpressProductAdapter.getCategories).toBe('function');
  });

  it('AliExpressProductService.resolveTrackingId should omit missing or known placeholder tracking IDs', () => {
    expect(AliExpressProductService.resolveTrackingId(undefined)).toBeUndefined();
    expect(AliExpressProductService.resolveTrackingId('')).toBeUndefined();
    expect(AliExpressProductService.resolveTrackingId('worldfind_default')).toBeUndefined();
    expect(AliExpressProductService.resolveTrackingId('your_tracking_id')).toBeUndefined();
    expect(AliExpressProductService.resolveTrackingId('test_tracking_id')).toBeUndefined();
    expect(AliExpressProductService.resolveTrackingId('demo_tracking_id')).toBeUndefined();
  });

  it('AliExpressProductService.resolveTrackingId should preserve legitimate non-placeholder tracking IDs', () => {
    const legitimate = 'my_real_tracking_id_2026';
    expect(AliExpressProductService.resolveTrackingId(legitimate)).toBe('my_real_tracking_id_2026');
  });

  it('AliExpressProductService.requireTrackingId should require valid tracking ID for affiliate link generation', () => {
    expect(() => AliExpressProductService.requireTrackingId('worldfind_default')).toThrow(BadRequestError);
    expect(AliExpressProductService.requireTrackingId('valid_affiliate_tag')).toBe('valid_affiliate_tag');
  });

  it('AliExpressTokenService.getToken should reject static token fallback in production mode', async () => {
    const originalEnv = process.env.NODE_ENV;
    process.env.NODE_ENV = 'production';

    // Clear in-memory cache
    AliExpressTokenService.clearCache();

    try {
      const token = await AliExpressTokenService.getToken();
      // Should be null because static fallback is prohibited in production
      expect(token).toBeNull();
    } finally {
      process.env.NODE_ENV = originalEnv;
      AliExpressTokenService.clearCache();
    }
  });

  it('AliExpressOAuthService.generateAndSaveState should generate cryptographically random 64-character hex string', async () => {
    const state = await AliExpressOAuthService.generateAndSaveState();
    expect(state).toHaveLength(64);
    expect(/^[0-9a-fA-F]{64}$/.test(state)).toBe(true);
  });
});
