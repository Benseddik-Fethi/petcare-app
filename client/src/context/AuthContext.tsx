// src/context/AuthContext.tsx
import {
    createContext,
    type ReactNode,
    useCallback,
    useContext,
    useEffect,
    useState,
} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import {api, setAccessToken} from "@/lib/api";

export type User = {
    id: string;
    email: string;
    firstName?: string | null;
    lastName?: string | null;
    role: "OWNER" | "PRO" | "ADMIN" | "VET";
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

export function AuthProvider({children}: { children: ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();
    const location = useLocation();

    const logout = useCallback(async () => {
        try {
            await api.post("/auth/logout");
        } catch (e) {
            console.error(e);
        }
        setAccessToken(null);
        setUser(null);
        navigate("/login");
    }, [navigate]);

    const initAuth = useCallback(async () => {
        try {
            // On tente de récupérer une session via le refresh_token en cookie
            const {data} = await api.post<{ accessToken: string; user: User }>("/auth/refresh");
            setAccessToken(data.accessToken);
            setUser(data.user);
        } catch {
            // Pas de session valide
            setAccessToken(null);
            setUser(null);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        initAuth();

        // Déconnexion forcée déclenchée par l'intercepteur axios
        const handleLogoutEvent = () => logout();
        window.addEventListener("auth:logout", handleLogoutEvent);
        return () => window.removeEventListener("auth:logout", handleLogoutEvent);
    }, [initAuth, logout]);

    const login = (newUser: User, token: string) => {
        setAccessToken(token);
        setUser(newUser);

        // Redirige vers le dashboard si on vient de /login ou /register
        if (["/login", "/register"].includes(location.pathname)) {
            navigate("/dashboard");
        }
    };

    return (
        <AuthContext.Provider value={{user, isLoading, login, logout}}>
            {children}
        </AuthContext.Provider>
    );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used within AuthProvider");
    return ctx;
}
