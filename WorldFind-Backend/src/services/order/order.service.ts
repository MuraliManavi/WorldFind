import { Order, OrderStatus, PaymentMethod, PaymentStatus } from '../../models/Order';
import { CheckoutService } from '../checkout/checkout.service';
import { CartService } from '../cart/cart.service';
import { getFirestoreDb } from '../../config/firebase';
import { BadRequestError, NotFoundError } from '../../utils/errors';
import { logger } from '../../utils/logger';

const memoryOrderStore = new Map<string, Order[]>();

export class OrderService {
  public static async createOrder(
    userId: string,
    params: {
      addressId?: string;
      items?: { productId: string; quantity: number }[];
      paymentMethod: PaymentMethod;
      paymentId?: string;
      razorpayOrderId?: string;
      razorpaySignature?: string;
    }
  ): Promise<Order> {
    const preview = await CheckoutService.previewCheckout(userId, {
      addressId: params.addressId,
      items: params.items,
    });

    if (!preview.valid || !preview.address) {
      throw new BadRequestError('Cannot place order: Invalid checkout details or missing delivery address.');
    }

    const now = Date.now();
    const orderId = `ORD${now.toString().slice(-6)}${Math.random().toString(36).substring(2, 5).toUpperCase()}`;

    let paymentStatus: PaymentStatus = 'PENDING';
    let orderStatus: OrderStatus = 'PAYMENT_PENDING';

    if (params.paymentMethod === 'COD') {
      paymentStatus = 'COD_PENDING';
      orderStatus = 'PLACED';
    } else if (params.paymentMethod === 'RAZORPAY' || params.paymentMethod === 'ONLINE_PAYMENT') {
      paymentStatus = 'PAID';
      orderStatus = 'CONFIRMED';
    }

    const order: Order = {
      id: orderId,
      orderNumber: orderId,
      userId,
      items: preview.items,
      subtotal: preview.subtotal,
      shipping: preview.shipping,
      discount: preview.discount,
      tax: preview.tax,
      totalAmount: preview.total,
      currency: preview.currency,
      paymentMethod: params.paymentMethod,
      paymentStatus,
      orderStatus,
      shippingAddress: {
        name: preview.address.name,
        phone: preview.address.phone,
        addressLine: preview.address.addressLine,
        city: preview.address.city,
        state: preview.address.state,
        postalCode: preview.address.postalCode,
        country: preview.address.country,
      },
      deliveryAddressFormatted: `${preview.address.addressLine}, ${preview.address.city}, ${preview.address.state} - ${preview.address.postalCode}`,
      trackingNumber: undefined,
      courierName: undefined,
      estimatedDelivery: preview.items[0]?.product?.deliveryEstimate || 'Estimated delivery unavailable',
      createdAt: now,
      updatedAt: now,
    };

    await this.saveOrder(order);

    if (!params.items || params.items.length === 0) {
      await CartService.clearCart(userId);
    }

    logger.info(`[OrderService] Order created: orderId=${orderId}, userId=${userId}, status=${orderStatus}`);
    return order;
  }

  public static async getOrders(userId: string): Promise<Order[]> {
    const db = getFirestoreDb();
    if (!db) {
      return memoryOrderStore.get(userId) || [];
    }

    const snapshot = await db.collection('orders').where('userId', '==', userId).orderBy('createdAt', 'desc').get();
    return snapshot.docs.map((doc: { data: () => unknown }) => doc.data() as Order);
  }

  public static async getOrderById(userId: string, orderId: string): Promise<Order> {
    const db = getFirestoreDb();
    if (!db) {
      const orders = memoryOrderStore.get(userId) || [];
      const order = orders.find((o) => o.id === orderId);
      if (!order) {
        throw new NotFoundError(`Order with ID ${orderId} not found.`);
      }
      return order;
    }

    const doc = await db.collection('orders').doc(orderId).get();
    if (!doc.exists) {
      throw new NotFoundError(`Order with ID ${orderId} not found.`);
    }

    const order = doc.data() as Order;
    if (order.userId !== userId) {
      throw new NotFoundError(`Order with ID ${orderId} not found.`);
    }

    return order;
  }

  public static async cancelOrder(userId: string, orderId: string): Promise<Order> {
    const order = await this.getOrderById(userId, orderId);

    if (order.orderStatus === 'DELIVERED' || order.orderStatus === 'CANCELLED') {
      throw new BadRequestError(`Cannot cancel order in status: ${order.orderStatus}`);
    }

    order.orderStatus = 'CANCELLED';
    order.paymentStatus = order.paymentStatus === 'PAID' ? 'REFUND_PENDING' : 'FAILED';
    order.updatedAt = Date.now();

    await this.saveOrder(order);
    return order;
  }

  private static async saveOrder(order: Order): Promise<void> {
    const db = getFirestoreDb();
    if (!db) {
      const userOrders = memoryOrderStore.get(order.userId) || [];
      const existingIndex = userOrders.findIndex((o) => o.id === order.id);
      if (existingIndex !== -1) {
        userOrders[existingIndex] = order;
      } else {
        userOrders.unshift(order);
      }
      memoryOrderStore.set(order.userId, userOrders);
      return;
    }

    await db.collection('orders').doc(order.id).set(order);
  }
}
