import { WorldFindProduct, CountryInfo, DeliveryInfo, PriceInfo, ShopInfo } from '../../models/Product';

export interface RawAliExpressProduct {
  product_id?: string | number;
  target_product_id?: string | number;
  product_title?: string;
  title?: string;
  product_main_image_url?: string;
  main_image_url?: string;
  product_small_image_urls?: { string?: string[] | string } | string[] | string;
  first_level_category_id?: string | number;
  first_level_category_name?: string;
  second_level_category_id?: string | number;
  second_level_category_name?: string;
  target_sale_price?: string | number;
  target_sale_price_currency?: string;
  target_app_sale_price?: string | number;
  target_app_sale_price_currency?: string;
  target_original_price?: string | number;
  target_original_price_currency?: string;
  app_sale_price?: string | number;
  sale_price?: string | number;
  original_price?: string | number;
  discount?: string | number;
  evaluate_rate?: string | number;
  ship_to_days?: string | number;
  delivery_days?: string | number;
  shop_id?: string | number;
  shop_name?: string;
  shop_url?: string;
  product_video_url?: string;
  product_detail_url?: string;
  promotion_link?: string;
  affiliate_url?: string;
  platform_product_type?: string;
}

export class ProductNormalizer {
  /**
   * Format currency amount with currency symbol if available
   */
  private static formatPrice(amount: number, currency: string): string {
    const symbols: Record<string, string> = {
      INR: '₹',
      USD: '$',
      EUR: '€',
      GBP: '£',
    };
    const symbol = symbols[currency] || `${currency} `;
    return `${symbol}${amount.toFixed(2)}`;
  }

  /**
   * Robustly parses delivery days from strings like "ship to IN in 7 days", "7-15 days", or numbers.
   */
  public static parseDeliveryDays(input?: string | number): number | undefined {
    if (input === undefined || input === null) return undefined;
    const str = String(input).trim();
    if (!str) return undefined;

    const match = str.match(/\b(\d+)\b/);
    if (match) {
      const parsed = parseInt(match[1], 10);
      if (!isNaN(parsed) && parsed > 0 && parsed < 365) {
        return parsed;
      }
    }
    return undefined;
  }

