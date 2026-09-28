import { Router } from 'express';
import { OrderController } from '../controllers/order.controller';
import { requireFirebaseAuth } from '../middleware/auth.middleware';

const router = Router();

router.post('/orders', requireFirebaseAuth, OrderController.createOrder);
router.get('/orders', requireFirebaseAuth, OrderController.getOrders);
router.get('/orders/:orderId', requireFirebaseAuth, OrderController.getOrderById);
router.post('/orders/:orderId/cancel', requireFirebaseAuth, OrderController.cancelOrder);

export default router;
