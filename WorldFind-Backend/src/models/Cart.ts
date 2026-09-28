import { Product } from './Product';

export interface CartItem {
  id: string;
  productId: string;
  quantity: number;
  product: Product;
  addedAt: number;
}

export interface CartSummary {
  userId: string;
  items: CartItem[];
  itemCount: number;
  subtotal: number;
  shipping: number;
  discount: number;
  total: number;
  currency: string;
}
