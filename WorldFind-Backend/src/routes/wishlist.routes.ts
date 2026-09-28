import { Router } from 'express';
import { WishlistController } from '../controllers/wishlist.controller';
import { requireFirebaseAuth } from '../middleware/auth.middleware';

const router = Router();

router.get('/wishlist', requireFirebaseAuth, WishlistController.getWishlist);
router.post('/wishlist/:productId', requireFirebaseAuth, WishlistController.toggleItem);
router.delete('/wishlist/:productId', requireFirebaseAuth, WishlistController.removeItem);

export default router;
