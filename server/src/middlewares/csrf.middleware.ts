import { doubleCsrf } from "csrf-csrf";
import { env } from "../config/env";

const isProduction = env.NODE_ENV === "production";

export const {
    generateToken, // Fonction pour générer le token à envoyer au front
    doubleCsrfProtection, // Le middleware à placer avant les routes protégées
    invalidCsrfTokenError, // Erreur type
} = doubleCsrf({
    getSecret: () => {
        if (!env.CSRF_SECRET) {
            throw new Error("CSRF_SECRET must be set in environment variables");
        }
        return env.CSRF_SECRET;
    },
    cookieName: isProduction ? "__Host-psifi.x-csrf-token" : "x-csrf-token",
    cookieOptions: {
        httpOnly: true,
        secure: isProduction,
        sameSite: isProduction ? "strict" : "lax",
        path: "/",
    },
    size: 64,
    ignoredMethods: ["GET", "HEAD", "OPTIONS"],
    getTokenFromRequest: (req) => req.headers["x-csrf-token"], // Où le front doit l'envoyer
});