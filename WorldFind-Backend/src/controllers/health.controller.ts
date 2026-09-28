import { Request, Response } from 'express';
import { env } from '../config/env';

export class HealthController {
  public static getHealth(_req: Request, res: Response): void {
    res.status(200).json({
      success: true,
      service: 'WorldFind Backend',
      status: 'healthy',
      timestamp: new Date().toISOString(),
      environment: env.NODE_ENV,
    });
  }

  public static getRootInfo(_req: Request, res: Response): void {
    res.status(200).json({
      success: true,
      service: 'WorldFind Backend API',
      description: 'Backend API for WorldFind India-focused shopping application powered by AliExpress Open Platform',
      version: '1.0.0',
      docs: {
        health: '/health',
        products: '/api/products',
        categories: '/api/categories',
        oauthStatus: '/api/aliexpress/token-status',
      },
    });
  }
}
