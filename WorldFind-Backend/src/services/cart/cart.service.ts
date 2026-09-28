import { CartItem, CartSummary } from '../../models/Cart';
import { AliExpressProductService } from '../aliexpress/aliexpress.product.service';
import { getFirestoreDb } from '../../config/firebase';
import { NotFoundError } from '../../utils/errors';

// In-memory fallback for development when Firestore Admin credentials are not initialized
const memoryCartStore = new Map<string, CartItem[]>();

export class CartService {
  public static async getCart(userId: string): Promise<CartSummary> {
    const items = await this.getCartItems(userId);
    const subtotal = items.reduce((sum, item) => sum + (item.product.price?.amount || 0) * item.quantity, 0);
    const shipping = subtotal > 0 ? 0 : 0; // Free shipping for INR
    const discount = 0;
    const total = subtotal + shipping - discount;

    return {
      userId,
      items,
      itemCount: items.reduce((sum, i) => sum + i.quantity, 0),
      subtotal,
      shipping,
      discount,
      total,
      currency: 'INR',
    };
  }

  public static async addItem(userId: string, productId: string, quantity = 1): Promise<CartSummary> {
    const product = await AliExpressProductService.getProductDetail(String(productId));
    if (!product) {
      throw new NotFoundError(`Product with ID ${productId} not found.`);
    }

    const currentItems = await this.getCartItems(userId);
    const existingIndex = currentItems.findIndex((i) => i.productId === String(productId));

    if (existingIndex !== -1) {
      currentItems[existingIndex].quantity += quantity;
    } else {
      currentItems.push({
        id: `cart_${productId}_${Date.now()}`,
        productId: String(productId),
        quantity,
        product,
        addedAt: Date.now(),
      });
    }

    await this.saveCartItems(userId, currentItems);
    return this.getCart(userId);
  }

  public static async updateItemQuantity(userId: string, productId: string, quantity: number): Promise<CartSummary> {
    let currentItems = await this.getCartItems(userId);
    if (quantity <= 0) {
      currentItems = currentItems.filter((i) => i.productId !== productId);
    } else {
      const item = currentItems.find((i) => i.productId === productId);
      if (item) {
        item.quantity = quantity;
      }
    }

    await this.saveCartItems(userId, currentItems);
    return this.getCart(userId);
  }

  public static async removeItem(userId: string, productId: string): Promise<CartSummary> {
    const currentItems = await this.getCartItems(userId);
    const updated = currentItems.filter((i) => i.productId !== productId);
    await this.saveCartItems(userId, updated);
    return this.getCart(userId);
  }

  public static async clearCart(userId: string): Promise<CartSummary> {
    await this.saveCartItems(userId, []);
    return this.getCart(userId);
  }

  private static async getCartItems(userId: string): Promise<CartItem[]> {
    const db = getFirestoreDb();
    if (!db) {
      return memoryCartStore.get(userId) || [];
    }

    const doc = await db.collection('users').doc(userId).collection('cart').doc('current').get();
    if (!doc.exists) {
      return [];
    }

    return (doc.data()?.items || []) as CartItem[];
  }

  private static async saveCartItems(userId: string, items: CartItem[]): Promise<void> {
    const db = getFirestoreDb();
    if (!db) {
      memoryCartStore.set(userId, items);
      return;
    }

    await db.collection('users').doc(userId).collection('cart').doc('current').set({
      items,
      updatedAt: Date.now(),
    });
  }
}
