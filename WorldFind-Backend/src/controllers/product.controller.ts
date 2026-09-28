import { Request, Response, NextFunction } from 'express';
import { z } from 'zod';
import { AliExpressProductService } from '../services/aliexpress/aliexpress.product.service';
import { BadRequestError, NotFoundError, ValidationError } from '../utils/errors';
import { ProductQueryFilter } from '../models/Product';

const SUPPORTED_CURRENCIES = [
  'USD', 'GBP', 'CAD', 'EUR', 'UAH', 'MXN', 'TRY', 'RUB',
  'BRL', 'AUD', 'INR', 'JPY', 'IDR', 'SEK', 'KRW', 'ILS',
  'THB', 'CLP', 'VND',
] as const;

const SUPPORTED_SORT_VALUES = [
  'SALE_PRICE_ASC', 'SALE_PRICE_DESC',
  'LAST_VOLUME_ASC', 'LAST_VOLUME_DESC',
  'EVALUATE_RATE_ASC', 'EVALUATE_RATE_DESC',
  'COMMISSION_RATE_ASC', 'COMMISSION_RATE_DESC',
] as const;

const SUPPORTED_PLATFORM_TYPES = ['ALL', 'PLAZA', 'TMALL'] as const;

const SUPPORTED_LANGUAGES = [
  'EN', 'RU', 'PT', 'ES', 'FR', 'ID', 'IT', 'TH',
  'TR', 'VI', 'DE', 'HE', 'JA', 'KO', 'NL', 'PL', 'AR',
] as const;

const productQuerySchema = z
  .object({
    keywords: z.string().max(200, 'keywords parameter exceeds maximum length of 200').optional(),
    categoryIds: z
      .string()
      .regex(/^\d+(,\d+)*$/, 'categoryIds must be a comma-separated list of numeric IDs')
      .optional(),
    minSalePrice: z
      .string()
      .optional()
      .transform((v) => (v !== undefined && v !== '' ? parseFloat(v) : undefined))
      .refine((val) => val === undefined || (!isNaN(val) && val >= 0), 'minSalePrice must be a number >= 0'),
    maxSalePrice: z
      .string()
      .optional()
      .transform((v) => (v !== undefined && v !== '' ? parseFloat(v) : undefined))
      .refine((val) => val === undefined || (!isNaN(val) && val >= 0), 'maxSalePrice must be a number >= 0'),
    page: z
      .string()
      .optional()
      .transform((v) => (v ? parseInt(v, 10) : 1))
      .refine((val) => val >= 1, 'page must be an integer >= 1'),
    pageSize: z
      .string()
      .optional()
      .transform((v) => (v ? parseInt(v, 10) : 20))
      .refine((val) => val >= 1 && val <= 100, 'pageSize must be an integer between 1 and 100'),
    sort: z
      .string()
      .toUpperCase()
      .refine((val) => (SUPPORTED_SORT_VALUES as readonly string[]).includes(val), {
        message: `sort must be one of: ${SUPPORTED_SORT_VALUES.join(', ')}`,
      })
      .optional(),
    targetCurrency: z
      .string()
      .toUpperCase()
      .refine((val) => (SUPPORTED_CURRENCIES as readonly string[]).includes(val), {
        message: `targetCurrency must be one of: ${SUPPORTED_CURRENCIES.join(', ')}`,
      })
      .optional(),
    targetLanguage: z
      .string()
      .toUpperCase()
      .refine((val) => (SUPPORTED_LANGUAGES as readonly string[]).includes(val), {
        message: `targetLanguage must be one of: ${SUPPORTED_LANGUAGES.join(', ')}`,
      })
      .optional(),
    trackingId: z.string().max(50).optional(),
    promotionName: z.string().max(100).optional(),
    shipToCountry: z
      .string()
      .regex(/^[A-Za-z]{2}$/, 'shipToCountry must be a 2-letter ISO country code (e.g. IN)')
      .transform((v) => v.toUpperCase())
      .optional(),
    deliveryDays: z
      .string()
      .optional()
      .transform((v) => (v !== undefined && v !== '' ? parseInt(v, 10) : undefined))
      .refine((val) => val === undefined || (!isNaN(val) && val > 0), 'deliveryDays must be a positive integer'),
    platformProductType: z
      .string()
      .toUpperCase()
      .refine((val) => (SUPPORTED_PLATFORM_TYPES as readonly string[]).includes(val), {
        message: `platformProductType must be one of: ${SUPPORTED_PLATFORM_TYPES.join(', ')}`,
      })
      .optional(),
    fields: z.string().max(1000).optional(),
  })
  .refine(
    (data) => {
      if (data.minSalePrice !== undefined && data.maxSalePrice !== undefined) {
        return data.maxSalePrice >= data.minSalePrice;
      }
      return true;
    },
    {
      message: 'maxSalePrice must be greater than or equal to minSalePrice',
      path: ['maxSalePrice'],
    }
  );

const affiliateLinkBodySchema = z.object({
  urls: z.array(z.string().url('Must provide valid URLs')).min(1, 'At least one URL required'),
  promotionLinkType: z.number().optional().default(0),
  trackingId: z.string().optional(),
});

export class ProductController {
  /**
   * GET /api/products
   * Search and filter products
   */
  public static async getProducts(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const parsed = productQuerySchema.safeParse(req.query);
      if (!parsed.success) {
        throw new ValidationError('Invalid product query parameters', parsed.error.format());
      }

      const filter: ProductQueryFilter = parsed.data;
      const result = await AliExpressProductService.queryProducts(filter);

      res.status(200).json({
        success: true,
        data: result,
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * GET /api/products/:productId
   * Get detail for a specific product
   */
  public static async getProductById(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const paramId = req.params.productId;
      const productId = Array.isArray(paramId) ? paramId[0] : String(paramId || '');
      if (!productId) {
        throw new BadRequestError('productId path parameter is required.');
      }

      const shipToCountry = (req.query.shipToCountry as string) || undefined;
      const targetCurrency = (req.query.targetCurrency as string) || undefined;

      const product = await AliExpressProductService.getProductDetail(
        productId,
        shipToCountry,
        targetCurrency
      );

      if (!product) {
        throw new NotFoundError(`Product with ID ${productId} was not found on AliExpress.`);
      }

      res.status(200).json({
        success: true,
        data: product,
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * POST /api/products/affiliate-link
   * Generate affiliate tracking links for given product URLs
   */
  public static async generateAffiliateLinks(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const parsed = affiliateLinkBodySchema.safeParse(req.body);
      if (!parsed.success) {
        throw new ValidationError('Invalid request body for affiliate link generation', parsed.error.format());
      }

      const { urls, promotionLinkType, trackingId } = parsed.data;
      const links = await AliExpressProductService.generateAffiliateLinks(urls, promotionLinkType, trackingId);

      res.status(200).json({
        success: true,
        data: links,
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * GET /api/categories
   * Get product categories
   */
  public static async getCategories(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const categoryId = req.query.categoryId as string | undefined;
      const categories = await AliExpressProductService.getCategories(categoryId);

      res.status(200).json({
        success: true,
        data: categories,
      });
    } catch (error) {
      next(error);
    }
  }
}
