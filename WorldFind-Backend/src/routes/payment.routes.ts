import { Router } from 'express';
import { PaymentController } from '../controllers/payment.controller';
import { requireFirebaseAuth } from '../middleware/auth.middleware';

const router = Router();

router.post('/payments/razorpay/create-order', requireFirebaseAuth, PaymentController.createRazorpayOrder);
router.post('/payments/razorpay/verify', requireFirebaseAuth, PaymentController.verifyRazorpayPayment);

export default router;
