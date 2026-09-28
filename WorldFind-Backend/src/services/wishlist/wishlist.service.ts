import { WishlistItem, WishlistSummary } from '../../models/Wishlist';
import { AliExpressProductService } from '../aliexpress/aliexpress.product.service';
import { getFirestoreDb } from '../../config/firebase';
import { NotFoundError } from '../../utils/errors';

const memoryWishlistStore = new Map<string, WishlistItem[]>();

export class WishlistService {
  public static async getWishlist(userId: string): Promise<WishlistSummary> {
    const items = await this.getWishlistItems(userId);
    return { userId, items };
  }

  public static async toggleItem(userId: string, productId: string): Promise<WishlistSummary> {
    let items = await this.getWishlistItems(userId);
    const existingIndex = items.findIndex((i) => i.productId === productId);

    if (existingIndex !== -1) {
      items.splice(existingIndex, 1);
    } else {
      const product = await AliExpressProductService.getProductDetail(productId);
      if (!product) {
        throw new NotFoundError(`Product with ID ${productId} not found.`);
      }
      items.push({
        productId,
        addedAt: Date.now(),
        product,
      });
    }

    await this.saveWishlistItems(userId, items);
    return this.getWishlist(userId);
  }

  public static async removeItem(userId: string, productId: string): Promise<WishlistSummary> {
    const items = await this.getWishlistItems(userId);
    const updated = items.filter((i) => i.productId !== productId);
    await this.saveWishlistItems(userId, updated);
    return this.getWishlist(userId);
  }

  private static async getWishlistItems(userId: string): Promise<WishlistItem[]> {
    const db = getFirestoreDb();
    if (!db) {
      return memoryWishlistStore.get(userId) || [];
    }

    const doc = await db.collection('users').doc(userId).collection('wishlist').doc('current').get();
    if (!doc.exists) {
      return [];
    }

    return (doc.data()?.items || []) as WishlistItem[];
  }

  private static async saveWishlistItems(userId: string, items: WishlistItem[]): Promise<void> {
    const db = getFirestoreDb();
    if (!db) {
      memoryWishlistStore.set(userId, items);
      return;
    }

    await db.collection('users').doc(userId).collection('wishlist').doc('current').set({
      items,
      updatedAt: Date.now(),
    });
  }
}
