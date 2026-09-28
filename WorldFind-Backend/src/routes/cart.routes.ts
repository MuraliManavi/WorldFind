import { Router } from 'express';
import { CartController } from '../controllers/cart.controller';
import { requireFirebaseAuth } from '../middleware/auth.middleware';

const router = Router();

router.get('/cart', requireFirebaseAuth, CartController.getCart);
router.post('/cart/items', requireFirebaseAuth, CartController.addItem);
router.patch('/cart/items/:productId', requireFirebaseAuth, CartController.updateQuantity);
router.delete('/cart/items/:productId', requireFirebaseAuth, CartController.removeItem);
router.delete('/cart', requireFirebaseAuth, CartController.clearCart);

export default router;
