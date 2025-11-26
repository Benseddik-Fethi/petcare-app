import 'dotenv/config';
import { z } from 'zod';

const envSchema = z.object({
    NODE_ENV: z.enum(['development', 'test', 'production']).default('development'),
    PORT: z.string().default('4000'),
    DATABASE_URL: z.string().min(1),

    // Sécurité & URLs
    FRONTEND_URL: z.string().url().default('http://localhost:5173'),

    // JWT Secrets
    JWT_SECRET: z.string().min(32),
    JWT_EXPIRES_IN: z.string().default('15m'),
    JWT_ISSUER: z.string().default('petcare-api'),
    JWT_AUDIENCE: z.string().default('petcare-app'),

    // Refresh Token
    REFRESH_TOKEN_TTL_DAYS: z.coerce.number().int().min(1).default(7),
    CSRF_SECRET: z.string().min(32),
    // Google / Facebook
    GOOGLE_CLIENT_ID: z.string().optional(),
    GOOGLE_CLIENT_SECRET: z.string().optional(),
});

export const env = envSchema.parse(process.env);