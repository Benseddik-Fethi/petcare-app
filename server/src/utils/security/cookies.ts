import type {Response} from "express";
import {env} from "../../config/env";

export const REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
const isProduction = env.NODE_ENV === "production";
const maxAge = env.REFRESH_TOKEN_TTL_DAYS * 24 * 60 * 60 * 1000;

export function setRefreshTokenCookie(res: Response, value: string) {
    res.cookie(REFRESH_TOKEN_COOKIE_NAME, value, {
        httpOnly: true,
        secure: isProduction,
        sameSite: isProduction ? "strict" : "lax",
        path: "/api/auth", // Restreint au endpoint auth
        maxAge,
    });
}

export function clearRefreshTokenCookie(res: Response) {
    res.clearCookie(REFRESH_TOKEN_COOKIE_NAME, {
        httpOnly: true,
        secure: isProduction,
        sameSite: isProduction ? "strict" : "lax",
        path: "/api/auth",
    });
}
