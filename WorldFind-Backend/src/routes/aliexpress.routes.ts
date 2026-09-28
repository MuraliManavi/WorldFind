import { Router } from 'express';
import { AliExpressController } from '../controllers/aliexpress.controller';
import { oauthRateLimiter, adminRateLimiter, webhookRateLimiter } from '../middleware/rateLimit.middleware';
import { requireAdminKey } from '../middleware/auth.middleware';

const router = Router();

router.get('/health', AliExpressController.getHealth);

// Webhook & Verification routes for /webhook
router.get('/webhook', AliExpressController.handleWebhook);
router.post('/webhook', webhookRateLimiter, AliExpressController.handleWebhook);

// Callback route supporting both OAuth callback (with code/state query) and Webhook verification/delivery
router.get('/callback', oauthRateLimiter, (req, res, next) => {
  if (req.query.code || req.query.state) {
    return AliExpressController.handleOAuthCallback(req, res, next);
  }
  return AliExpressController.handleWebhook(req, res, next);
});
router.post('/callback', webhookRateLimiter, AliExpressController.handleWebhook);

router.get('/auth-url', oauthRateLimiter, AliExpressController.getAuthUrl);
router.get('/token-status', adminRateLimiter, requireAdminKey, AliExpressController.getTokenStatus);
router.post('/refresh-token', adminRateLimiter, requireAdminKey, AliExpressController.refreshToken);

export default router;
