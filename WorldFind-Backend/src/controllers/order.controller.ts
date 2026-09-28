import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { OrderService } from '../services/order/order.service';

function getStringParam(param: string | string[] | undefined): string {
  if (Array.isArray(param)) return param[0] || '';
  return String(param || '');
}

export class OrderController {
  public static async createOrder(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const order = await OrderService.createOrder(userId, req.body || {});
      res.status(201).json({ success: true, data: order });
    } catch (error) {
      next(error);
    }
  }

  public static async getOrders(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const orders = await OrderService.getOrders(userId);
      res.status(200).json({ success: true, data: orders });
    } catch (error) {
      next(error);
    }
  }

  public static async getOrderById(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const orderId = getStringParam(req.params.orderId);
      const order = await OrderService.getOrderById(userId, orderId);
      res.status(200).json({ success: true, data: order });
    } catch (error) {
      next(error);
    }
  }

  public static async cancelOrder(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const orderId = getStringParam(req.params.orderId);
      const order = await OrderService.cancelOrder(userId, orderId);
      res.status(200).json({ success: true, data: order });
    } catch (error) {
      next(error);
    }
  }
}
