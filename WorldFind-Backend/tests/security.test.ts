import request from 'supertest';
import { createApp } from '../src/app';
import { AliExpressTokenService } from '../src/services/aliexpress/aliexpress.token.service';
import { AliExpressOAuthService } from '../src/services/aliexpress/aliexpress.oauth.service';

const app = createApp();

describe('Security & Token Protection Tests', () => {
  it('Encryption must generate independent IVs and authTags for access and refresh tokens', async () => {
    const accessToken = 'access_token_secret_123';
    const refreshToken = 'refresh_token_secret_456';

    const accessEncrypted = AliExpressTokenService.encrypt(accessToken);
    const refreshEncrypted = AliExpressTokenService.encrypt(refreshToken);

    expect(accessEncrypted.iv).not.toBe(refreshEncrypted.iv);
    expect(accessEncrypted.authTag).not.toBe(refreshEncrypted.authTag);

    const decryptedAccess = AliExpressTokenService.decrypt(
      accessEncrypted.encrypted,
      accessEncrypted.iv,
      accessEncrypted.authTag
    );
    const decryptedRefresh = AliExpressTokenService.decrypt(
      refreshEncrypted.encrypted,
      refreshEncrypted.iv,
      refreshEncrypted.authTag
    );

    expect(decryptedAccess).toBe(accessToken);
    expect(decryptedRefresh).toBe(refreshToken);
  });

  it('OAuth state must be generated, single-use consumed, and reject replay attempts', async () => {
    const state = await AliExpressOAuthService.generateAndSaveState();
    expect(state).toBeDefined();
    expect(state.length).toBe(64); // 32 random bytes in hex

    // First verification succeeds (consumes state)
    const firstVerify = await AliExpressOAuthService.verifyAndConsumeState(state);
    expect(firstVerify).toBe(true);

    // Second verification fails (replay attack protection)
    const replayVerify = await AliExpressOAuthService.verifyAndConsumeState(state);
    expect(replayVerify).toBe(false);
  });

  it('Protected admin routes should reject requests without x-admin-key header and succeed with valid key', async () => {
    // Missing key -> 401
    const statusResMissing = await request(app).get('/api/aliexpress/token-status');
    expect(statusResMissing.status).toBe(401);
    expect(statusResMissing.body.success).toBe(false);

    // Valid key -> 200
    const statusResValid = await request(app)
      .get('/api/aliexpress/token-status')
      .set('x-admin-key', 'worldfind_admin_secret_key');
    expect(statusResValid.status).toBe(200);
    expect(statusResValid.body.success).toBe(true);

    // Refresh route without key -> 401
    const refreshResMissing = await request(app).post('/api/aliexpress/refresh-token');
    expect(refreshResMissing.status).toBe(401);
  });

  it('OAuth callback should reject missing or invalid state parameter', async () => {
    const res = await request(app).get('/api/aliexpress/callback?code=12345');
    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.error.message).toContain('state');
  });
});
