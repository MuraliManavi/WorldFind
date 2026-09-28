import { ProductNormalizer, RawAliExpressProduct } from '../src/services/product/product.normalizer';

describe('Product Normalizer & Delivery Parsing Tests', () => {
  it('should correctly parse delivery days from various text formats', () => {
    expect(ProductNormalizer.parseDeliveryDays('ship to IN in 7 days')).toBe(7);
    expect(ProductNormalizer.parseDeliveryDays('7-15 days')).toBe(7);
    expect(ProductNormalizer.parseDeliveryDays('14')).toBe(14);
    expect(ProductNormalizer.parseDeliveryDays(5)).toBe(5);
    expect(ProductNormalizer.parseDeliveryDays('no estimate available')).toBeUndefined();
    expect(ProductNormalizer.parseDeliveryDays('')).toBeUndefined();
    expect(ProductNormalizer.parseDeliveryDays(undefined)).toBeUndefined();
  });

  it('should normalize product payload with INR currency and parsed delivery days', () => {
    const raw: RawAliExpressProduct = {
      product_id: '1005001234567890',
      product_title: 'Noise Cancelling Earbuds',
      product_main_image_url: 'https://ae01.alicdn.com/kf/S1.jpg',
      target_sale_price: '362.50',
      target_sale_price_currency: 'INR',
      target_original_price: '450.00',
      target_original_price_currency: 'INR',
      discount: '19%',
      ship_to_days: 'ship to IN in 7 days',
      evaluate_rate: '96%',
    };

    const normalized = ProductNormalizer.normalizeProduct(raw, 'IN');

    expect(normalized.id).toBe('1005001234567890');
    expect(normalized.price.amount).toBe(362.50);
    expect(normalized.price.currency).toBe('INR');
    expect(normalized.price.formatted).toBe('₹362.50');
    expect(normalized.delivery?.estimatedDays).toBe(7);
    expect(normalized.delivery?.shipToCountry).toBe('IN');
    expect(normalized.rating).toBe(4.8);
  });

  it('should handle minimal raw products without fabricating missing fields', () => {
    const rawMinimal: RawAliExpressProduct = {
      product_id: '8888',
      product_title: 'Simple Item',
      product_main_image_url: 'https://ae01.alicdn.com/kf/S2.jpg',
      target_sale_price: '100',
      target_sale_price_currency: 'INR',
    };

    const normalized = ProductNormalizer.normalizeProduct(rawMinimal, 'IN');

    expect(normalized.id).toBe('8888');
    expect(normalized.delivery).toBeUndefined();
    expect(normalized.shop).toBeUndefined();
    expect(normalized.originalPrice).toBeUndefined();
    expect(normalized.rating).toBeUndefined();
  });
});
