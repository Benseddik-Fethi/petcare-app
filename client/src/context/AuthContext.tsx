import { createContext, useContext, useState, useEffect, type ReactNode, useCallback } from "react";

import { useNavigate, useLocation } from "react-router-dom";
import {api, setAccessToken} from "@/lib/api.ts";

// Type User (reprise de ton fichier auth.ts)
export type User = {
    id: string;
    email: string;
    firstName?: string | null;
    lastName?: string | null;
    role: "OWNER" | "PRO";
    createdAt: string;
    updatedAt: string;
};

interface AuthContextType {
    user: User | null;
    isLoading: boolean;
    login: (user: User, token: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();
    const location = useLocation();

    // Fonction stable pour initialiser l'auth (appelée au montage)
    const initAuth = useCallback(async () => {
        try {
            // On récupère le profil. Si 401, l'intercepteur d'api-client tentera le refresh.
            const { data } = await api.get<User>("/auth/me");
            setUser(data);
        } catch {
            setUser(null);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        initAuth();

        // Écoute l'événement de déconnexion forcé (depuis api-client.ts)
        const handleLogoutEvent = () => logout();
        window.addEventListener('auth:logout', handleLogoutEvent);
        return () => window.removeEventListener('auth:logout', handleLogoutEvent);
    }, [initAuth]);

    const login = (newUser: User, token: string) => {
        setAccessToken(token); // Stocke le token pour les requêtes
        setUser(newUser);      // Met à jour l'UI

        // Redirection intelligente
        if (["/login", "/register"].includes(location.pathname)) {
            navigate("/");
        }
    };

    const logout = async () => {
        try {
            await api.post("/auth/logout");
        } catch (e) {
            console.error(e);
        }
        setAccessToken(null);
        setUser(null);
        navigate("/login");
    };

    return (
        <AuthContext.Provider value={{ user, isLoading, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuthContext() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuthContext must be used within AuthProvider");
    }
    return context;
}