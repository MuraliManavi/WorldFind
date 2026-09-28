import { Product } from './Product';

export interface WishlistItem {
  productId: string;
  addedAt: number;
  product: Product;
}

export interface WishlistSummary {
  userId: string;
  items: WishlistItem[];
}
