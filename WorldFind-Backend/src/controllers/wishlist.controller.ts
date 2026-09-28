import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { WishlistService } from '../services/wishlist/wishlist.service';

function getStringParam(param: string | string[] | undefined): string {
  if (Array.isArray(param)) return param[0] || '';
  return String(param || '');
}

export class WishlistController {
  public static async getWishlist(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const wishlist = await WishlistService.getWishlist(userId);
      res.status(200).json({ success: true, data: wishlist });
    } catch (error) {
      next(error);
    }
  }

  public static async toggleItem(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const productId = getStringParam(req.params.productId);
      const wishlist = await WishlistService.toggleItem(userId, productId);
      res.status(200).json({ success: true, data: wishlist });
    } catch (error) {
      next(error);
    }
  }

  public static async removeItem(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const productId = getStringParam(req.params.productId);
      const wishlist = await WishlistService.removeItem(userId, productId);
      res.status(200).json({ success: true, data: wishlist });
    } catch (error) {
      next(error);
    }
  }
}
