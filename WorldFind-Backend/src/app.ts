import express, { Express, Request, Response, NextFunction } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import crypto from 'crypto';
import { env } from './config/env';
import healthRoutes from './routes/health.routes';
import aliexpressRoutes from './routes/aliexpress.routes';
import productRoutes from './routes/product.routes';
import cartRoutes from './routes/cart.routes';
import wishlistRoutes from './routes/wishlist.routes';
import addressRoutes from './routes/address.routes';
import checkoutRoutes from './routes/checkout.routes';
import paymentRoutes from './routes/payment.routes';
import orderRoutes from './routes/order.routes';
import { errorHandler, ExtendedRequest } from './middleware/error.middleware';
import { apiRateLimiter } from './middleware/rateLimit.middleware';
import { logger } from './utils/logger';
import { NotFoundError } from './utils/errors';

export const createApp = (): Express => {
  const app = express();

  // Security Headers
  app.use(helmet());

  // CORS Configuration
  const allowedOrigins = env.CORS_ORIGIN === '*'
    ? '*'
    : env.CORS_ORIGIN.split(',').map((origin) => origin.trim());

  app.use(
    cors({
      origin: (requestOrigin, callback) => {
        // Native mobile apps (Retrofit/OkHttp), server-to-server calls, or curl do not send an Origin header
        if (!requestOrigin) return callback(null, true);
        if (allowedOrigins === '*' || allowedOrigins.includes(requestOrigin)) {
          return callback(null, true);
        }
        return callback(null, false);
      },
      credentials: true,
      methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
      allowedHeaders: ['Content-Type', 'Authorization', 'x-admin-key', 'X-AliExpress-Signature'],
    })
  );

  // Request ID injection
  app.use((req: ExtendedRequest, _res: Response, next: NextFunction) => {
    req.id = crypto.randomUUID();
    next();
  });

  // Body Parsing with Raw Body Preservation for Webhook Signature Verification
  app.use(
    express.json({
      limit: '2mb',
      verify: (req: ExtendedRequest, _res: Response, buf: Buffer) => {
        req.rawBody = buf;
      },
    })
  );
  app.use(express.urlencoded({ extended: true, limit: '2mb' }));

  // Request Logging
  app.use((req: ExtendedRequest, res: Response, next: NextFunction) => {
    const startTime = Date.now();
    res.on('finish', () => {
      const duration = Date.now() - startTime;
      logger.info(
        `[${req.id}] ${req.method} ${req.originalUrl} - Status: ${res.statusCode} (${duration}ms)`
      );
    });
    next();
  });

  // Global Rate Limiter
  app.use('/api', apiRateLimiter);

  // Mount Routes
  app.use('/', healthRoutes);
  app.use('/api/aliexpress', aliexpressRoutes);
  app.use('/api', productRoutes);
  app.use('/api', cartRoutes);
  app.use('/api', wishlistRoutes);
  app.use('/api', addressRoutes);
  app.use('/api', checkoutRoutes);
  app.use('/api', paymentRoutes);
  app.use('/api', orderRoutes);

  // 404 Route Handler
  app.use((req: Request, _res: Response, next: NextFunction) => {
    next(new NotFoundError(`Endpoint ${req.method} ${req.originalUrl} does not exist on WorldFind Backend.`));
  });

  // Global Error Handler
  app.use(errorHandler);

  return app;
};
