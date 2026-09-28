import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { CheckoutService } from '../services/checkout/checkout.service';

export class CheckoutController {
  public static async previewCheckout(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const preview = await CheckoutService.previewCheckout(userId, req.body || {});
      res.status(200).json({ success: true, data: preview });
    } catch (error) {
      next(error);
    }
  }
}
