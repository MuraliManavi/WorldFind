import { AliExpressProductAdapter } from './adapters/aliexpress.product.adapter';
import { ProductQueryFilter, PaginatedProductsResponse, WorldFindProduct } from '../../models/Product';
import { ProductNormalizer, RawAliExpressProduct } from '../product/product.normalizer';
import { env } from '../../config/env';
import { logger } from '../../utils/logger';

const DEFAULT_PRODUCT_FIELDS = [
  'app_sale_price',
  'commission_rate',
  'discount',
  'evaluate_rate',
  'first_level_category_id',
  'first_level_category_name',
  'original_price',
  'product_detail_url',
  'product_id',
  'product_main_image_url',
  'product_small_image_urls',
  'product_title',
  'product_video_url',
  'promotion_link',
  'second_level_category_id',
  'second_level_category_name',
  'shop_id',
  'shop_url',
  'target_app_sale_price',
  'target_app_sale_price_currency',
  'target_original_price',
  'target_original_price_currency',
  'target_sale_price',
  'target_sale_price_currency',
  'ship_to_days',
  'total_page_no',
  'total_record_count',
].join(',');

export class AliExpressProductService {
  /**
   * Query AliExpress products using aliexpress.affiliate.product.query
   */
  public static async queryProducts(filter: ProductQueryFilter): Promise<PaginatedProductsResponse> {
    const shipToCountry = filter.shipToCountry || env.DEFAULT_SHIP_TO_COUNTRY;
    const targetCurrency = filter.targetCurrency || env.DEFAULT_TARGET_CURRENCY;
    const targetLanguage = filter.targetLanguage || env.DEFAULT_TARGET_LANGUAGE;
    const trackingId = filter.trackingId || env.DEFAULT_TRACKING_ID;
    const fields = filter.fields || DEFAULT_PRODUCT_FIELDS;

    const apiParams: Record<string, string | number | undefined> = {
      keywords: filter.keywords,
      category_ids: filter.categoryIds,
      min_sale_price: filter.minSalePrice,
      max_sale_price: filter.maxSalePrice,
      page_no: filter.page || 1,
      page_size: filter.pageSize || 20,
      sort: filter.sort,
      target_currency: targetCurrency,
      target_language: targetLanguage,
      tracking_id: trackingId,
      promotion_name: filter.promotionName,
      ship_to_country: shipToCountry,
      delivery_days: filter.deliveryDays,
      platform_product_type: filter.platformProductType,
      fields: fields,
    };

    logger.info(`Querying AliExpress products: keywords="${filter.keywords || ''}", ship_to_country=${shipToCountry}, currency=${targetCurrency}`);

    const response = await AliExpressProductAdapter.queryProducts<{
      resp_result?: {
        result?: {
          products?: { product?: RawAliExpressProduct[] | RawAliExpressProduct } | RawAliExpressProduct[];
          total_record_count?: number;
          total_page_no?: number;
          current_record_count?: number;
          current_page_no?: number;
        };
      };
      result?: {
        products?: { product?: RawAliExpressProduct[] | RawAliExpressProduct } | RawAliExpressProduct[];
        total_record_count?: number;
        total_page_no?: number;
        current_record_count?: number;
      };
    }>(apiParams);

    let rawList: RawAliExpressProduct[] = [];
    let totalCount = 0;
    const pageSize = filter.pageSize || 20;
    const currentPage = filter.page || 1;
    let totalPages = 1;
    let currentRecordCount = 0;

    const resultObj = response.resp_result?.result || response.result;
    if (resultObj) {
      totalCount = resultObj.total_record_count || 0;
      totalPages = resultObj.total_page_no || Math.ceil(totalCount / pageSize) || 1;

      if (Array.isArray(resultObj.products)) {
        rawList = resultObj.products;
      } else if (resultObj.products && 'product' in resultObj.products) {
        const prod = resultObj.products.product;
        rawList = Array.isArray(prod) ? prod : prod ? [prod] : [];
      }

      currentRecordCount = resultObj.current_record_count || rawList.length;
    }

    const normalizedProducts = ProductNormalizer.normalizeProductsList(rawList, shipToCountry);
    const hasNextPage = currentPage < totalPages || (totalCount > 0 && currentPage * pageSize < totalCount);

    return {
      products: normalizedProducts,
      pagination: {
        currentPage,
        pageSize,
        currentRecordCount,
        totalPages,
        totalCount,
        hasNextPage,
      },
    };
  }

