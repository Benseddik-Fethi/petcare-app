import { useState } from "react";
import { Mail, PawPrint, CheckCircle, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useSearchParams, Link } from "react-router-dom";
import { resendVerificationEmail } from "@/lib/api";
import { isAxiosError } from "axios";

export default function EmailSentPage() {
    const [searchParams] = useSearchParams();
    const email = searchParams.get('email') || 'votre adresse email';
    const [resending, setResending] = useState(false);
    const [resendSuccess, setResendSuccess] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleResend = async () => {
        try {
            setResending(true);
            setError(null);
            setResendSuccess(false);

            await resendVerificationEmail(email);

            setResendSuccess(true);
        } catch (err) {
            if (isAxiosError(err)) {
                setError(err.response?.data?.message || "Erreur lors de l'envoi de l'email");
            } else {
                setError("Une erreur est survenue");
            }
        } finally {
            setResending(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-rose-50 via-pink-50 to-amber-50 flex items-center justify-center p-6 relative overflow-hidden dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
            <Card className="w-full max-w-md relative z-10 border-white/50 bg-white/80 dark:bg-slate-900/80 backdrop-blur-sm shadow-xl dark:border-slate-800">
                <CardHeader className="text-center pt-10">
                    <div className="mx-auto w-20 h-20 bg-gradient-to-br from-rose-400 to-pink-500 rounded-3xl mb-4 flex items-center justify-center shadow-lg shadow-rose-200 dark:shadow-none">
                        <Mail size={40} className="text-white" />
                    </div>
                    <CardTitle className="text-3xl font-bold text-rose-500 mb-1">
                        Vérifiez votre email
                    </CardTitle>
                    <CardDescription className="text-gray-500 dark:text-gray-400 font-medium">
                        Un email de vérification vous a été envoyé
                    </CardDescription>
                </CardHeader>

                <CardContent className="px-8 pb-8 space-y-6">
                    {/* Success Message */}
                    <div className="bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 rounded-lg p-4">
                        <div className="flex items-start gap-3">
                            <CheckCircle className="w-5 h-5 text-emerald-600 dark:text-emerald-400 flex-shrink-0 mt-0.5" />
                            <div className="flex-1">
                                <p className="text-sm font-medium text-emerald-900 dark:text-emerald-100">
                                    Email envoyé avec succès !
                                </p>
                                <p className="text-sm text-emerald-700 dark:text-emerald-300 mt-1">
                                    Nous avons envoyé un lien de vérification à :
                                </p>
                                <p className="text-sm font-semibold text-emerald-900 dark:text-emerald-100 mt-1">
                                    {email}
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Instructions */}
                    <div className="space-y-3">
                        <h3 className="font-semibold text-gray-900 dark:text-gray-100">
                            Prochaines étapes :
                        </h3>
                        <ol className="space-y-2 text-sm text-gray-600 dark:text-gray-400">
                            <li className="flex gap-2">
                                <span className="font-semibold text-rose-500">1.</span>
                                <span>Ouvrez votre boîte de réception</span>
                            </li>
                            <li className="flex gap-2">
                                <span className="font-semibold text-rose-500">2.</span>
                                <span>Cliquez sur le lien de vérification</span>
                            </li>
                            <li className="flex gap-2">
                                <span className="font-semibold text-rose-500">3.</span>
                                <span>Votre compte sera activé automatiquement</span>
                            </li>
                        </ol>
                    </div>

                    {/* Warning */}
                    <div className="bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-800 rounded-lg p-4">
                        <div className="flex items-start gap-3">
                            <AlertCircle className="w-5 h-5 text-amber-600 dark:text-amber-400 flex-shrink-0 mt-0.5" />
                            <div className="flex-1">
                                <p className="text-sm font-medium text-amber-900 dark:text-amber-100">
                                    Important
                                </p>
                                <p className="text-sm text-amber-700 dark:text-amber-300 mt-1">
                                    Le lien de vérification expire dans <strong>24 heures</strong>.
                                    Vérifiez aussi vos spams si vous ne trouvez pas l'email.
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Resend Success Message */}
                    {resendSuccess && (
                        <div className="bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 rounded-lg p-4">
                            <div className="flex items-center gap-2">
                                <CheckCircle className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                                <p className="text-sm text-emerald-700 dark:text-emerald-300">
                                    Email renvoyé avec succès !
                                </p>
                            </div>
                        </div>
                    )}

                    {/* Resend Error Message */}
                    {error && (
                        <div className="bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800 rounded-lg p-4">
                            <div className="flex items-center gap-2">
                                <AlertCircle className="w-4 h-4 text-red-600 dark:text-red-400" />
                                <p className="text-sm text-red-700 dark:text-red-300">
                                    {error}
                                </p>
                            </div>
                        </div>
                    )}

                    {/* Resend Button */}
                    <div className="space-y-3">
                        <p className="text-sm text-gray-600 dark:text-gray-400 text-center">
                            Vous n'avez pas reçu l'email ?
                        </p>
                        <Button
                            onClick={handleResend}
                            disabled={resending}
                            variant="outline"
                            className="w-full"
                        >
                            {resending ? "Envoi en cours..." : "Renvoyer l'email"}
                        </Button>
                    </div>

                    {/* Back to Login */}
                    <div className="text-center">
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
