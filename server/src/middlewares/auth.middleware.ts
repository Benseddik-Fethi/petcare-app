import { Request, Response, NextFunction } from "express";
import { verifyAccessToken } from "../utils/security/jwt";
import { UnauthorizedError } from "../utils/AppError";

// Extension du type Request
declare global {
    namespace Express {
        interface Request {
            userId?: string;
            userRole?: string;
        }
    }
}

export function authenticate(req: Request, res: Response, next: NextFunction) {
    const authHeader = req.headers.authorization;

    if (!authHeader?.startsWith("Bearer ")) {
        throw new UnauthorizedError("Token manquant");
    }

    const token = authHeader.substring(7);

    try {
        const payload = verifyAccessToken(token);
        req.userId = payload.sub;
        req.userRole = payload.role;
        next();
    } catch {
        throw new UnauthorizedError("Token invalide ou expiré");
    }
}