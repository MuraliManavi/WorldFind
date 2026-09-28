import { Router } from 'express';
import { CheckoutController } from '../controllers/checkout.controller';
import { requireFirebaseAuth } from '../middleware/auth.middleware';

const router = Router();

router.post('/checkout/preview', requireFirebaseAuth, CheckoutController.previewCheckout);

export default router;
