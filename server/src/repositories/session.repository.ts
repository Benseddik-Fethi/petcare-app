import { prisma } from "../config/prisma";
import crypto from "crypto";
import { env } from "../config/env";

export class SessionRepository {
    private hashToken(token: string) {
        return crypto.createHash("sha256").update(token).digest("hex");
    }

    async create(userId: string, userAgent?: string, ip?: string) {
        const token = crypto.randomBytes(64).toString("hex");
        const hash = this.hashToken(token);
        const expiresAt = new Date(Date.now() + env.REFRESH_TOKEN_TTL_DAYS * 24 * 60 * 60 * 1000);

        await prisma.session.create({
            data: { userId, refreshTokenHash: hash, userAgent, ipAddress: ip, expiresAt }
        });

        return token;
    }

    async findByToken(token: string) {
        const hash = this.hashToken(token);
        return prisma.session.findFirst({
            where: {
                refreshTokenHash: hash,
                revokedAt: null,
                expiresAt: { gt: new Date() }
            },
            include: { user: true }
        });
    }

    async revoke(sessionId: string) {
        return prisma.session.update({
            where: { id: sessionId },
            data: { revokedAt: new Date() }
        });
    }

    async rotate(sessionId: string) {
        const newToken = crypto.randomBytes(64).toString("hex");
        const newHash = this.hashToken(newToken);
        const newExpiry = new Date(Date.now() + env.REFRESH_TOKEN_TTL_DAYS * 24 * 60 * 60 * 1000);

        await prisma.session.update({
            where: { id: sessionId },
            data: { refreshTokenHash: newHash, expiresAt: newExpiry }
        });

        return newToken;
    }
}