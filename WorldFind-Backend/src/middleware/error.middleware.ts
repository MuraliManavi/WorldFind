import { Request, Response, NextFunction } from 'express';
import { AppError } from '../utils/errors';
import { logger } from '../utils/logger';
import { env } from '../config/env';
import crypto from 'crypto';

export interface ExtendedRequest extends Request {
  id?: string;
  rawBody?: Buffer;
}

export function errorHandler(
  err: Error,
  req: ExtendedRequest,
  res: Response,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  _next: NextFunction
): void {
  const requestId = req.id || crypto.randomUUID();

  let statusCode = 500;
  let errorCode = 'INTERNAL_SERVER_ERROR';
  let message = 'An unexpected error occurred on WorldFind Backend.';
  let details: unknown = undefined;

  if (err instanceof AppError) {
    statusCode = err.statusCode;
    errorCode = err.errorCode;
    message = err.message;
    if ('details' in err) {
      details = (err as unknown as { details: unknown }).details;
    }
  } else if (err.name === 'SyntaxError') {
    statusCode = 400;
    errorCode = 'BAD_REQUEST';
    message = 'Invalid JSON request body.';
  }

  // Log error
  logger.error(`[RequestId: ${requestId}] Path: ${req.method} ${req.originalUrl} - Error: ${err.message}`, {
    statusCode,
    errorCode,
    stack: env.NODE_ENV === 'development' ? err.stack : undefined,
  });

  res.status(statusCode).json({
    success: false,
    error: {
      code: errorCode,
      message: message,
      requestId: requestId,
      ...(details ? { details } : {}),
      ...(env.NODE_ENV === 'development' && !(err instanceof AppError) ? { stack: err.stack } : {}),
    },
  });
}
