import {useEffect, useState} from "react";
import {api} from "@/lib/api";
import {useAuth} from "@/context/AuthContext";
import {Card} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Activity, Bell, Calendar, ChevronRight, Clock, Dog, Search, User} from "lucide-react";
import {Input} from "@/components/ui/input";

import {format, isAfter, parseISO} from "date-fns";
import {fr} from "date-fns/locale";
import {useNavigate} from "react-router-dom";
import {NewAppointmentModal} from "@/components/NewAppointmentModal.tsx";

interface Pet {
    id: string;
    name: string;
    breed?: string;
    avatar?: string;
    species: string;
}

interface Appointment {
    id: string;
    date: string;
    type: string;
    status: string;
    vet: { name: string };
    pet: { name: string };
}

export default function DashboardPage() {
    const {user} = useAuth();
    const navigate = useNavigate();
    const [pets, setPets] = useState<Pet[]>([]);
    const [appointments, setAppointments] = useState<Appointment[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [petsRes, apptsRes] = await Promise.all([
                    api.get<Pet[]>('/pets'),
                    api.get<Appointment[]>('/appointments')
                ]);
                setPets(petsRes.data);
                setAppointments(apptsRes.data);
            } catch (error) {
                console.error(error);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    // --- CALCUL DES STATS (KPIs) ---
    const upcomingApptsCount = appointments.filter(a => isAfter(parseISO(a.date), new Date())).length;
    // Pour l'instant on mocke les vaccins à jour, on pourra le calculer plus tard via l'API
    const activeAlerts = 0;

    return (
        <div className="space-y-8 pb-10">
            {/* Header */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold text-gray-800 dark:text-white">Bonjour, {user?.firstName} ! 👋</h1>
                    <p className="text-gray-500 dark:text-gray-400 mt-1">Voici ce qu'il se passe aujourd'hui</p>
                </div>
                <div className="flex items-center gap-4">
                    <Input
                        icon={Search}
                        placeholder="Rechercher..."
                        className="w-full md:w-64 bg-white dark:bg-slate-950 border-rose-100 dark:border-slate-800 focus:border-rose-300 dark:focus:border-rose-500 text-gray-800 dark:text-white"
                    />
                    <Button variant="outline" size="icon"
                            className="shrink-0 rounded-2xl relative bg-white dark:bg-slate-950 border-rose-100 dark:border-slate-800 hover:bg-rose-50 dark:hover:bg-slate-900 text-gray-500 dark:text-gray-400">
                        <Bell size={20}/>
                        <span className="absolute top-2 right-2 w-2.5 h-2.5 bg-rose-500 rounded-full"/>
                    </Button>
                </div>
            </div>

            {/* 1. STATISTIQUES (KPIs) */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <Card
                    className="p-6 border-none shadow-sm bg-gradient-to-br from-rose-50 to-white dark:from-slate-900 dark:to-slate-900/50 rounded-3xl flex items-center gap-4">
                    <div
                        className="w-14 h-14 rounded-2xl bg-rose-100 dark:bg-rose-900/20 flex items-center justify-center text-rose-500">
                        <Dog size={28}/>
                    </div>
                    <div>
                        <p className="text-gray-500 dark:text-gray-400 text-sm font-medium">Mes Compagnons</p>
                        <p className="text-3xl font-bold text-gray-800 dark:text-white">{pets.length}</p>
                    </div>
                </Card>

                <Card
                    className="p-6 border-none shadow-sm bg-gradient-to-br from-sky-50 to-white dark:from-slate-900 dark:to-slate-900/50 rounded-3xl flex items-center gap-4">
                    <div
                        className="w-14 h-14 rounded-2xl bg-sky-100 dark:bg-sky-900/20 flex items-center justify-center text-sky-500">
                        <Calendar size={28}/>
                    </div>
                    <div>
                        <p className="text-gray-500 dark:text-gray-400 text-sm font-medium">RDV à venir</p>
                        <p className="text-3xl font-bold text-gray-800 dark:text-white">{upcomingApptsCount}</p>
                    </div>
                </Card>

                <Card
                    className="p-6 border-none shadow-sm bg-gradient-to-br from-emerald-50 to-white dark:from-slate-900 dark:to-slate-900/50 rounded-3xl flex items-center gap-4">
                    <div
                        className="w-14 h-14 rounded-2xl bg-emerald-100 dark:bg-emerald-900/20 flex items-center justify-center text-emerald-500">
                        <Activity size={28}/>
                    </div>
                    <div>
                        <p className="text-gray-500 dark:text-gray-400 text-sm font-medium">Alertes Santé</p>
                        <p className="text-3xl font-bold text-gray-800 dark:text-white">{activeAlerts}</p>
                    </div>
                </Card>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

                {/* 2. ACCÈS RAPIDE (Compact) */}
                <div className="lg:col-span-2 space-y-6">
                    <div className="flex items-center justify-between">
                        <h2 className="text-xl font-bold text-gray-800 dark:text-white">Accès Rapide</h2>
                        <Button variant="link" onClick={() => navigate('/pets')}
                                className="text-rose-500 hover:text-rose-600 p-0 h-auto font-semibold">
                            Voir tous les animaux <ChevronRight size={16}/>
                        </Button>
                    </div>

                    {loading ? (
                        <div className="text-center py-10 text-gray-400">Chargement...</div>
                    ) : pets.length === 0 ? (
                        <div
                            className="p-8 text-center border-2 border-dashed rounded-3xl border-gray-200 dark:border-slate-800">
                            <p className="text-gray-400">Aucun animal</p>
                            <Button variant="link" onClick={() => navigate('/pets')} className="text-rose-500">En
                                ajouter un</Button>
                        </div>
                    ) : (
                        /* Liste Horizontale Compacte */
                        <div className="flex gap-4 overflow-x-auto pb-2">
                            {pets.map(pet => (
                                <Card
                                    key={pet.id}
                                    onClick={() => navigate(`/pets/${pet.id}`)}
                                    className="min-w-[160px] p-4 cursor-pointer hover:shadow-md hover:-translate-y-1 transition-all duration-300 bg-white dark:bg-slate-900 border-white dark:border-slate-800 shadow-sm rounded-3xl flex flex-col items-center text-center gap-3"
                                >
                                    <div
                                        className="w-16 h-16 bg-gradient-to-br from-amber-100 to-rose-100 dark:from-slate-800 dark:to-slate-700 rounded-2xl flex items-center justify-center text-3xl shadow-inner">
                                        {pet.avatar || "🐾"}
                                    </div>
                                    <div>
                                        <h3 className="font-bold text-gray-800 dark:text-white truncate w-full">{pet.name}</h3>
                                        <p className="text-xs text-gray-500 dark:text-gray-400">{pet.breed || pet.species}</p>
                                    </div>
                                </Card>
                            ))}

                            {/* Bouton "Plus" pour aller vers /pets */}
                            <button
                                onClick={() => navigate('/pets')}
                                className="min-w-[60px] rounded-3xl border-2 border-dashed border-gray-200 dark:border-slate-800 flex items-center justify-center text-gray-400 hover:border-rose-300 hover:text-rose-500 transition-colors"
                            >
                                <ChevronRight/>
                            </button>
                        </div>
                    )}
                </div>

                {/* 3. PROCHAINS RDV */}
                <div className="space-y-6">
                    <div className="flex items-center justify-between">
                        <h2 className="text-xl font-bold text-gray-800 dark:text-white">Prochains RDV</h2>
                        <NewAppointmentModal/>
                    </div>

                    <Card
                        className="p-5 bg-white dark:bg-slate-900 border-white dark:border-slate-800 shadow-sm rounded-3xl min-h-[200px]">
                        {appointments.length === 0 ? (
                            <div
                                className="h-full flex flex-col items-center justify-center text-gray-400 dark:text-gray-600 py-8">
                                <Calendar size={32} className="mb-2 opacity-50"/>
                                <p className="text-sm">Aucun rendez-vous prévu</p>
                            </div>
                        ) : (
                            <div className="space-y-3">
                                {appointments.slice(0, 3).map((apt) => (
                                    <div key={apt.id}
                                         className="flex gap-3 p-3 rounded-2xl bg-gray-50 dark:bg-slate-950 border border-gray-100 dark:border-slate-800 hover:bg-rose-50 dark:hover:bg-slate-900 transition-colors group cursor-pointer">
                                        <div
                                            className="flex flex-col items-center justify-center w-12 bg-white dark:bg-slate-900 rounded-xl border border-gray-100 dark:border-slate-700 shadow-sm shrink-0">
                                            <span
                                                className="text-xs font-bold text-rose-500">{format(new Date(apt.date), 'dd')}</span>
                                            <span
                                                className="text-[10px] text-gray-400 uppercase">{format(new Date(apt.date), 'MMM', {locale: fr})}</span>
                                        </div>
                                        <div className="flex-1 min-w-0">
                                            <p className="font-bold text-gray-800 dark:text-gray-200 text-sm truncate">{apt.type}</p>
                                            <div
                                                className="flex items-center gap-2 text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                                                <span className="flex items-center gap-1"><Clock
                                                    size={10}/> {format(new Date(apt.date), 'HH:mm')}</span>
                                                <span>•</span>
                                                <span className="flex items-center gap-1"><Dog
                                                    size={10}/> {apt.pet.name}</span>
                                            </div>
                                            <div
                                                className="flex items-center gap-1 text-xs text-gray-400 dark:text-gray-500 mt-1">
                                                <User size={10}/> {apt.vet.name}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </Card>
                </div>
            </div>
        </div>
    );
}