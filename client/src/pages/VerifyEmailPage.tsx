import { useEffect, useState } from "react";
import { CheckCircle, XCircle, Loader2, PawPrint } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import { verifyEmail } from "@/lib/api";
import { isAxiosError } from "axios";

export default function VerifyEmailPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const token = searchParams.get('token');

    const [verifying, setVerifying] = useState(true);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!token) {
            setError("Token de vérification manquant");
            setVerifying(false);
            return;
        }

        const verify = async () => {
            try {
                setVerifying(true);
                setError(null);

                const result = await verifyEmail(token);

                if (result.success) {
                    setSuccess(true);
                    // Rediriger vers le dashboard après 3 secondes
                    setTimeout(() => {
                        navigate('/dashboard');
                    }, 3000);
                } else {
                    setError(result.message || "La vérification a échoué");
                }
            } catch (err) {
                if (isAxiosError(err)) {
                    setError(err.response?.data?.message || "Token invalide ou expiré");
                } else {
                    setError("Une erreur est survenue");
                }
            } finally {
                setVerifying(false);
            }
        };

        verify();
    }, [token, navigate]);

    return (
        <div className="min-h-screen bg-gradient-to-br from-rose-50 via-pink-50 to-amber-50 flex items-center justify-center p-6 relative overflow-hidden dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
            <Card className="w-full max-w-md relative z-10 border-white/50 bg-white/80 dark:bg-slate-900/80 backdrop-blur-sm shadow-xl dark:border-slate-800">
                <CardHeader className="text-center pt-10">
                    {verifying && (
                        <div className="mx-auto w-20 h-20 bg-gradient-to-br from-blue-400 to-blue-500 rounded-3xl mb-4 flex items-center justify-center shadow-lg shadow-blue-200 dark:shadow-none">
                            <Loader2 size={40} className="text-white animate-spin" />
                        </div>
                    )}

                    {success && (
                        <div className="mx-auto w-20 h-20 bg-gradient-to-br from-emerald-400 to-emerald-500 rounded-3xl mb-4 flex items-center justify-center shadow-lg shadow-emerald-200 dark:shadow-none">
                            <CheckCircle size={40} className="text-white" />
                        </div>
                    )}

                    {error && (
                        <div className="mx-auto w-20 h-20 bg-gradient-to-br from-red-400 to-red-500 rounded-3xl mb-4 flex items-center justify-center shadow-lg shadow-red-200 dark:shadow-none">
                            <XCircle size={40} className="text-white" />
                        </div>
                    )}

                    <CardTitle className="text-3xl font-bold text-rose-500 mb-1">
                        {verifying && "Vérification en cours..."}
                        {success && "Email vérifié !"}
                        {error && "Vérification échouée"}
                    </CardTitle>

                    <CardDescription className="text-gray-500 dark:text-gray-400 font-medium">
                        {verifying && "Veuillez patienter quelques instants"}
                        {success && "Votre compte est maintenant actif"}
                        {error && "Impossible de vérifier votre email"}
                    </CardDescription>
                </CardHeader>

                <CardContent className="px-8 pb-8 space-y-6">
                    {/* Verifying State */}
                    {verifying && (
                        <div className="text-center py-8">
                            <div className="flex flex-col items-center gap-4">
                                <div className="flex gap-2">
                                    <div className="w-2 h-2 rounded-full bg-rose-500 animate-bounce" style={{ animationDelay: '0ms' }} />
                                    <div className="w-2 h-2 rounded-full bg-rose-500 animate-bounce" style={{ animationDelay: '150ms' }} />
                                    <div className="w-2 h-2 rounded-full bg-rose-500 animate-bounce" style={{ animationDelay: '300ms' }} />
                                </div>
                                <p className="text-sm text-gray-600 dark:text-gray-400">
                                    Vérification de votre email en cours...
                                </p>
                            </div>
                        </div>
                    )}

                    {/* Success State */}
                    {success && (
                        <div className="space-y-6">
                            <div className="bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 rounded-lg p-4">
                                <div className="flex items-start gap-3">
                                    <CheckCircle className="w-5 h-5 text-emerald-600 dark:text-emerald-400 flex-shrink-0 mt-0.5" />
                                    <div className="flex-1">
                                        <p className="text-sm font-medium text-emerald-900 dark:text-emerald-100">
                                            Vérification réussie !
                                        </p>
                                        <p className="text-sm text-emerald-700 dark:text-emerald-300 mt-1">
                                            Votre adresse email a été vérifiée avec succès. Votre compte est maintenant actif et vous allez être redirigé vers le tableau de bord.
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="text-center space-y-3">
                                <p className="text-sm text-gray-600 dark:text-gray-400">
                                    Redirection automatique dans 3 secondes...
                                </p>
                                <Button
                                    onClick={() => navigate('/dashboard')}
                                    className="w-full bg-gradient-to-r from-rose-500 to-pink-500 hover:from-rose-600 hover:to-pink-600"
                                >
                                    Accéder au tableau de bord
                                </Button>
                            </div>
                        </div>
                    )}

                    {/* Error State */}
                    {error && (
                        <div className="space-y-6">
                            <div className="bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800 rounded-lg p-4">
                                <div className="flex items-start gap-3">
                                    <XCircle className="w-5 h-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
                                    <div className="flex-1">
                                        <p className="text-sm font-medium text-red-900 dark:text-red-100">
                                            Erreur de vérification
                                        </p>
                                        <p className="text-sm text-red-700 dark:text-red-300 mt-1">
                                            {error}
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="space-y-3">
                                <p className="text-sm text-gray-600 dark:text-gray-400 text-center">
                                    Raisons possibles :
                                </p>
                                <ul className="space-y-2 text-sm text-gray-600 dark:text-gray-400">
                                    <li className="flex gap-2">
                                        <span className="text-red-500">•</span>
                                        <span>Le lien a expiré (24 heures maximum)</span>
                                    </li>
                                    <li className="flex gap-2">
                                        <span className="text-red-500">•</span>
                                        <span>Le lien a déjà été utilisé</span>
                                    </li>
                                    <li className="flex gap-2">
                                        <span className="text-red-500">•</span>
                                        <span>Le lien est invalide</span>
                                    </li>
                                </ul>
                            </div>

                            <div className="space-y-3">
                                <Link to="/auth/resend-verification">
                                    <Button
                                        variant="default"
                                        className="w-full bg-gradient-to-r from-rose-500 to-pink-500 hover:from-rose-600 hover:to-pink-600"
                                    >
                                        Renvoyer un email de vérification
                                    </Button>
                                </Link>

                                <Link to="/login">
                                    <Button variant="outline" className="w-full">
                                        Retour à la connexion
                                    </Button>
                                </Link>
                            </div>
                        </div>
                    )}

                    {/* Help Link */}
                    <div className="text-center pt-4 border-t border-gray-200 dark:border-gray-700">
                        <Link
                            to="/login"
                            className="text-sm text-rose-600 dark:text-rose-400 hover:text-rose-700 dark:hover:text-rose-300 font-medium inline-flex items-center gap-1"
                        >
                            <PawPrint className="w-4 h-4" />
                            Retour à la connexion
                        </Link>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
