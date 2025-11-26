import {useState} from "react";
import {ChevronRight, Eye, EyeOff, Lock, Mail, PawPrint} from "lucide-react";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Separator} from "@/components/ui/separator";
import {api} from "@/lib/api";
import {useGoogleLogin} from '@react-oauth/google';
import {Link} from "react-router-dom";
import {FacebookIcon, GoogleIcon} from "@/components/ui/Icons";
import {isAxiosError} from "axios";
import {useAuth} from "@/context/AuthContext";
import {Label} from "@/components/ui/label";

export default function RegisterPage() {
    const [showPassword, setShowPassword] = useState(false);
    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        confirmPassword: "",
    });
    const {login} = useAuth();

    const handleRegister = async () => {
        if (formData.password !== formData.confirmPassword) {
            alert("Les mots de passe ne correspondent pas");
            return;
        }
        try {
            const {data} = await api.post('/auth/register', {
                email: formData.email,
                password: formData.password,
                firstName: formData.firstName,
                lastName: formData.lastName
            });
            login(data.user, data.accessToken);
        } catch (error) {
            if (isAxiosError(error)) {
                alert(error.response?.data?.message || "Erreur lors de l'inscription");
            } else {
                console.error(error);
            }
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
            className="min-h-screen bg-gradient-to-br from-amber-50 via-rose-50 to-pink-50 flex items-center justify-center p-6 relative overflow-hidden dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
            <Card
                className="w-full max-w-md relative z-10 border-white/50 bg-white/80 dark:bg-slate-900/80 backdrop-blur-sm shadow-xl dark:border-slate-800">
                <CardHeader className="text-center pt-10">
                    <div
                        className="mx-auto w-16 h-16 bg-gradient-to-br from-amber-400 to-rose-400 rounded-2xl flex items-center justify-center mb-3 shadow-lg shadow-rose-200 dark:shadow-none">
                        <PawPrint size={32} className="text-white"/>
                    </div>
                    <CardTitle className="text-2xl font-bold text-amber-500 mb-1">
                        Créer un compte
                    </CardTitle>
                    <CardDescription className="text-gray-500 dark:text-gray-400">Rejoignez la communauté
                        PetCare</CardDescription>
                </CardHeader>

                <CardContent className="p-8 space-y-6">
                    <div className="space-y-4">
                        <div className="grid grid-cols-2 gap-3">
                            <div className="space-y-1.5">
                                <Label className="text-gray-600 dark:text-gray-300 pl-1">Prénom</Label>
                                <Input
                                    placeholder="Jean"
                                    className="h-11 bg-slate-50 dark:bg-slate-950 border-transparent focus:bg-white dark:focus:bg-slate-900 rounded-xl"
                                    value={formData.firstName}
                                    onChange={(e) => setFormData({...formData, firstName: e.target.value})}
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label className="text-gray-600 dark:text-gray-300 pl-1">Nom</Label>
                                <Input
                                    placeholder="Dupont"
                                    className="h-11 bg-slate-50 dark:bg-slate-950 border-transparent focus:bg-white dark:focus:bg-slate-900 rounded-xl"
                                    value={formData.lastName}
                                    onChange={(e) => setFormData({...formData, lastName: e.target.value})}
                                />
                            </div>
                        </div>

                        <div className="space-y-1.5">
                            <Label className="text-gray-600 dark:text-gray-300 pl-1">Email</Label>
                            <Input
                                icon={Mail}
                                placeholder="votre@email.com"
                                type="email"
                                className="h-11 pl-10 bg-slate-50 dark:bg-slate-950 border-transparent focus:bg-white dark:focus:bg-slate-900 rounded-xl"
                                value={formData.email}
                                onChange={(e) => setFormData({...formData, email: e.target.value})}
                            />
                        </div>

                        <div className="space-y-1.5">
                            <Label className="text-gray-600 dark:text-gray-300 pl-1">Mot de passe</Label>
                            <div className="relative">
                                <Input
                                    icon={Lock}
                                    placeholder="Min. 8 caractères"
                                    type={showPassword ? "text" : "password"}
                                    className="h-11 pl-10 pr-10 bg-slate-50 dark:bg-slate-950 border-transparent focus:bg-white dark:focus:bg-slate-900 rounded-xl"
                                    value={formData.password}
                                    onChange={(e) => setFormData({...formData, password: e.target.value})}
                                />
                                <button type="button" onClick={() => setShowPassword(!showPassword)}
                                        className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                                    {showPassword ? <EyeOff size={18}/> : <Eye size={18}/>}
                                </button>
                            </div>
                        </div>

                        <div className="space-y-1.5">
                            <Label className="text-gray-600 dark:text-gray-300 pl-1">Confirmation</Label>
                            <Input
                                icon={Lock}
                                placeholder="Répétez le mot de passe"
                                type="password"
                                className="h-11 pl-10 bg-slate-50 dark:bg-slate-950 border-transparent focus:bg-white dark:focus:bg-slate-900 rounded-xl"
                                value={formData.confirmPassword}
                                onChange={(e) => setFormData({...formData, confirmPassword: e.target.value})}
                            />
                        </div>

                        <Button
                            className="w-full h-12 text-base font-bold rounded-2xl bg-gradient-to-r from-amber-400 to-rose-400 hover:opacity-90 shadow-md shadow-amber-100 dark:shadow-none text-white mt-2"
                            onClick={handleRegister}>
                            Créer mon compte <ChevronRight className="ml-2 h-5 w-5"/>
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
                                className="flex-1 h-11 rounded-xl border-gray-100 dark:border-slate-800 bg-white dark:bg-slate-950 font-semibold shadow-sm"
                                onClick={() => googleLogin()}>
                            <GoogleIcon className="mr-2 h-4 w-4"/> Google
                        </Button>
                        <Button variant="outline"
                                className="flex-1 h-11 rounded-xl border-gray-100 dark:border-slate-800 bg-white dark:bg-slate-950 font-semibold shadow-sm">
                            <FacebookIcon className="mr-2 h-4 w-4"/> Facebook
                        </Button>
                    </div>

                    <p className="text-center text-sm text-gray-500 dark:text-gray-400 mt-4">
                        Déjà un compte ? <Link to="/login" className="text-rose-500 font-bold hover:underline ml-1">Se
                        connecter</Link>
                    </p>
                </CardContent>
            </Card>
        </div>
    );
}