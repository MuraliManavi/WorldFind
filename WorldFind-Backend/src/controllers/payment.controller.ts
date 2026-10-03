import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { RazorpayService } from '../services/payment/razorpay.service';
import { OrderService } from '../services/order/order.service';
import { BadRequestError } from '../utils/errors';

export class PaymentController {
  public static async createRazorpayOrder(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { amount, receipt } = req.body;
      if (!amount || amount <= 0) {
        throw new BadRequestError('amount is required and must be greater than 0.');
      }

      const order = await RazorpayService.createOrder(amount, receipt || `rcpt_${Date.now()}`);
      res.status(200).json({ success: true, data: order });
    } catch (error) {
      next(error);
    }
  }

  public static async verifyRazorpayPayment(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const { razorpayOrderId, razorpayPaymentId, razorpaySignature, orderId } = req.body;

      if (!razorpayOrderId || !razorpayPaymentId || !razorpaySignature) {
        throw new BadRequestError('razorpayOrderId, razorpayPaymentId, and razorpaySignature are required.');
      }

      if (orderId) {
        const order = await OrderService.verifyOrderPayment(userId, orderId, {
          razorpayOrderId,
          razorpayPaymentId,
          razorpaySignature,
        });
        res.status(200).json({
          success: true,
          message: 'Payment verified and order confirmed successfully.',
          data: order,
        });
        return;
      }

      const isValid = RazorpayService.verifyPaymentSignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);

      if (!isValid) {
        throw new BadRequestError('Invalid Razorpay payment signature.');
      }

      res.status(200).json({
        success: true,
        message: 'Payment signature verified successfully.',
        data: { razorpayOrderId, razorpayPaymentId },
      });
    } catch (error) {
      next(error);
    }
  }
}