  /**
   * Normalizes raw AliExpress product payload into WorldFindProduct DTO
   */
  public static normalizeProduct(raw: RawAliExpressProduct, targetCountry = 'IN'): WorldFindProduct {
    const id = String(raw.product_id || raw.target_product_id || '');
    const title = raw.product_title || raw.title || 'Product';
    const imageUrl = raw.product_main_image_url || raw.main_image_url || '';

    // Small images array parsing
    let smallImages: string[] = [];
    if (raw.product_small_image_urls) {
      if (Array.isArray(raw.product_small_image_urls)) {
        smallImages = raw.product_small_image_urls;
      } else if (typeof raw.product_small_image_urls === 'object' && raw.product_small_image_urls.string) {
        smallImages = Array.isArray(raw.product_small_image_urls.string)
          ? raw.product_small_image_urls.string
          : [raw.product_small_image_urls.string];
      } else if (typeof raw.product_small_image_urls === 'string') {
        smallImages = [raw.product_small_image_urls];
      }
    }

    // Target currency and sale amounts
    const currency = raw.target_app_sale_price_currency || raw.target_sale_price_currency || raw.target_original_price_currency || 'INR';
    const rawSale = raw.target_app_sale_price || raw.target_sale_price || raw.app_sale_price || raw.sale_price || 0;
    const rawOriginal = raw.target_original_price || raw.original_price || 0;

    const saleAmount = parseFloat(String(rawSale));
    const originalAmount = parseFloat(String(rawOriginal));

    const price: PriceInfo = {
      amount: saleAmount,
      currency: currency,
      formatted: this.formatPrice(saleAmount, currency),
    };

    let originalPrice: PriceInfo | undefined = undefined;
    if (originalAmount > saleAmount && originalAmount > 0) {
      originalPrice = {
        amount: originalAmount,
        currency: currency,
        formatted: this.formatPrice(originalAmount, currency),
      };
    }

    // Discount percentage
    let discountPercent: number | undefined = undefined;
    if (raw.discount) {
      const parsedDiscount = parseFloat(String(raw.discount).replace('%', ''));
      if (!isNaN(parsedDiscount)) {
        discountPercent = Math.round(parsedDiscount);
      }
    } else if (originalAmount > saleAmount && saleAmount > 0) {
      discountPercent = Math.round(((originalAmount - saleAmount) / originalAmount) * 100);
    }

    // Category mapping
    const categoryId = String(raw.second_level_category_id || raw.first_level_category_id || '');
    const categoryName = raw.second_level_category_name || raw.first_level_category_name || undefined;
    const firstLevelCategoryId = raw.first_level_category_id ? String(raw.first_level_category_id) : undefined;
    const firstLevelCategoryName = raw.first_level_category_name || undefined;
    const secondLevelCategoryId = raw.second_level_category_id ? String(raw.second_level_category_id) : undefined;
    const secondLevelCategoryName = raw.second_level_category_name || undefined;

    // Country info
    const countryNames: Record<string, string> = {
      IN: 'India',
      US: 'United States',
      UK: 'United Kingdom',
      CA: 'Canada',
      AU: 'Australia',
    };

    const countryNameStr = countryNames[targetCountry] || targetCountry;

    const country: CountryInfo = {
      code: targetCountry,
      name: countryNameStr,
    };

    // Delivery info
    const parsedDays = this.parseDeliveryDays(raw.ship_to_days || raw.delivery_days);
    let delivery: DeliveryInfo | undefined = undefined;
    let deliveryEstimate = 'Estimated delivery unavailable';
    if (parsedDays !== undefined) {
      delivery = {
        estimatedDays: parsedDays,
        shipToCountry: targetCountry,
      };
      deliveryEstimate = `${parsedDays} days`;
    }

    // Rating
    let rating: number | undefined = undefined;
    if (raw.evaluate_rate) {
      const parsedRate = parseFloat(String(raw.evaluate_rate).replace('%', ''));
      if (!isNaN(parsedRate)) {
        rating = parsedRate > 5 ? parseFloat(((parsedRate / 100) * 5).toFixed(1)) : parsedRate;
      }
    }

    // Shop info
    let shop: ShopInfo | undefined = undefined;
    if (raw.shop_id || raw.shop_url || raw.shop_name) {
      shop = {
        id: raw.shop_id ? String(raw.shop_id) : undefined,
        name: raw.shop_name || undefined,
        url: raw.shop_url || undefined,
      };
    }

    const currencySymbol = currency === 'INR' ? '₹' : this.formatPrice(0, currency).replace(/[\d\.\s]/g, '') || currency;

    return {
      id,
      title,
      description: title,
      imageUrl,
      smallImages: smallImages.length > 0 ? smallImages : undefined,
      category: categoryId ? { id: categoryId, name: categoryName } : undefined,
      categoryId: categoryId || undefined,
      categoryName: categoryName || undefined,
      firstLevelCategoryId,
      firstLevelCategoryName,
      secondLevelCategoryId,
      secondLevelCategoryName,
      price,
      originalPrice,
      priceInINR: saleAmount,
      originalPriceInINR: originalAmount > saleAmount ? originalAmount : undefined,
      currency,
      discountPercent,
      rating,
      reviewCount: undefined,
      country,
      countryId: targetCountry,
      countryName: countryNameStr,
      countryCode: targetCountry,
      currencyCode: currency,
      currencySymbol,
      localPrice: saleAmount,
      delivery,
      estimatedDeliveryDays: parsedDays,
      deliveryEstimate,
      shop,
      shopUrl: raw.shop_url || undefined,
      videoUrl: raw.product_video_url || undefined,
      affiliateUrl: raw.promotion_link || raw.affiliate_url || raw.product_detail_url || undefined,
      availability: true,
      source: 'aliexpress',
    };
  }

  /**
   * Normalizes an array of raw AliExpress product objects
   */
  public static normalizeProductsList(rawProducts: RawAliExpressProduct[], targetCountry = 'IN'): WorldFindProduct[] {
    if (!Array.isArray(rawProducts)) {
      return [];
    }
    return rawProducts.map((p) => this.normalizeProduct(p, targetCountry));
  }
}
