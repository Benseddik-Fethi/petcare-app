import { useState } from "react";
import { Mail, PawPrint, Send, CheckCircle, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Link } from "react-router-dom";
import { resendVerificationEmail } from "@/lib/api";
import { isAxiosError } from "axios";
import { z } from "zod";

const emailSchema = z.string().email("Format d'email invalide");

export default function ResendVerificationPage() {
    const [email, setEmail] = useState("");
    const [sending, setSending] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            setSending(true);
            setError(null);
            setSuccess(false);

            // Validate email
            const validEmail = emailSchema.parse(email);

            await resendVerificationEmail(validEmail);

            setSuccess(true);
            setEmail(""); // Clear form
        } catch (err) {
            if (err instanceof z.ZodError) {
                setError("Format d'email invalide");
            } else if (isAxiosError(err)) {
                setError(err.response?.data?.message || "Erreur lors de l'envoi de l'email");
            } else {
                setError("Une erreur est survenue");
            }
        } finally {
            setSending(false);
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
                        Renvoyer l'email
                    </CardTitle>
                    <CardDescription className="text-gray-500 dark:text-gray-400 font-medium">
                        Vous n'avez pas reçu l'email de vérification ?
                    </CardDescription>
                </CardHeader>

                <CardContent className="px-8 pb-8 space-y-6">
                    {!success ? (
                        <>
                            {/* Form */}
                            <form onSubmit={handleSubmit} className="space-y-4">
                                <div className="space-y-2">
                                    <Label htmlFor="email" className="text-gray-700 dark:text-gray-300">
                                        Adresse email
                                    </Label>
                                    <div className="relative">
                                        <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                                        <Input
                                            id="email"
                                            type="email"
                                            placeholder="votre@email.com"
                                            value={email}
                                            onChange={(e) => setEmail(e.target.value)}
                                            className="pl-10"
                                            required
                                        />
                                    </div>
                                    <p className="text-xs text-gray-500 dark:text-gray-400">
                                        Entrez l'adresse email utilisée lors de l'inscription
                                    </p>
                                </div>

                                {/* Error Message */}
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

                                {/* Submit Button */}
                                <Button
                                    type="submit"
                                    disabled={sending}
                                    className="w-full bg-gradient-to-r from-rose-500 to-pink-500 hover:from-rose-600 hover:to-pink-600"
                                >
                                    {sending ? (
                                        <>
                                            <Send className="w-4 h-4 mr-2 animate-pulse" />
                                            Envoi en cours...
                                        </>
                                    ) : (
                                        <>
                                            <Send className="w-4 h-4 mr-2" />
                                            Renvoyer l'email
                                        </>
                                    )}
                                </Button>
                            </form>

                            {/* Info */}
                            <div className="bg-blue-50 dark:bg-blue-950/30 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
                                <div className="flex items-start gap-3">
                                    <Mail className="w-5 h-5 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-0.5" />
                                    <div className="flex-1">
                                        <p className="text-sm font-medium text-blue-900 dark:text-blue-100">
                                            Vérifiez vos spams
                                        </p>
                                        <p className="text-sm text-blue-700 dark:text-blue-300 mt-1">
                                            Si vous ne trouvez pas l'email, vérifiez votre dossier spam ou courrier indésirable.
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </>
                    ) : (
                        <>
                            {/* Success Message */}
                            <div className="bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 rounded-lg p-4">
                                <div className="flex items-start gap-3">
                                    <CheckCircle className="w-5 h-5 text-emerald-600 dark:text-emerald-400 flex-shrink-0 mt-0.5" />
                                    <div className="flex-1">
                                        <p className="text-sm font-medium text-emerald-900 dark:text-emerald-100">
                                            Email envoyé !
                                        </p>
                                        <p className="text-sm text-emerald-700 dark:text-emerald-300 mt-1">
                                            Si un compte existe avec cette adresse email et n'est pas encore vérifié, nous avons envoyé un nouveau lien de vérification.
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
                                        <span>Consultez votre boîte de réception</span>
                                    </li>
                                    <li className="flex gap-2">
                                        <span className="font-semibold text-rose-500">2.</span>
                                        <span>Cliquez sur le lien de vérification</span>
                                    </li>
                                    <li className="flex gap-2">
                                        <span className="font-semibold text-rose-500">3.</span>
                                        <span>Votre compte sera activé</span>
                                    </li>
                                </ol>
                            </div>

                            {/* Send Another */}
                            <Button
                                onClick={() => setSuccess(false)}
                                variant="outline"
                                className="w-full"
                            >
                                Envoyer à une autre adresse
                            </Button>
                        </>
                    )}

                    {/* Back to Login */}
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
