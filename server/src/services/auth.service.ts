import { UserRepository } from "../repositories/user.repository";
import { SessionRepository } from "../repositories/session.repository";
import { hashPassword, verifyPassword } from "../utils/security/password";
import { signAccessToken } from "../utils/security/jwt";
import { UnauthorizedError, ConflictError, ForbiddenError } from "../utils/AppError";
import { verifyGoogleToken } from "../utils/socialProvider"; // Ton fichier existant

const userRepo = new UserRepository();
const sessionRepo = new SessionRepository();

export class AuthService {

    async register(
        data: { email: string; password: string; firstName: string; lastName: string },
        ip: string,
        ua: string
    ) {
        const existing = await userRepo.findByEmail(data.email);
        if (existing) throw new ConflictError("Cet email est déjà utilisé");

        const passwordHash = await hashPassword(data.password);
        const user = await userRepo.create({ ...data, password: passwordHash });

        const refreshToken = await sessionRepo.create(user.id, ua, ip);
        const accessToken = signAccessToken(user.id, user.role);

        return { user, accessToken, refreshToken };
    }

    async login(email: string, pass: string, ip: string, ua: string) {
        const user = await userRepo.findByEmail(email);

        // 1. Check User
        if (!user?.passwordHash) throw new UnauthorizedError("Identifiants invalides");

        // 2. Check Lockout
        if (user.lockedUntil && user.lockedUntil > new Date()) {
            throw new ForbiddenError("Compte verrouillé temporairement. Réessayez plus tard.");
        }

        // 3. Check Password
        const valid = await verifyPassword(user.passwordHash, pass);
        if (!valid) {
            // Incrémenter échecs
            const attempts = user.failedLoginAttempts + 1;
            const shouldLock = attempts >= 5;
            await userRepo.updateLockout(user.id, {
                failedLoginAttempts: attempts,
                lastFailedLogin: new Date(),
                lockedUntil: shouldLock ? new Date(Date.now() + 15 * 60 * 1000) : null
            });
            throw new UnauthorizedError("Identifiants invalides");
        }

        // 4. Reset Lockout & Créer Session
        await userRepo.updateLockout(user.id, { failedLoginAttempts: 0, lockedUntil: null });

        const refreshToken = await sessionRepo.create(user.id, ua, ip);
        const accessToken = signAccessToken(user.id, user.role);

        const { passwordHash, ...safeUser } = user;
        return { user: safeUser, accessToken, refreshToken };
    }

    async googleLogin(token: string, ip: string, ua: string) {
        const payload = await verifyGoogleToken(token);
        let user = await userRepo.findByEmail(payload.email);

        user ??= await userRepo.create({
            email: payload.email,
            firstName: payload.firstName,
            lastName: payload.lastName,
            avatar: payload.avatar,
            provider: "GOOGLE",
            googleId: payload.id
        });

        const refreshToken = await sessionRepo.create(user.id, ua, ip);
        const accessToken = signAccessToken(user.id, user.role);

        const { passwordHash, ...safeUser } = user;
        return { user: safeUser, accessToken, refreshToken };
    }

    async refresh(token: string) {
        const session = await sessionRepo.findByToken(token);
        if (!session?.user) throw new UnauthorizedError("Session invalide");

        const newRefreshToken = await sessionRepo.rotate(session.id);
        const accessToken = signAccessToken(session.userId, session.user.role);

        return { accessToken, refreshToken: newRefreshToken, user: session.user };
    }

    async logout(token: string) {
        const session = await sessionRepo.findByToken(token);
        if (session) await sessionRepo.revoke(session.id);
    }

    async getMe(userId: string) {
        return userRepo.findById(userId);
    }
}
