import { CartItem } from './Cart';

export type OrderStatus =
  | 'PAYMENT_PENDING'
  | 'PAYMENT_FAILED'
  | 'PLACED'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'PACKED'
  | 'SHIPPED'
  | 'IN_TRANSIT'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'CANCEL_REQUESTED'
  | 'CANCELLED'
  | 'REFUND_PENDING'
  | 'REFUNDED';

export type PaymentMethod = 'ONLINE_PAYMENT' | 'RAZORPAY' | 'COD';

export type PaymentStatus =
  | 'PENDING'
  | 'PAID'
  | 'FAILED'
  | 'COD_PENDING'
  | 'COD_CONFIRMED'
  | 'REFUND_PENDING'
  | 'REFUNDED';

export interface Order {
  id: string;
  orderNumber: string;
  userId: string;
  items: CartItem[];
  subtotal: number;
  shipping: number;
  discount: number;
  tax: number;
  totalAmount: number;
  currency: string;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  orderStatus: OrderStatus;
  paymentId?: string;
  shippingAddress: {
    name: string;
    phone: string;
    addressLine: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
  };
  deliveryAddressFormatted: string;
  trackingNumber?: string;
  courierName?: string;
  estimatedDelivery?: string;
  createdAt: number;
  updatedAt: number;
}
