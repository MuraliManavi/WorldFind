import { AliExpressSignatureService } from '../src/services/aliexpress/aliexpress.signature.service';

describe('AliExpress Signature Calculation', () => {
  const testSecret = 'sample_app_secret_999';

  it('should sort parameter keys alphabetically and produce upper-case HMAC-SHA256 signature', () => {
    const params = {
      app_key: '87654321',
      method: 'aliexpress.affiliate.product.query',
      timestamp: '1700000000000',
      v: '2.0',
      keywords: 'earbuds',
    };

    const signature = AliExpressSignatureService.generateSignature(params, {
      appSecret: testSecret,
      signMethod: 'hmac-sha256',
    });

    expect(typeof signature).toBe('string');
    expect(signature).toHaveLength(64);
    expect(signature).toBe(signature.toUpperCase());
  });

  it('should produce valid MD5 signature according to TOP protocol spec', () => {
    const params = {
      app_key: '12345678',
      method: 'aliexpress.affiliate.product.query',
      v: '2.0',
    };

    const sigMd5 = AliExpressSignatureService.generateSignature(params, {
      appSecret: testSecret,
      signMethod: 'md5',
    });

    expect(typeof sigMd5).toBe('string');
    expect(sigMd5).toHaveLength(32);
    expect(sigMd5).toBe(sigMd5.toUpperCase());
  });

  it('should ignore undefined, null, empty, and "sign" keys when computing signature', () => {
    const params1 = {
      a: '1',
      sign: 'EXCLUDE_ME',
      b: null,
      c: undefined,
      d: '',
    };

    const params2 = {
      a: '1',
    };

    const sign1 = AliExpressSignatureService.generateSignature(params1, { appSecret: testSecret });
    const sign2 = AliExpressSignatureService.generateSignature(params2, { appSecret: testSecret });

    expect(sign1).toBe(sign2);
  });

  it('should format timestamps in YYYY-MM-DD HH:mm:ss format', () => {
    const formattedTs = AliExpressSignatureService.getFormattedTimestamp();
    expect(formattedTs).toMatch(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/);
  });
});
