import {useState} from "react";
import {ChevronRight, Eye, EyeOff, Lock, Mail, PawPrint} from "lucide-react";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Checkbox} from "@/components/ui/checkbox";
import {Label} from "@/components/ui/label";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Separator} from "@/components/ui/separator";
import {api} from "@/lib/api";
import {useGoogleLogin} from '@react-oauth/google';
import {Link} from "react-router-dom";
import {isAxiosError} from "axios";
import {FacebookIcon, GoogleIcon} from "@/components/ui/Icons";
import {useAuth} from "@/context/AuthContext";
import {z, ZodError} from "zod";

const loginSchema = z.object({
    email: z.string().regex(/^[^\s@]+@[^\s@]+\.[^\s@]+$/, "Invalid email format"),
    password: z.string().min(8, "Mot de passe trop court"),
});

export default function LoginPage() {
    const [showPassword, setShowPassword] = useState(false);
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const {login} = useAuth();

    const handleLogin = async () => {
        try {
            const payload = loginSchema.parse({email, password});
            const {data} = await api.post('/auth/login', payload);
            login(data.user, data.accessToken);
            setError(null);
        } catch (error) {
            if (error instanceof ZodError) {
                setError(error.issues[0]?.message || "Champs invalides");
                return;
            }

            if (isAxiosError(error)) {
                setError(error.response?.data?.message || "Erreur de connexion");
                return;
            }

            console.error(error);
        }
    };

    const googleLogin = useGoogleLogin({
        onSuccess: async (tokenResponse) => {
            try {
                const {data} = await api.post('/auth/social', {
                    token: tokenResponse.access_token,
                    provider: 'GOOGLE'
                });
                login(data.user, data.accessToken);
            } catch (err) {
                console.error(err);
            }
        },
    });

    return (
        <div
            className="min-h-screen bg-gradient-to-br from-rose-50 via-pink-50 to-amber-50 flex items-center justify-center p-6 relative overflow-hidden dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
            <Card
                className="w-full max-w-md relative z-10 border-white/50 bg-white/80 dark:bg-slate-900/80 backdrop-blur-sm shadow-xl dark:border-slate-800">
                <CardHeader className="text-center pt-10">
                    <div
                        className="mx-auto w-20 h-20 bg-gradient-to-br from-rose-400 to-pink-500 rounded-3xl mb-4 flex items-center justify-center shadow-lg shadow-rose-200 dark:shadow-none">
                        <PawPrint size={40} className="text-white"/>
                    </div>
                    <CardTitle className="text-3xl font-bold text-rose-500 mb-1">
                        PetCare
                    </CardTitle>
                    <CardDescription className="text-gray-500 dark:text-gray-400 font-medium">
                        Bienvenue ! Connectez-vous pour continuer
                    </CardDescription>
                </CardHeader>

                <CardContent className="p-8 space-y-6">
                    <div className="space-y-5">
                        {error &&
                            <p className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</p>}
                        <div className="space-y-1.5">
                            <Label className="text-gray-600 dark:text-gray-300 font-medium pl-1">Email</Label>
                            <Input
                                icon={Mail}
                                className="h-12 bg-slate-50 dark:bg-slate-950 border-transparent focus:bg-white dark:focus:bg-slate-900 focus:border-rose-200 rounded-xl pl-11 text-gray-600 dark:text-white shadow-sm"
                                placeholder="votre@email.com"
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                            />
                        </div>

                        <div className="space-y-1.5">
                            <Label className="text-gray-600 dark:text-gray-300 font-medium pl-1">Mot de passe</Label>
                            <div className="relative">
                                <Input
                                    icon={Lock}
                                    className="h-12 bg-slate-50 dark:bg-slate-950 border-transparent focus:bg-white dark:focus:bg-slate-900 focus:border-rose-200 rounded-xl pl-11 pr-10 text-gray-600 dark:text-white shadow-sm"
                                    placeholder="••••••••"
                                    type={showPassword ? "text" : "password"}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
                                >
                                    {showPassword ? <EyeOff size={20}/> : <Eye size={20}/>}
                                </button>
                            </div>
                        </div>

                        <div className="flex items-center justify-between pt-1">
                            <div className="flex items-center space-x-2">
                                <Checkbox id="remember" className="border-gray-300 data-[state=checked]:bg-rose-500"/>
                                <Label htmlFor="remember"
                                       className="text-sm text-gray-500 dark:text-gray-400 cursor-pointer">Se souvenir
                                    de moi</Label>
                            </div>
                            <button className="text-sm text-rose-500 hover:text-rose-600 font-semibold">
                                Mot de passe oublié ?
                            </button>
                        </div>

                        <Button
                            className="w-full h-14 text-base font-bold rounded-2xl bg-gradient-to-r from-[#FF6B6B] to-[#FF8E8E] hover:opacity-90 shadow-md shadow-rose-100 dark:shadow-none text-white"
                            onClick={handleLogin}>
                            Se connecter <ChevronRight className="ml-2 h-5 w-5"/>
                        </Button>
                    </div>

                    <div className="relative py-2">
                        <div className="absolute inset-0 flex items-center"><Separator
                            className="bg-gray-100 dark:bg-slate-800"/></div>
                        <div className="relative flex justify-center text-xs uppercase"><span
                            className="bg-white dark:bg-slate-900 px-4 text-gray-400 font-medium">ou</span></div>
                    </div>

                    <div className="flex gap-4">
                        <Button variant="outline"
                                className="flex-1 h-12 rounded-xl border-gray-100 dark:border-slate-800 bg-white dark:bg-slate-950 font-semibold shadow-sm"
                                onClick={() => googleLogin()}>
                            <GoogleIcon className="mr-2 h-5 w-5"/> Google
                        </Button>
                        <Button variant="outline"
                                className="flex-1 h-12 rounded-xl border-gray-100 dark:border-slate-800 bg-white dark:bg-slate-950 font-semibold shadow-sm">
                            <FacebookIcon className="mr-2 h-5 w-5"/> Facebook
                        </Button>
                    </div>

                    <p className="text-center text-sm text-gray-500 dark:text-gray-400 mt-6 font-medium">
                        Pas encore de compte ? <Link to="/register"
                                                     className="text-rose-500 font-bold hover:underline ml-1">Créer un
                        compte</Link>
                    </p>
                </CardContent>
            </Card>
        </div>
    );
}