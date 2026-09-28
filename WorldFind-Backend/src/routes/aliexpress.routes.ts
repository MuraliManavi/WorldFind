import { Router } from 'express';
import { AliExpressController } from '../controllers/aliexpress.controller';
import { oauthRateLimiter, adminRateLimiter, webhookRateLimiter } from '../middleware/rateLimit.middleware';
import { requireAdminKey } from '../middleware/auth.middleware';

const router = Router();

router.get('/health', AliExpressController.getHealth);

router.get('/webhook', AliExpressController.handleWebhook);
router.post('/webhook', webhookRateLimiter, AliExpressController.handleWebhook);

router.get('/auth-url', oauthRateLimiter, AliExpressController.getAuthUrl);
router.get('/callback', oauthRateLimiter, AliExpressController.handleOAuthCallback);
router.get('/token-status', adminRateLimiter, requireAdminKey, AliExpressController.getTokenStatus);
router.post('/refresh-token', adminRateLimiter, requireAdminKey, AliExpressController.refreshToken);

export default router;
