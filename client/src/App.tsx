import {Navigate, Route, Routes} from 'react-router-dom';
import RegisterPage from './pages/RegisterPage';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import {ProtectedRoute} from './components/ProtectedRoute';
import DashboardLayout from './layouts/DashboardLayout';
import CalendarPage from "@/pages/CalendarPage.tsx";
import PetProfilePage from "@/pages/PetProfilePage.tsx";
import SettingsPage from "@/pages/SettingsPage.tsx";
import PetsPage from "@/pages/PetsPage.tsx";
import EmailSentPage from "@/pages/EmailSentPage.tsx";
import VerifyEmailPage from "@/pages/VerifyEmailPage.tsx";
import ResendVerificationPage from "@/pages/ResendVerificationPage.tsx";
import {useAuth} from "@/context/AuthContext.tsx";

function RootRedirect() {
    const {user, isLoading} = useAuth();

    if (isLoading) return <div>Chargement...</div>;

    return <Navigate to={user ? "/dashboard" : "/login"} replace/>;
}

function App() {
    return (
        <Routes>
            <Route path="/" element={<RootRedirect/>}/>
            <Route path="/register" element={<RegisterPage/>}/>
            <Route path="/login" element={<LoginPage/>}/>

            {/* Routes de vérification d'email */}
            <Route path="/auth/verify-email-sent" element={<EmailSentPage/>}/>
            <Route path="/auth/verify-email" element={<VerifyEmailPage/>}/>
            <Route path="/auth/resend-verification" element={<ResendVerificationPage/>}/>

            {/* Routes Protégées + Layout */}
            <Route element={<ProtectedRoute/>}>
                <Route element={<DashboardLayout/>}>
                    <Route path="/dashboard" element={<DashboardPage/>}/>
                    <Route path="/pets" element={<PetsPage/>}/>
                    <Route path="/calendar" element={<CalendarPage/>}/>
                    <Route path="/pets/:id" element={<PetProfilePage/>}/>
                    <Route path="/settings" element={<SettingsPage/>}/>
                </Route>
            </Route>
        </Routes>
    );
}

export default App;