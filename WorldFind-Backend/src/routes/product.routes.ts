import { Router } from 'express';
import { ProductController } from '../controllers/product.controller';
import { productRateLimiter, affiliateLinkRateLimiter } from '../middleware/rateLimit.middleware';

const router = Router();

router.get('/products', productRateLimiter, ProductController.getProducts);
router.get('/products/:productId', productRateLimiter, ProductController.getProductById);
router.post('/products/affiliate-link', affiliateLinkRateLimiter, ProductController.generateAffiliateLinks);
router.get('/categories', productRateLimiter, ProductController.getCategories);

export default router;
