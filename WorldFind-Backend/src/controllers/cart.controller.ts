import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { CartService } from '../services/cart/cart.service';
import { BadRequestError } from '../utils/errors';

function getStringParam(param: string | string[] | undefined): string {
  if (Array.isArray(param)) return param[0] || '';
  return String(param || '');
}

export class CartController {
  public static async getCart(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const cart = await CartService.getCart(userId);
      res.status(200).json({ success: true, data: cart });
    } catch (error) {
      next(error);
    }
  }

  public static async addItem(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const { productId, quantity } = req.body;
      if (!productId) {
        throw new BadRequestError('productId is required in request body.');
      }
      const cart = await CartService.addItem(userId, productId, quantity || 1);
      res.status(200).json({ success: true, data: cart });
    } catch (error) {
      next(error);
    }
  }

  public static async updateQuantity(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const productId = getStringParam(req.params.productId);
      const { quantity } = req.body;
      if (quantity === undefined) {
        throw new BadRequestError('quantity is required in request body.');
      }
      const cart = await CartService.updateItemQuantity(userId, productId, quantity);
      res.status(200).json({ success: true, data: cart });
    } catch (error) {
      next(error);
    }
  }

  public static async removeItem(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const productId = getStringParam(req.params.productId);
      const cart = await CartService.removeItem(userId, productId);
      res.status(200).json({ success: true, data: cart });
    } catch (error) {
      next(error);
    }
  }

  public static async clearCart(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const cart = await CartService.clearCart(userId);
      res.status(200).json({ success: true, data: cart });
    } catch (error) {
      next(error);
    }
  }
}
