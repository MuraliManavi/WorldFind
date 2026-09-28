import { CheckoutPreviewRequest, CheckoutPreviewResponse } from '../../models/Checkout';
import { CartService } from '../cart/cart.service';
import { AddressService } from '../address/address.service';
import { AliExpressProductService } from '../aliexpress/aliexpress.product.service';
import { CartItem } from '../../models/Cart';

export class CheckoutService {
  public static async previewCheckout(
    userId: string,
    request: CheckoutPreviewRequest
  ): Promise<CheckoutPreviewResponse> {
    let items: CartItem[] = [];

    if (request.items && request.items.length > 0) {
      for (const reqItem of request.items) {
        const product = await AliExpressProductService.getProductDetail(reqItem.productId);
        if (product) {
          items.push({
            id: `direct_${reqItem.productId}`,
            productId: reqItem.productId,
            quantity: reqItem.quantity,
            product,
            addedAt: Date.now(),
          });
        }
      }
    } else {
      const cart = await CartService.getCart(userId);
      items = cart.items;
    }

    const addresses = await AddressService.getAddresses(userId);
    const selectedAddress = request.addressId
      ? addresses.find((a) => a.id === request.addressId) || addresses.find((a) => a.isDefault) || addresses[0]
      : addresses.find((a) => a.isDefault) || addresses[0];

    const subtotal = items.reduce((sum, item) => sum + (item.product.price?.amount || 0) * item.quantity, 0);
    const shipping = 0; // Free delivery
    const discount = 0;
    const tax = 0;
    const total = subtotal + shipping + tax - discount;

    return {
      valid: items.length > 0 && Boolean(selectedAddress),
      items,
      address: selectedAddress,
      subtotal,
      shipping,
      discount,
      tax,
      total,
      currency: 'INR',
      allowedPaymentMethods: ['RAZORPAY', 'COD', 'ONLINE_PAYMENT'],
    };
  }
}
