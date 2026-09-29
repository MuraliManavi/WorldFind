import crypto from 'crypto';

export type SignMethod = 'sha256' | 'hmac' | 'md5';

export interface SignOptions {
  appSecret: string;
  signMethod?: SignMethod;
  apiPath?: string;
}

export class AliExpressSignatureService {
  /**
   * Calculates signature for AliExpress Open Platform TOP API calls according to official protocol standards.
   *
   * @param params Key-value dictionary of all request parameters (excluding 'sign')
   * @param options Configuration including appSecret and signMethod
   * @returns Upper-case Hex signature string
   */
  public static generateSignature(
    params: Record<string, string | number | boolean | undefined | null>,
    options: SignOptions
  ): string {
    const { appSecret, signMethod = 'sha256', apiPath } = options;

    if (!appSecret) {
      throw new Error('App Secret is required for signature generation');
    }

    // 1. Filter out undefined, null, empty strings, and 'sign' parameter
    const validKeys = Object.keys(params).filter((key) => {
      if (key === 'sign') return false;
      const val = params[key];
      return val !== undefined && val !== null && val !== '';
    });

    // 2. Sort parameter keys alphabetically in ASCII byte order
    validKeys.sort();

    // 3. Concatenate key-value pairs without separators: key1value1key2value2...
    let queryStr = '';
    for (const key of validKeys) {
      queryStr += `${key}${params[key]}`;
    }

    const stringToSign = apiPath ? `${apiPath}${queryStr}` : queryStr;
    const methodLower = signMethod.toLowerCase();

    // 4. Compute HMAC/MD5 according to official TOP protocol parameter value:
    // "sha256" -> HMAC-SHA256
    // "hmac"   -> HMAC-MD5
    // "md5"    -> appSecret + stringToSign + appSecret MD5
    if (methodLower === 'sha256' || methodLower === 'hmac-sha256') {
      const hmac = crypto.createHmac('sha256', appSecret);
      hmac.update(stringToSign, 'utf8');
      return hmac.digest('hex').toUpperCase();
    } else if (methodLower === 'hmac') {
      const hmac = crypto.createHmac('md5', appSecret);
      hmac.update(stringToSign, 'utf8');
      return hmac.digest('hex').toUpperCase();
    } else if (methodLower === 'md5') {
      const content = `${appSecret}${stringToSign}${appSecret}`;
      return crypto.createHash('md5').update(content, 'utf8').digest('hex').toUpperCase();
    } else {
      throw new Error(`Unsupported signature method: ${signMethod}`);
    }
  }

  /**
   * Helper to format current timestamp for AliExpress API (YYYY-MM-DD HH:mm:ss in GMT+8 / Beijing time)
   */
  public static getFormattedTimestamp(): string {
    const now = new Date();
    // Convert to GMT+8 (Beijing Time) as required by AliExpress Open Platform
    const beijingMs = now.getTime() + (8 * 60 + now.getTimezoneOffset()) * 60000;
    const beijingTime = new Date(beijingMs);
    const pad = (n: number) => n.toString().padStart(2, '0');
    const year = beijingTime.getFullYear();
    const month = pad(beijingTime.getMonth() + 1);
    const day = pad(beijingTime.getDate());
    const hours = pad(beijingTime.getHours());
    const minutes = pad(beijingTime.getMinutes());
    const seconds = pad(beijingTime.getSeconds());

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  }
}
