export interface PriceInfo {
  amount: number;
  currency: string;
  formatted?: string;
}

export interface CategoryInfo {
  id: string;
  name?: string;
}

export interface CountryInfo {
  code: string;
  name: string;
}

export interface DeliveryInfo {
  estimatedDays?: number;
  shipToCountry?: string;
}

export interface ShopInfo {
  id?: string;
  name?: string;
  url?: string;
}

export interface WorldFindProduct {
  id: string;
  title: string;
  description?: string;
  imageUrl: string;
  smallImages?: string[];
  category?: CategoryInfo;
  categoryId?: string;
  categoryName?: string;
  firstLevelCategoryId?: string;
  firstLevelCategoryName?: string;
  secondLevelCategoryId?: string;
  secondLevelCategoryName?: string;
  price: PriceInfo;
  originalPrice?: PriceInfo;
  priceInINR: number;
  originalPriceInINR?: number;
  currency: string;
  discountPercent?: number;
  rating?: number;
  reviewCount?: number;
  country: CountryInfo;
  countryId: string;
  countryName: string;
  countryCode: string;
  currencyCode: string;
  currencySymbol: string;
  localPrice: number;
  delivery?: DeliveryInfo;
  estimatedDeliveryDays?: number;
  deliveryEstimate: string;
  shop?: ShopInfo;
  shopUrl?: string;
  videoUrl?: string;
  affiliateUrl?: string;
  availability: boolean;
  source: 'aliexpress';
}

export type Product = WorldFindProduct;

export interface ProductQueryFilter {
  keywords?: string;
  categoryIds?: string;
  minSalePrice?: number;
  maxSalePrice?: number;
  page?: number;
  pageSize?: number;
  sort?: string;
  targetCurrency?: string;
  targetLanguage?: string;
  trackingId?: string;
  promotionName?: string;
  shipToCountry?: string;
  deliveryDays?: number;
  platformProductType?: string;
  fields?: string;
}

export interface PaginatedProductsResponse {
  products: WorldFindProduct[];
  pagination: {
    currentPage: number;
    pageSize: number;
    currentRecordCount: number;
    totalPages: number;
    totalCount: number;
    hasNextPage: boolean;
  };
}
