import { Request, Response, NextFunction } from "express";
import { AuthService } from "../services/auth.service";
import { setRefreshTokenCookie, clearRefreshTokenCookie, REFRESH_TOKEN_COOKIE_NAME } from "../utils/security/cookies";
import { z } from "zod";

const authService = new AuthService();

const registerSchema = z.object({
    email: z.string().email(),
    password: z.string().min(8),
    firstName: z.string(),
    lastName: z.string(),
});

export class AuthController {
    static async register(req: Request, res: Response, next: NextFunction) {
        try {
            const data = registerSchema.parse(req.body);
            const result = await authService.register(data, req.ip || "", req.headers["user-agent"] || "");
            setRefreshTokenCookie(res, result.refreshToken);
            res.status(201).json({ accessToken: result.accessToken, user: result.user });
        } catch (e) { next(e); }
    }

    static async login(req: Request, res: Response, next: NextFunction) {
        try {
            const { email, password } = req.body;
            const result = await authService.login(email, password, req.ip || "", req.headers["user-agent"] || "");
            setRefreshTokenCookie(res, result.refreshToken);
            res.json(result);
        } catch (e) { next(e); }
    }

    // ✅ CORRECTION : On renomme 'google' en 'social' pour matcher le front
    // Et on gère le paramètre 'provider'
    static async social(req: Request, res: Response, next: NextFunction) {
        try {
            const { token, provider } = req.body;

            if (provider === 'GOOGLE') {
                const result = await authService.googleLogin(token, req.ip || "", req.headers["user-agent"] || "");
                setRefreshTokenCookie(res, result.refreshToken);
                res.json({ accessToken: result.accessToken });
            } else {
                res.status(400).json({ message: "Provider non supporté" });
            }
        } catch (e) { next(e); }
    }

    static async refresh(req: Request, res: Response, next: NextFunction) {
        try {
            const token = req.cookies[REFRESH_TOKEN_COOKIE_NAME];
            if (!token) throw new Error("No token");

            const result = await authService.refresh(token);
            setRefreshTokenCookie(res, result.refreshToken);
            res.json({ accessToken: result.accessToken, user: result.user });
        } catch (e) {
            clearRefreshTokenCookie(res);
            res.status(401).json({ message: "Session expirée" });
        }
    }

    static async logout(req: Request, res: Response, next: NextFunction) {
        try {
            const token = req.cookies[REFRESH_TOKEN_COOKIE_NAME];
            if (token) await authService.logout(token);
            clearRefreshTokenCookie(res);
            res.sendStatus(204);
        } catch (e) { next(e); }
    }

    static async me(req: Request, res: Response, next: NextFunction) {
        try {
            const user = await authService.getMe(req.userId!);
            res.json({ user });
        } catch (e) { next(e); }
    }
}