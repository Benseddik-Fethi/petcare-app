import rateLimit from 'express-rate-limit';

export const apiLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 min
    max: 300, // 300 requêtes globales
    standardHeaders: true,
    legacyHeaders: false,
});

export const authLimiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 20, // 20 tentatives login/register
    standardHeaders: true,
    legacyHeaders: false,
    message: "Trop de tentatives, réessayez plus tard."
});
