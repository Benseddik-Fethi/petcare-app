import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './index.css'
import { GoogleOAuthProvider } from '@react-oauth/google';
import { BrowserRouter } from 'react-router-dom';
import {AuthProvider} from "@/context/AuthContext.tsx";
import { ThemeProvider } from "@/components/ThemeProvider";

// Si la variable d'env est vide, on met une chaîne bidon pour éviter le crash (White Screen)
const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || "temp_id_pour_dev_local";

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <GoogleOAuthProvider clientId={clientId}>
            <BrowserRouter>
                <AuthProvider>
                    <ThemeProvider defaultTheme="dark" storageKey="vite-ui-theme">
                        <App />
                    </ThemeProvider>
                </AuthProvider>
            </BrowserRouter>
        </GoogleOAuthProvider>
    </React.StrictMode>,
)