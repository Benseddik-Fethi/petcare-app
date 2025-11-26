import jwt, { SignOptions } from "jsonwebtoken";
import { env } from "../../config/env";

export type JwtPayload = { sub: string; role?: string };

export function signAccessToken(userId: string, role?: string): string {
    const payload: JwtPayload = { sub: userId, role };
    const options: SignOptions = {
        expiresIn: env.JWT_EXPIRES_IN as SignOptions["expiresIn"],
        issuer: env.JWT_ISSUER,
        audience: env.JWT_AUDIENCE,
        algorithm: "HS256",
    };
    return jwt.sign(payload, env.JWT_SECRET, options);
}

export function verifyAccessToken(token: string): JwtPayload {
    return jwt.verify(token, env.JWT_SECRET, {
        issuer: env.JWT_ISSUER,
        audience: env.JWT_AUDIENCE,
        algorithms: ["HS256"],
    }) as JwtPayload;
}
