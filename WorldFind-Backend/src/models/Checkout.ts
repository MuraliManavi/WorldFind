import { CartItem } from './Cart';
import { Address } from './Address';

export interface CheckoutPreviewRequest {
  addressId?: string;
  items?: { productId: string; quantity: number }[];
  promoCode?: string;
}

export interface CheckoutPreviewResponse {
  valid: boolean;
  items: CartItem[];
  address?: Address;
  subtotal: number;
  shipping: number;
  discount: number;
  tax: number;
  total: number;
  currency: string;
  allowedPaymentMethods: string[];
}
