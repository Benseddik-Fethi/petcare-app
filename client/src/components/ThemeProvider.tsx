import {createContext, useContext, useEffect, useMemo, useState} from "react";

type Theme = "dark" | "light" | "system";

type ThemeProviderProps = {
    children: React.ReactNode;
    defaultTheme?: Theme;
    storageKey?: string;
};

type ThemeProviderState = {
    theme: Theme;
    setTheme: (theme: Theme) => void;
};

const initialState: ThemeProviderState = {
    theme: "system",
    setTheme: () => null,
};

const ThemeProviderContext = createContext<ThemeProviderState>(initialState);

export function ThemeProvider({
                                  children,
                                  defaultTheme = "system",
                                  storageKey = "vite-ui-theme",
                                  ...props
                              }: Readonly<ThemeProviderProps>) {
    console.log("📢 [ThemeProvider] Le composant Provider est monté !");
    // On nomme le setter "setThemeState" pour ne pas le confondre avec la fonction "setTheme" du contexte
    const [theme, setThemeState] = useState<Theme>(
        () => (localStorage.getItem(storageKey) as Theme) || defaultTheme
    );

    useEffect(() => {
        const root = window.document.documentElement;

        root.classList.remove("light", "dark");

        if (theme === "system") {
            const systemTheme = window.matchMedia("(prefers-color-scheme: dark)")
                .matches
                ? "dark"
                : "light";

            root.classList.add(systemTheme);
            return;
        }

        root.classList.add(theme);
    }, [theme]);

    const value = useMemo(
        () => ({
            theme,
            setTheme: (newTheme: Theme) => {
                console.log(`[ThemeProvider] 1. Reçu demande de changement vers : ${newTheme}`);

                localStorage.setItem(storageKey, newTheme);
                console.log(`[ThemeProvider] 2. Sauvegardé dans localStorage (${storageKey})`);

                setThemeState(newTheme);
                console.log(`[ThemeProvider] 3. État React mis à jour`);
            },
        }),
        [theme, storageKey]
    );

    return (
        <ThemeProviderContext.Provider {...props} value={value}>
            {children}
        </ThemeProviderContext.Provider>
    );
}

export const useTheme = () => {
    const context = useContext(ThemeProviderContext);

    // Maintenant, cette vérification servira vraiment à quelque chose
    if (context === undefined)
        throw new Error("⛔ ERREUR FATALE : useTheme est utilisé HORS du ThemeProvider ! Vérifie main.tsx");

    return context;
};