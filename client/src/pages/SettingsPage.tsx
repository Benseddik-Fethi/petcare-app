import {useEffect, useState} from "react";
import {useAuth} from "@/context/AuthContext";
import {useTheme} from "@/components/ThemeProvider";
import {Card} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Switch} from "@/components/ui/switch";
import {Bell, Camera, HelpCircle, LogOut, Mail, MapPin, Moon, Palette, Phone, Shield, Sun, User} from "lucide-react";
import {cn} from "@/lib/utils";

export default function SettingsPage() {
    const {user, logout} = useAuth();
    const {theme, setTheme} = useTheme();
    const [activeSection, setActiveSection] = useState("profile");

    const isSystemDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
    const isDarkMode = theme === "dark" || (theme === "system" && isSystemDark);

    const toggleTheme = (checked: boolean) => {
        console.log("1. [SettingsPage] Clic détecté ! Valeur reçue :", checked);

        const newTheme = checked ? "dark" : "light";
        console.log("2. [SettingsPage] J'envoie ce thème au Provider :", newTheme);

        setTheme(newTheme);
    };

    const menuItems = [
        {id: "profile", label: "Mon profil", icon: User},
        {id: "notifications", label: "Notifications", icon: Bell},
        {id: "security", label: "Sécurité", icon: Shield},
        {id: "appearance", label: "Apparence", icon: Palette},
        {id: "help", label: "Aide", icon: HelpCircle},
    ];
    useEffect(() => {
        console.log("🔄 Le thème a été mis à jour :", theme);
    }, [theme]);
    return (
        <div className="space-y-6 pb-10">
            <div>
                <h1 className="text-3xl font-bold text-gray-800 dark:text-white">Paramètres</h1>
                <p className="text-gray-500 dark:text-gray-400 mt-1">Gérez votre compte et vos préférences</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
                {/* MENU LATÉRAL */}
                <Card
                    className="p-4 h-fit lg:col-span-1 bg-white/80 dark:bg-slate-900/80 backdrop-blur-sm border-white/20">
                    <nav className="space-y-1">
                        {menuItems.map((item) => {
                            const Icon = item.icon;
                            const isActive = activeSection === item.id;
                            return (
                                <button
                                    key={item.id}
                                    onClick={() => setActiveSection(item.id)}
                                    className={cn(
                                        "w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300 text-sm font-medium",
                                        isActive
                                            ? "bg-gradient-to-r from-rose-400 to-pink-400 text-white shadow-md"
                                            : "text-gray-600 dark:text-gray-300 hover:bg-rose-50 dark:hover:bg-slate-800"
                                    )}
                                >
                                    <Icon size={18}/>
                                    {item.label}
                                </button>
                            );
                        })}
                    </nav>

                    <div className="mt-6 pt-6 border-t border-gray-100 dark:border-slate-800">
                        <button
                            onClick={logout}
                            className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all text-sm font-medium"
                        >
                            <LogOut size={18}/>
                            Déconnexion
                        </button>
                    </div>
                </Card>

                {/* CONTENU */}
                <div className="lg:col-span-3">

                    {/* SECTION PROFIL (Inchangée mais incluse pour contexte) */}
                    {activeSection === "profile" && (
                        <Card
                            className="p-8 animate-in fade-in slide-in-from-right-4 dark:bg-slate-900 border-white/10">
                            <div
                                className="flex items-center gap-6 mb-8 pb-8 border-b border-gray-100 dark:border-slate-800">
                                <div className="relative">
                                    <div
                                        className="w-24 h-24 bg-gradient-to-br from-amber-300 to-rose-300 rounded-full flex items-center justify-center text-white text-3xl font-bold shadow-lg">
                                        {user?.firstName?.charAt(0)}
                                    </div>
                                    <button
                                        className="absolute -bottom-1 -right-1 w-8 h-8 bg-white dark:bg-slate-800 rounded-full shadow-md flex items-center justify-center text-gray-400 hover:text-rose-500 transition-colors">
                                        <Camera size={14}/>
                                    </button>
                                </div>
                                <div>
                                    <h3 className="font-bold text-gray-800 dark:text-white text-xl">{user?.firstName} {user?.lastName}</h3>
                                    <p className="text-gray-500 dark:text-gray-400">{user?.email}</p>
                                    <span
                                        className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-rose-100 text-rose-800 mt-2">
                    Propriétaire
                  </span>
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div className="space-y-2">
                                    <Label>Prénom</Label>
                                    <Input defaultValue={user?.firstName ?? ""} className="bg-gray-50 dark:bg-slate-950"/>
                                </div>
                                <div className="space-y-2">
                                    <Label>Nom</Label>
                                    <Input defaultValue={user?.lastName ?? ""} className="bg-gray-50 dark:bg-slate-950"/>
                                </div>
                                <div className="space-y-2">
                                    <Label>Email</Label>
                                    <Input icon={Mail} defaultValue={user?.email} disabled
                                           className="bg-gray-100 dark:bg-slate-900 opacity-70"/>
                                </div>
                                <div className="space-y-2">
                                    <Label>Téléphone</Label>
                                    <Input icon={Phone} placeholder="Ajouter un numéro"
                                           className="bg-gray-50 dark:bg-slate-950"/>
                                </div>
                                <div className="col-span-full space-y-2">
                                    <Label>Adresse</Label>
                                    <Input icon={MapPin} placeholder="Votre adresse"
                                           className="bg-gray-50 dark:bg-slate-950"/>
                                </div>
                            </div>

                            <div className="mt-8 flex justify-end">
                                <Button
                                    className="bg-gray-900 text-white hover:bg-gray-800 dark:bg-white dark:text-black">
                                    Enregistrer
                                </Button>
                            </div>
                        </Card>
                    )}

                    {/* 🟢 SECTION APPARENCE (Mise à jour avec Switch) */}
                    {activeSection === "appearance" && (
                        <Card
                            className="p-8 animate-in fade-in slide-in-from-right-4 dark:bg-slate-900 border-white/10">
                            <h2 className="text-xl font-bold text-gray-800 dark:text-white mb-6 flex items-center gap-2">
                                <Palette className="text-rose-500"/> Apparence
                            </h2>

                            <div className="space-y-6">
                                {/* Option Dark Mode */}
                                <div
                                    className="flex items-center justify-between p-4 bg-gray-50 dark:bg-slate-950 rounded-2xl border border-gray-100 dark:border-slate-800">
                                    <div className="flex items-center gap-4">
                                        <div
                                            className={cn("w-12 h-12 rounded-xl flex items-center justify-center transition-colors",
                                                isDarkMode ? "bg-indigo-100 text-indigo-600" : "bg-amber-100 text-amber-600"
                                            )}>
                                            {isDarkMode ? <Moon size={24}/> : <Sun size={24}/>}
                                        </div>
                                        <div>
                                            <p className="font-bold text-gray-800 dark:text-white">Mode Sombre</p>
                                            <p className="text-sm text-gray-500 dark:text-gray-400">
                                                {isDarkMode ? "Activé (Thème sombre)" : "Désactivé (Thème clair)"}
                                            </p>
                                        </div>
                                    </div>
                                    <Switch
                                        onCheckedChange={toggleTheme}
                                        className="data-[state=checked]:bg-indigo-500 data-[state=unchecked]:bg-gray-200"
                                    />
                                </div>

                                <div
                                    className="flex items-center justify-between p-4 bg-gray-50 dark:bg-slate-950 rounded-2xl border border-gray-100 dark:border-slate-800 opacity-50 cursor-not-allowed">
                                    <div className="flex items-center gap-4">
                                        <div
                                            className="w-12 h-12 rounded-xl bg-gray-200 flex items-center justify-center text-gray-500">
                                            <Palette size={24}/>
                                        </div>
                                        <div>
                                            <p className="font-bold text-gray-800 dark:text-white">Thème automatique</p>
                                            <p className="text-sm text-gray-500 dark:text-gray-400">Synchroniser avec le
                                                système</p>
                                        </div>
                                    </div>
                                    <Switch disabled checked={false}/>
                                </div>
                            </div>
                        </Card>
                    )}

                    {/* ... autres sections (notifications, security...) inchangées ... */}
                    {["notifications", "security", "help"].includes(activeSection) && (
                        <Card
                            className="p-12 text-center text-gray-400 border-dashed border-2 dark:bg-slate-900 dark:border-slate-800">
                            <p>Section en cours de développement</p>
                        </Card>
                    )}
                </div>
            </div>
        </div>
    );
}