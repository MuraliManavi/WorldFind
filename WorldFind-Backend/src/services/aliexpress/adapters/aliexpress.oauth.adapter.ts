import { AliExpressOAuthService } from '../aliexpress.oauth.service';
import { AliExpressTokenData } from '../../../models/AliExpressToken';

export class AliExpressOAuthAdapter {
  /**
   * Generates authorization URL with state parameter
   */
  public static async getAuthUrl(): Promise<{ authUrl: string; state: string }> {
    return AliExpressOAuthService.getAuthorizationUrl();
  }

  /**
   * Verifies state and exchanges authorization code for tokens
   */
  public static async exchangeCodeForToken(code: string, state: string, uuid?: string): Promise<AliExpressTokenData> {
    const isValidState = await AliExpressOAuthService.verifyAndConsumeState(state);
    if (!isValidState) {
      throw new Error('OAuth state parameter is invalid, expired, or already consumed.');
    }
    return AliExpressOAuthService.exchangeCodeForToken(code, uuid);
  }

  /**
   * Refreshes OAuth access token
   */
  public static async refreshToken(refreshToken: string): Promise<AliExpressTokenData> {
    return AliExpressOAuthService.refreshToken(refreshToken);
  }
}
