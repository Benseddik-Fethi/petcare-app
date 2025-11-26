import { useAuth } from "@/context/AuthContext";
import { Navigate, Outlet } from "react-router-dom";

export const ProtectedRoute = () => {
    const { user, isLoading } = useAuth();

    if (isLoading) return <div>Chargement...</div>;

    return user ? <Outlet /> : <Navigate to="/login" replace />;
};