  /**
   * Fetch details for a specific AliExpress product using aliexpress.affiliate.productdetail.get
   * with fallback to aliexpress.affiliate.product.query
   */
  public static async getProductDetail(
    productId: string,
    shipToCountry = env.DEFAULT_SHIP_TO_COUNTRY,
    targetCurrency = env.DEFAULT_TARGET_CURRENCY,
    targetLanguage = env.DEFAULT_TARGET_LANGUAGE,
    trackingId = env.DEFAULT_TRACKING_ID
  ): Promise<WorldFindProduct | null> {
    logger.info(`Fetching product details: productId=${productId}`);

    // First try aliexpress.affiliate.productdetail.get
    try {
      const response = await AliExpressProductAdapter.getProductDetail<{
        resp_result?: {
          result?: {
            products?: { product?: RawAliExpressProduct[] | RawAliExpressProduct } | RawAliExpressProduct[];
          };
        };
      }>({
        product_ids: productId,
        target_currency: targetCurrency,
        target_language: targetLanguage,
        tracking_id: trackingId,
        country: shipToCountry,
        fields: DEFAULT_PRODUCT_FIELDS,
      });

      let rawList: RawAliExpressProduct[] = [];
      const resultObj = response.resp_result?.result;

      if (resultObj && resultObj.products) {
        if (Array.isArray(resultObj.products)) {
          rawList = resultObj.products;
        } else if ('product' in resultObj.products) {
          const prod = resultObj.products.product;
          rawList = Array.isArray(prod) ? prod : prod ? [prod] : [];
        }
      }

      if (rawList.length > 0) {
        return ProductNormalizer.normalizeProduct(rawList[0], shipToCountry);
      }
    } catch (err) {
      logger.warn(`getProductDetail via productdetail.get failed/unsupported, trying product.query fallback for productId=${productId}:`, err);
    }

    // Fallback via aliexpress.affiliate.product.query with product_ids parameter
    try {
      const response = await AliExpressProductAdapter.queryProducts<{
        resp_result?: {
          result?: {
            products?: { product?: RawAliExpressProduct[] | RawAliExpressProduct } | RawAliExpressProduct[];
          };
        };
        result?: {
          products?: { product?: RawAliExpressProduct[] | RawAliExpressProduct } | RawAliExpressProduct[];
        };
      }>({
        product_ids: productId,
        target_currency: targetCurrency,
        target_language: targetLanguage,
        tracking_id: trackingId,
        ship_to_country: shipToCountry,
        fields: DEFAULT_PRODUCT_FIELDS,
      });

      const resultObj = response.resp_result?.result || response.result;
      let rawList: RawAliExpressProduct[] = [];
      if (resultObj && resultObj.products) {
        if (Array.isArray(resultObj.products)) {
          rawList = resultObj.products;
        } else if ('product' in resultObj.products) {
          const prod = resultObj.products.product;
          rawList = Array.isArray(prod) ? prod : prod ? [prod] : [];
        }
      }

      if (rawList.length > 0) {
        return ProductNormalizer.normalizeProduct(rawList[0], shipToCountry);
      }
    } catch (err) {
      logger.error(`Fallback product.query failed for productId=${productId}:`, err);
    }

    return null;
  }

  /**
   * Generate affiliate tracking links using aliexpress.affiliate.link.generate
   */
  public static async generateAffiliateLinks(
    sourceUrls: string[],
    promotionLinkType = 0,
    trackingId = env.DEFAULT_TRACKING_ID
  ): Promise<Array<{ sourceUrl: string; promotionUrl: string }>> {
    logger.info(`Generating affiliate links for ${sourceUrls.length} URL(s)`);

    const response = await AliExpressProductAdapter.generateAffiliateLinks<{
      resp_result?: {
        result?: {
          promotion_links?: {
            promotion_link?: Array<{ source_value: string; promotion_link: string }>;
          };
        };
      };
    }>({
      promotion_link_type: promotionLinkType,
      source_values: sourceUrls.join(','),
      tracking_id: trackingId,
    });

    const links = response.resp_result?.result?.promotion_links?.promotion_link || [];
    return links.map((l) => ({
      sourceUrl: l.source_value,
      promotionUrl: l.promotion_link,
    }));
  }

  /**
   * Fetch AliExpress affiliate categories using aliexpress.affiliate.category.get
   */
  public static async getCategories(categoryId?: string): Promise<Array<{ id: string; name: string; parentId?: string }>> {
    logger.info(`Fetching categories, parent categoryId=${categoryId || 'root'}`);

    const params: Record<string, string> = {};
    if (categoryId) {
      params.category_id = categoryId;
    }

    const response = await AliExpressProductAdapter.getCategories<{
      resp_result?: {
        result?: {
          categories?: {
            category?: Array<{
              category_id: number | string;
              category_name: string;
              parent_category_id?: number | string;
            }>;
          };
        };
      };
    }>(params);

    const catList = response.resp_result?.result?.categories?.category || [];
    return catList.map((c) => ({
      id: String(c.category_id),
      name: c.category_name,
      parentId: c.parent_category_id ? String(c.parent_category_id) : undefined,
    }));
  }
}
