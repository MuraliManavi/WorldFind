import { AliExpressProductAdapter } from './adapters/aliexpress.product.adapter';
import { ProductQueryFilter, PaginatedProductsResponse, WorldFindProduct } from '../../models/Product';
import { ProductNormalizer, RawAliExpressProduct } from '../product/product.normalizer';
import { env } from '../../config/env';
import { logger } from '../../utils/logger';

export class AliExpressProductService {
  /**
   * Safe recursive product list and pagination extractor supporting all TOP/IOP response shapes
   */
  private static extractProductsAndPagination(payload: any): {
    products: RawAliExpressProduct[];
    totalCount: number;
    totalPages: number;
  } {
    if (!payload || typeof payload !== 'object') {
      return { products: [], totalCount: 0, totalPages: 1 };
    }

    // Resolve result container object
    const resultObj =
      payload.resp_result?.result ||
      payload.result?.resp_result ||
      payload.result ||
      payload.resp_result ||
      payload;

    const totalCount =
      resultObj.total_record_count ||
      payload.total_record_count ||
      payload.resp_result?.result?.total_record_count ||
      0;

    const totalPages =
      resultObj.total_page_no ||
      payload.total_page_no ||
      payload.resp_result?.result?.total_page_no ||
      1;

    // Resolve products container
    const productsContainer = resultObj.products || payload.products || resultObj;

    let rawList: RawAliExpressProduct[] = [];

    if (Array.isArray(productsContainer)) {
      rawList = productsContainer;
    } else if (productsContainer && typeof productsContainer === 'object') {
      if (Array.isArray(productsContainer.product)) {
        rawList = productsContainer.product;
      } else if (productsContainer.product && typeof productsContainer.product === 'object') {
        rawList = [productsContainer.product];
      } else if ('product_id' in productsContainer || 'target_product_id' in productsContainer) {
        rawList = [productsContainer];
      }
    }

    return { products: rawList, totalCount, totalPages };
  }

  /**
   * Query AliExpress products using aliexpress.affiliate.product.query
   */
  public static async queryProducts(filter: ProductQueryFilter): Promise<PaginatedProductsResponse> {
    const shipToCountry = filter.shipToCountry || env.DEFAULT_SHIP_TO_COUNTRY;
    const targetCurrency = filter.targetCurrency || env.DEFAULT_TARGET_CURRENCY;
    const targetLanguage = filter.targetLanguage || env.DEFAULT_TARGET_LANGUAGE;
    const trackingId = filter.trackingId || env.DEFAULT_TRACKING_ID;

    // Default search keyword 'trending' when neither keywords nor category_ids are specified
    const keywords = filter.keywords || (filter.categoryIds ? undefined : 'trending');

    const apiParams: Record<string, string | number | undefined> = {
      keywords: keywords,
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
      fields: filter.fields || undefined,
    };

    logger.info(`[AliExpressProductService] Querying AliExpress products:`, {
      keywords: keywords || 'N/A',
      categoryIds: filter.categoryIds || 'N/A',
      shipToCountry,
      targetCurrency,
      targetLanguage,
      page: filter.page || 1,
      pageSize: filter.pageSize || 20,
    });

    const response = await AliExpressProductAdapter.queryProducts<any>(apiParams);

    // Safe diagnostic logging of raw response shape (no secrets)
    logger.info(`[AliExpressProductService] Raw response top-level structure:`, {
      hasRespResult: Boolean(response?.resp_result),
      hasResult: Boolean(response?.result || response?.resp_result?.result),
      hasProductsContainer: Boolean(
        response?.products || response?.result?.products || response?.resp_result?.result?.products
      ),
      topKeys: response && typeof response === 'object' ? Object.keys(response) : [],
    });

    const { products: rawList, totalCount, totalPages } = this.extractProductsAndPagination(response);

    const pageSize = filter.pageSize || 20;
    const currentPage = filter.page || 1;
    const currentRecordCount = rawList.length;

    logger.info(`[AliExpressProductService] Extraction complete: rawProductsCount=${rawList.length}, totalCount=${totalCount}, totalPages=${totalPages}`);

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
      const response = await AliExpressProductAdapter.getProductDetail<any>({
        product_ids: productId,
        target_currency: targetCurrency,
        target_language: targetLanguage,
        tracking_id: trackingId,
        country: shipToCountry,
      });

      const { products: rawList } = this.extractProductsAndPagination(response);
      if (rawList.length > 0) {
        return ProductNormalizer.normalizeProduct(rawList[0], shipToCountry);
      }
    } catch (err) {
      logger.warn(`getProductDetail via productdetail.get failed/unsupported, trying product.query fallback for productId=${productId}:`, err);
    }

    // Fallback via aliexpress.affiliate.product.query with product_ids parameter
    try {
      const response = await AliExpressProductAdapter.queryProducts<any>({
        product_ids: productId,
        target_currency: targetCurrency,
        target_language: targetLanguage,
        tracking_id: trackingId,
        ship_to_country: shipToCountry,
      });

      const { products: rawList } = this.extractProductsAndPagination(response);
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
