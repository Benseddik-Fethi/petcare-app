import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';

// URL de base (vérifie ton port backend, ici 4000)
export const API_URL = 'http://localhost:4000/api';

export const api = axios.create({
    baseURL: API_URL,
    withCredentials: true, // Indispensable pour les cookies (Refresh Token)
    headers: {
        'Content-Type': 'application/json',
    },
});

// --- GESTION DU TOKEN EN MÉMOIRE (Sécurité) ---
let accessToken: string | null = null;

export const setAccessToken = (token: string | null) => {
    accessToken = token;
};

// Intercepteur : Injecte le token dans chaque requête
api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        if (accessToken) {
            config.headers['Authorization'] = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Intercepteur : Gère l'expiration (Refresh Token)
api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        // Si erreur 401 (Non autorisé) et qu'on n'a pas déjà réessayé
        if (error.response?.status === 401 && !originalRequest._retry) {

            // Évite la boucle infinie sur le login lui-même
            if (originalRequest.url?.includes('/auth/login') || originalRequest.url?.includes('/auth/refresh')) {
                return Promise.reject(error);
            }

            originalRequest._retry = true;

            try {
                // On tente de rafraîchir le token via le cookie HttpOnly
                // Ton backend attend POST /auth/refresh et renvoie { token, user }
                const { data } = await api.post('/auth/refresh');

                // On met à jour le token en mémoire
                setAccessToken(data.token);

                // On met à jour le header de la requête originale et on relance
                originalRequest.headers['Authorization'] = `Bearer ${data.token}`;
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