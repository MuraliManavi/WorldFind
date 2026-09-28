import { aliExpressClient, AliExpressRequestOptions } from '../aliexpress.client';
import { env } from '../../../config/env';

export interface ProtocolRequestPayload {
  apiName: string;
  parameters?: Record<string, string | number | boolean | undefined | null>;
  requireSession?: boolean;
  appSignature?: string;
}

export class AliExpressProtocolAdapter {
  /**
   * Constructs IoP / TOP protocol request parameters according to active OpenService documentation.
   */
  public static buildRequestParams(payload: ProtocolRequestPayload): Record<string, string | number | boolean | undefined | null> {
    const params = { ...payload.parameters };

    // Include app_signature if required by the active OpenService app configuration
    const appSignature = payload.appSignature || env.ALIEXPRESS_APP_SIGNATURE;
    if (appSignature) {
      params.app_signature = appSignature;
    }

    return params;
  }

  /**
   * Executes a protocol-wrapped API call to AliExpress Open Platform.
   */
  public static async executeApiCall<T = unknown>(payload: ProtocolRequestPayload): Promise<T> {
    const finalParams = this.buildRequestParams(payload);

    const options: AliExpressRequestOptions = {
      method: payload.apiName,
      params: finalParams,
      requireSession: payload.requireSession,
    };

    return aliExpressClient.execute<T>(options);
  }
}
