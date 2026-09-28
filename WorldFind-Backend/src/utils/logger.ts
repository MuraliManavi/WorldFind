import winston from 'winston';

const SENSITIVE_KEYS = new Set([
  'app_secret',
  'appsecret',
  'access_token',
  'accesstoken',
  'refresh_token',
  'refreshtoken',
  'private_key',
  'privatekey',
  'password',
  'secret',
  'token',
  'authorization',
]);

/**
 * Recursively sanitize objects to remove or mask sensitive fields
 */
export function sanitizeLogData(data: unknown): unknown {
  if (data === null || data === undefined) {
    return data;
  }

  if (typeof data === 'string') {
    if (data.startsWith('-----BEGIN PRIVATE KEY-----')) {
      return '[REDACTED_PRIVATE_KEY]';
    }
    return data;
  }

  if (Array.isArray(data)) {
    return data.map(sanitizeLogData);
  }

  if (typeof data === 'object') {
    const sanitized: Record<string, unknown> = {};
    for (const [key, value] of Object.entries(data as Record<string, unknown>)) {
      const lowerKey = key.toLowerCase();
      if (SENSITIVE_KEYS.has(lowerKey) || lowerKey.includes('secret') || lowerKey.includes('token') || lowerKey.includes('key')) {
        sanitized[key] = '[REDACTED]';
      } else {
        sanitized[key] = sanitizeLogData(value);
      }
    }
    return sanitized;
  }

  return data;
}

const customFormat = winston.format.printf(({ level, message, timestamp, ...meta }) => {
  const sanitizedMeta = sanitizeLogData(meta);
  const metaString = Object.keys(sanitizedMeta as object).length
    ? ` ${JSON.stringify(sanitizedMeta)}`
    : '';
  return `[${timestamp}] [${level.toUpperCase()}]: ${message}${metaString}`;
});

export const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss.SSS' }),
    winston.format.errors({ stack: true }),
    customFormat
  ),
  transports: [
    new winston.transports.Console({
      handleExceptions: true,
    }),
    new winston.transports.File({
      filename: 'requests.log',
      handleExceptions: true,
    }),
  ],
});
