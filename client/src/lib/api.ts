import axios, {AxiosError, type InternalAxiosRequestConfig} from 'axios';

const DEFAULT_API_URL = 'http://localhost:4000/api';

const buildApiUrl = (rawUrl?: string) => {
    const fallback = new URL(DEFAULT_API_URL);

    if (!rawUrl) return DEFAULT_API_URL;

    try {
        const parsed = new URL(rawUrl);

        if (!['http:', 'https:'].includes(parsed.protocol)) {
            throw new Error('Protocol must be http or https');
        }

        if (typeof window !== 'undefined') {
            const allowedHosts = new Set([window.location.hostname, 'localhost', '127.0.0.1']);
            if (!allowedHosts.has(parsed.hostname)) {
                console.warn('VITE_API_URL hostname not allowed, falling back to same-origin API');
                return DEFAULT_API_URL;
            }
        }

        const normalizedPath = parsed.pathname.replace(/\/$/, '');
        parsed.pathname = normalizedPath.endsWith('/api') ? normalizedPath : `${normalizedPath}/api`;

        return parsed.toString();
    } catch (error) {
        console.warn('Invalid VITE_API_URL, using default API URL', error);
        return fallback.toString();
    }
};

// URL de base configurée via variable d'environnement avec garde-fous
export const API_URL = buildApiUrl(import.meta.env.VITE_API_URL);
const csrfUrl = new URL('/api/csrf-token', API_URL).toString();

export const api = axios.create({
    baseURL: API_URL,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

// --- GESTION DU TOKEN EN MÉMOIRE (Sécurité) ---
let accessToken: string | null = null;
let csrfToken: string | null = null;

export const setAccessToken = (token: string | null) => {
    accessToken = token;
};

// Récupère le token CSRF depuis le serveur
const fetchCsrfToken = async (): Promise<string> => {
    try {
        const {data} = await axios.get(csrfUrl, {
            withCredentials: true,
        });
        csrfToken = data.csrfToken;
        return data.csrfToken;
    } catch (error) {
        console.error('Failed to fetch CSRF token:', error);
        throw error;
    }
};

// Initialise le token CSRF au démarrage
fetchCsrfToken().catch(console.error);

api.interceptors.request.use(
    async (config: InternalAxiosRequestConfig) => {
        // Injecte le token d'authentification
        if (accessToken) {
            config.headers['Authorization'] = `Bearer ${accessToken}`;
        }

        const method = config.method?.toUpperCase();
        if (method && !['GET', 'HEAD', 'OPTIONS'].includes(method)) {
            if (!csrfToken) {
                try {
                    await fetchCsrfToken();
                } catch (error) {
                    console.error('Failed to fetch CSRF token before request:', error);
                }
            }
            if (csrfToken) {
                config.headers['x-csrf-token'] = csrfToken;
            }
        }

        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean; _csrfRetry?: boolean };

        // Si erreur 403 (CSRF invalide) et qu'on n'a pas déjà réessayé
        if (error.response?.status === 403 && !originalRequest._csrfRetry) {
            originalRequest._csrfRetry = true;

            try {
                // On rafraîchit le token CSRF
                await fetchCsrfToken();

                // On met à jour le header et on relance la requête
                if (csrfToken && originalRequest.headers) {
                    originalRequest.headers['x-csrf-token'] = csrfToken;
                }
                return api(originalRequest);
            } catch (csrfError) {
                console.error('Failed to refresh CSRF token:', csrfError);
                return Promise.reject(error);
            }
        }

        // Si erreur 401 (Non autorisé) et qu'on n'a pas déjà réessayé
        if (error.response?.status === 401 && !originalRequest._retry) {

            // Évite la boucle infinie sur le login lui-même
            if (originalRequest.url?.includes('/auth/login') || originalRequest.url?.includes('/auth/refresh')) {
                return Promise.reject(error);
            }

            originalRequest._retry = true;

            try {
                // On tente de rafraîchir le token via le cookie HttpOnly
                const {data} = await api.post('/auth/refresh');

                // On met à jour le token en mémoire
                setAccessToken(data.accessToken);

                // On met à jour le header de la requête originale et on relance
                originalRequest.headers['Authorization'] = `Bearer ${data.accessToken}`;
                return api(originalRequest);
            } catch (refreshError) {
                // Si le refresh échoue, on déconnecte
                setAccessToken(null);
                window.dispatchEvent(new Event('auth:logout'));
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

// ============================================================================
// EMAIL VERIFICATION API
// ============================================================================

/**
 * Vérifie l'email avec le token reçu par email
 */
export async function verifyEmail(token: string): Promise<{ success: boolean; message: string }> {
    const response = await api.post(`/v1/users/verify-email?token=${token}`);
    return response.data;
}

/**
 * Renvoie l'email de vérification
 */
export async function resendVerificationEmail(email: string): Promise<{ message: string }> {
    const response = await api.post('/v1/users/resend-verification', { email });
    return response.data;
}

/**
 * Envoie un email de vérification à l'utilisateur authentifié
 */
export async function sendVerificationEmail(): Promise<{ message: string }> {
    const response = await api.post('/v1/users/send-verification');
    return response.data;
}