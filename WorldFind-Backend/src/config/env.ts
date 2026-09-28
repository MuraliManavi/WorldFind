import dotenv from 'dotenv';
import path from 'path';
import { z } from 'zod';

// Load .env file
dotenv.config({ path: path.resolve(process.cwd(), '.env') });

const envSchema = z.object({
  PORT: z
    .string()
    .default('8080')
    .transform((val) => parseInt(val, 10)),
  NODE_ENV: z.enum(['development', 'test', 'production']).default('development'),
  WORLD_FIND_API_BASE_URL: z.string().default('https://worldfind.onrender.com'),
  CORS_ORIGIN: z.string().optional(),

  // Firebase Credentials
  FIREBASE_PROJECT_ID: z.string().optional(),
  FIREBASE_CLIENT_EMAIL: z.string().optional(),
  FIREBASE_PRIVATE_KEY: z.string().optional(),

  // AliExpress Credentials
  ALIEXPRESS_APP_KEY: z.string().default(''),
  ALIEXPRESS_APP_SECRET: z.string().default(''),
  ALIEXPRESS_API_BASE_URL: z.string().default('https://api-sg.aliexpress.com/sync'),
  ALIEXPRESS_OAUTH_URL: z.string().default('https://oauth.aliexpress.com/authorize'),
  ALIEXPRESS_CALLBACK_URL: z.string().default('https://worldfind.onrender.com/api/aliexpress/callback'),

  // Optional app_signature if required
  ALIEXPRESS_APP_SIGNATURE: z.string().optional(),

  // Static Tokens for Local Development Fallback
  ALIEXPRESS_ACCESS_TOKEN: z.string().optional(),
  ALIEXPRESS_REFRESH_TOKEN: z.string().optional(),

  // Token Encryption Key
  TOKEN_ENCRYPTION_KEY: z.string().optional(),

  // Admin API Key for protected admin routes
  ADMIN_API_KEY: z.string().optional(),

  // Razorpay Credentials
  RAZORPAY_KEY_ID: z.string().optional(),
  RAZORPAY_KEY_SECRET: z.string().optional(),

  // WorldFind Defaults
  DEFAULT_SHIP_TO_COUNTRY: z.string().default('IN'),
  DEFAULT_TARGET_CURRENCY: z.string().default('INR'),
  DEFAULT_TARGET_LANGUAGE: z.string().default('EN'),
  DEFAULT_TRACKING_ID: z.string().default('worldfind_default'),
});

const parsedEnv = envSchema.safeParse(process.env);

if (!parsedEnv.success) {
  console.error('Invalid environment variables:', parsedEnv.error.format());
  throw new Error('Invalid environment variables configuration.');
}

const rawEnv = parsedEnv.data;

const envData = {
  ...rawEnv,
  CORS_ORIGIN: rawEnv.CORS_ORIGIN || (rawEnv.NODE_ENV === 'production' ? rawEnv.WORLD_FIND_API_BASE_URL : '*'),
};

// Production strict validation
if (envData.NODE_ENV === 'production') {
  const missingProdVars: string[] = [];

  if (!envData.FIREBASE_PROJECT_ID) missingProdVars.push('FIREBASE_PROJECT_ID');
  if (!envData.FIREBASE_CLIENT_EMAIL) missingProdVars.push('FIREBASE_CLIENT_EMAIL');
  if (!envData.FIREBASE_PRIVATE_KEY) missingProdVars.push('FIREBASE_PRIVATE_KEY');
  if (!envData.ALIEXPRESS_APP_KEY) missingProdVars.push('ALIEXPRESS_APP_KEY');
  if (!envData.ALIEXPRESS_APP_SECRET) missingProdVars.push('ALIEXPRESS_APP_SECRET');
  if (!envData.TOKEN_ENCRYPTION_KEY) missingProdVars.push('TOKEN_ENCRYPTION_KEY');
  if (!envData.ADMIN_API_KEY) missingProdVars.push('ADMIN_API_KEY');

  if (missingProdVars.length > 0) {
    throw new Error(`[FATAL] Missing required production environment variables: ${missingProdVars.join(', ')}`);
  }

  if (envData.CORS_ORIGIN === '*') {
    throw new Error('[FATAL] CORS_ORIGIN cannot be "*" in production with credentials enabled. Specify an explicit domain.');
  }

  const invalidPlaceholderKeys = [
    'GENERATE_A_RANDOM_64_HEX_CHARACTER_SECRET',
    'worldfind_secret_encryption_key_32',
    'your_encryption_key_here',
  ];
  if (invalidPlaceholderKeys.includes(envData.TOKEN_ENCRYPTION_KEY || '')) {
    throw new Error('[FATAL] TOKEN_ENCRYPTION_KEY cannot use a known default placeholder in production.');
  }
}

export const env = envData;
