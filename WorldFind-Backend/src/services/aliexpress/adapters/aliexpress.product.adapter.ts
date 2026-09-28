import { AliExpressProtocolAdapter } from './aliexpress.protocol.adapter';

export class AliExpressProductAdapter {
  /**
   * Product search query using aliexpress.affiliate.product.query
   */
  public static async queryProducts<T = unknown>(params: Record<string, string | number | boolean | undefined | null>): Promise<T> {
    return AliExpressProtocolAdapter.executeApiCall<T>({
      apiName: 'aliexpress.affiliate.product.query',
      parameters: params,
    });
  }

  /**
   * Product detail query using aliexpress.affiliate.productdetail.get
   */
  public static async getProductDetail<T = unknown>(params: Record<string, string | number | boolean | undefined | null>): Promise<T> {
    return AliExpressProtocolAdapter.executeApiCall<T>({
      apiName: 'aliexpress.affiliate.productdetail.get',
      parameters: params,
    });
  }

  /**
   * Generate affiliate tracking links using aliexpress.affiliate.link.generate
   */
  public static async generateAffiliateLinks<T = unknown>(params: Record<string, string | number | boolean | undefined | null>): Promise<T> {
    return AliExpressProtocolAdapter.executeApiCall<T>({
      apiName: 'aliexpress.affiliate.link.generate',
      parameters: params,
    });
  }

  /**
   * Retrieve categories using aliexpress.affiliate.category.get
   */
  public static async getCategories<T = unknown>(params?: Record<string, string | number | boolean | undefined | null>): Promise<T> {
    return AliExpressProtocolAdapter.executeApiCall<T>({
      apiName: 'aliexpress.affiliate.category.get',
      parameters: params,
    });
  }
}
