export interface AliExpressTokenData {
  access_token: string;
  refresh_token: string;
  expires_in: number; // in seconds
  refresh_expires_in?: number; // in seconds
  expire_time: number; // unix timestamp in ms
  refresh_token_valid_time?: number; // unix timestamp in ms
  account_id?: string;
  user_id?: string;
  user_nick?: string;
  account?: string;
  locale?: string;
  sp?: string; // service provider
  w1_expires_in?: number;
  w2_expires_in?: number;
  r1_expires_in?: number;
  r2_expires_in?: number;
  updatedAt: number; // unix timestamp in ms
}

export interface EncryptedTokenRecord {
  encryptedAccessToken: string;
  accessTokenIv: string;
  accessTokenAuthTag: string;

  encryptedRefreshToken: string;
  refreshTokenIv: string;
  refreshTokenAuthTag: string;

  expireTime: number;
  refreshTokenValidTime: number;
  accountId?: string;
  updatedAt: number;
}

export interface OAuthStateRecord {
  state: string;
  createdAt: number;
  expiresAt: number;
}
