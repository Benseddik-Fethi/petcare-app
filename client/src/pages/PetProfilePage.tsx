import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import {api} from "@/lib/api";
import {Card} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {
    Calendar,
    Camera,
    ChevronLeft,
    Clock,
    Edit3,
    FileText,
    Heart,
    Stethoscope,
    Syringe,
    TrendingUp,
    User
} from "lucide-react";
import {differenceInYears, format, isValid, parseISO} from "date-fns";
import {fr} from "date-fns/locale";
import {Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis} from "recharts";
import {AddWeightModal} from "@/components/AddWeightModal";
import {AddVaccineModal} from "@/components/AddVaccineModal";


// --- TYPES ---
// Ces types correspondent EXACTEMENT à ton JSON
interface PetDetails {
    species: string;
    id: string;
    name: string;
    breed?: string;
    gender?: string;
    birthDate?: string; // Peut être null
    microchip?: string;
    color?: string;
    avatar?: string;

    // Relations (Tableaux)
    appointments: {
        id: string;
        date: string;
        type: string;
        status: string;
        vet: { name: string };
    }[];

    vaccines: {
        id: string;
        name: string;
        date: string;
        nextDate: string;
    }[];

    weights: {
        id: string;
        date: string;
        weight: number
    }[];
}

export default function PetProfilePage() {
    const {id} = useParams();
    const navigate = useNavigate();
    const [pet, setPet] = useState<PetDetails | null>(null);
    const [loading, setLoading] = useState(true);

    const fetchPet = async () => {
        try {
            const {data} = await api.get(`/pets/${id}`);
            setPet(data);
        } catch (error) {
            console.error(error);
            navigate('/dashboard');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPet();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id]);

    if (loading || !pet) return <div className="p-8 text-center text-gray-500 dark:text-gray-400">Chargement...</div>;

    // --- CALCULS SÉCURISÉS ---

    // Âge
    let age = "Âge inconnu";
    if (pet.birthDate) {
        const birthDate = parseISO(pet.birthDate);
        if (isValid(birthDate)) {
            age = `${differenceInYears(new Date(), birthDate)} ans`;
        }
    }

    const sortedWeights = [...(pet.weights || [])].sort((a, b) =>
        new Date(a.date).getTime() - new Date(b.date).getTime()
    );

    const currentWeight = sortedWeights.length > 0
        ? sortedWeights[sortedWeights.length - 1].weight
        : null;

    // Tendance
    let trend = null;
    if (sortedWeights.length >= 2) {
        const last = sortedWeights[sortedWeights.length - 1].weight;
        const prev = sortedWeights[sortedWeights.length - 2].weight;
        const diff = (last - prev).toFixed(1);
        trend = Number(diff) > 0 ? `+${diff}` : diff;
    }

    // Données Graphique
    const chartData = sortedWeights.map(w => ({
        date: format(parseISO(w.date), 'MMM', {locale: fr}),
        fullDate: format(parseISO(w.date), 'dd MMMM yyyy', {locale: fr}),
        poids: w.weight
    }));

    // Historique (inversé pour afficher le plus récent en premier)
    const historyWeights = [...sortedWeights].reverse();

    return (
        <div className="space-y-6 pb-10">
            {/* ... Header inchangé ... */}
            <button onClick={() => navigate('/dashboard')}
                    className="flex items-center gap-2 text-gray-500 hover:text-rose-500 dark:text-gray-400 dark:hover:text-rose-400 transition-colors">
                <ChevronLeft size={20}/> Retour au dashboard
            </button>
            <Card
                className="p-8 bg-white/80 dark:bg-slate-900/80 backdrop-blur-sm border-white dark:border-slate-800 shadow-sm rounded-[32px]">
                <div className="flex flex-col md:flex-row items-center md:items-start gap-6 text-center md:text-left">
                    <div className="relative group shrink-0">
                        <div
                            className="w-32 h-32 bg-gradient-to-br from-amber-100 to-rose-100 dark:from-slate-800 dark:to-slate-700 rounded-3xl flex items-center justify-center text-6xl shadow-lg cursor-pointer">
                            {pet.avatar || "🐾"}
                        </div>
                        <button
                            className="absolute -bottom-2 -right-2 w-10 h-10 bg-white dark:bg-slate-800 rounded-xl shadow-lg flex items-center justify-center text-gray-400 dark:text-gray-500 hover:text-rose-500 transition-colors">
                            <Camera size={18}/>
                        </button>
                    </div>

                    <div className="flex-1 w-full">
                        <div className="flex flex-col md:flex-row items-center md:items-start justify-between gap-4">
                            <div>
                                <div className="flex items-center gap-3 justify-center md:justify-start mb-1">
                                    <h1 className="text-3xl font-bold text-gray-800 dark:text-white">{pet.name}</h1>
                                    <button
                                        className="p-2 text-gray-400 hover:text-rose-500 transition-colors rounded-full hover:bg-rose-50 dark:hover:bg-rose-900/20">
                                        <Edit3 size={18}/>
                                    </button>
                                </div>
                                <p className="text-lg text-gray-500 dark:text-gray-400">{pet.breed || "Race inconnue"}</p>

                                <div className="flex flex-wrap justify-center md:justify-start items-center gap-3 mt-4">
                                    {pet.gender && (
                                        <Badge variant="secondary"
                                               className="bg-sky-50 dark:bg-sky-900/30 text-sky-600 dark:text-sky-400 border-sky-100 dark:border-sky-800 rounded-lg">
                                            {pet.gender}
                                        </Badge>
                                    )}
                                    <Badge variant="secondary"
                                           className="bg-emerald-50 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 border-emerald-100 dark:border-emerald-800 rounded-lg">
                                        {age}
                                    </Badge>
                                    {currentWeight && (
                                        <Badge variant="secondary"
                                               className="bg-rose-50 dark:bg-rose-900/30 text-rose-600 dark:text-rose-400 border-rose-100 dark:border-rose-800 rounded-lg">
                                            {currentWeight} kg
                                        </Badge>
                                    )}
                                    {pet.color && (
                                        <Badge variant="secondary"
                                               className="bg-amber-50 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400 border-amber-100 dark:border-amber-800 rounded-lg">
                                            {pet.color}
                                        </Badge>
                                    )}
                                </div>
                            </div>

                            <div className="text-right hidden md:block">
                                <p className="text-[10px] uppercase tracking-widest text-gray-400 font-bold mb-1">N°
                                    Puce</p>
                                <p className="font-mono text-gray-600 dark:text-gray-300 text-sm mb-4">
                                    {pet.microchip || "Non pucé"}
                                </p>

                                <div>
                                    <p className="text-[10px] uppercase tracking-widest text-gray-400 font-bold mb-1">Né(e)
                                        le</p>
                                    <p className="text-gray-600 dark:text-gray-300 text-sm font-medium">
                                        {pet.birthDate
                                            ? format(parseISO(pet.birthDate), 'dd MMMM yyyy', {locale: fr})
                                            : "Inconnue"}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </Card>

            {/* ONGLETS */}
            <Tabs defaultValue="weight" className="w-full">
                <TabsList
                    className="bg-rose-50/60 dark:bg-slate-800/50 p-1.5 rounded-2xl mb-8 h-auto w-full justify-start overflow-x-auto">
                    {[
                        {id: "info", label: "Informations", icon: FileText},
                        {id: "appointments", label: "RDV Vétérinaires", icon: Stethoscope},
                        {id: "vaccinations", label: "Vaccinations", icon: Syringe},
                        {id: "weight", label: "Courbe de poids", icon: TrendingUp},
                    ].map((tab) => (
                        <TabsTrigger
                            key={tab.id}
                            value={tab.id}
                            className="rounded-xl px-5 py-3 gap-2 font-medium text-gray-500 dark:text-gray-400
                         data-[state=active]:bg-white dark:data-[state=active]:bg-slate-700
                         data-[state=active]:text-rose-500 dark:data-[state=active]:text-rose-400
                         data-[state=active]:shadow-sm transition-all"
                        >
                            <tab.icon size={18}/>
                            {tab.label}
                        </TabsTrigger>
                    ))}
                </TabsList>

                {/* 1. INFORMATIONS */}
                <TabsContent value="info" className="space-y-6 animate-in fade-in slide-in-from-bottom-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

                        {/* Carte Général */}
                        <Card
                            className="p-6 bg-white dark:bg-slate-900 border-white dark:border-slate-800 shadow-sm rounded-3xl">
                            <h3 className="font-bold text-gray-800 dark:text-white mb-4 flex items-center gap-2">
                                <FileText className="text-rose-400"/> Général
                            </h3>
                            <div className="space-y-1">
                                {/* 👇 UTILISATION DU COMPOSANT ROW ICI */}
                                <Row label="Nom" value={pet.name}/>
                                <Row label="Espèce" value={pet.species || "Non renseigné"}/>
                                <Row label="Race" value={pet.breed}/>
                                <Row
                                    label="Date de naissance"
                                    value={pet.birthDate ? format(parseISO(pet.birthDate), 'dd/MM/yyyy') : undefined}
                                />
                            </div>
                        </Card>

                        {/* Carte Santé */}
                        <Card
                            className="p-6 bg-white dark:bg-slate-900 border-white dark:border-slate-800 shadow-sm rounded-3xl">
                            <h3 className="font-bold text-gray-800 dark:text-white mb-4 flex items-center gap-2">
                                <Heart className="text-rose-400" size={20}/> Santé
                            </h3>
                            <div className="space-y-1">
                                {/* 👇 ET ICI */}
                                <Row label="Poids actuel" value={currentWeight ? `${currentWeight} kg` : undefined}/>
                                <Row label="Stérilisé" value="Non renseigné"/> {/* À connecter plus tard */}
                                <Row label="Identification" value={pet.microchip}/>
                            </div>
                        </Card>
                    </div>
                </TabsContent>

                {/* 2. RDV */}
                <TabsContent value="appointments" className="animate-in fade-in slide-in-from-bottom-4">
                    <Card
                        className="p-6 bg-white dark:bg-slate-900 border-white dark:border-slate-800 shadow-sm rounded-3xl">
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="font-bold text-gray-800 dark:text-white">Historique</h3>
                            <Button size="sm" variant="outline" onClick={() => navigate('/calendar')}
                                    className="rounded-xl">Voir calendrier</Button>
                        </div>

                        {/* Vérification de l'existence des données */}
                        {pet.appointments && pet.appointments.length > 0 ? (
                            <div className="space-y-4">
                                {pet.appointments.map((apt) => (
                                    <div key={apt.id}
                                         className="flex items-start gap-4 p-4 bg-gray-50 dark:bg-slate-950 rounded-2xl hover:bg-rose-50 dark:hover:bg-slate-900 border border-transparent hover:border-rose-100 dark:hover:border-slate-800 transition-colors">
                                        <div
                                            className="w-12 h-12 bg-white dark:bg-slate-900 rounded-xl flex items-center justify-center shadow-sm text-rose-500 shrink-0 border border-gray-100 dark:border-slate-800">
                                            <Stethoscope size={20}/>
                                        </div>
                                        <div className="flex-1">
                                            <div className="flex justify-between items-start">
                                                <h4 className="font-bold text-gray-800 dark:text-gray-200">{apt.type}</h4>
                                                <Badge variant={apt.status === 'upcoming' ? 'default' : 'secondary'}
                                                       className={apt.status === 'upcoming' ? 'bg-amber-500 hover:bg-amber-600' : ''}>
                                                    {apt.status === 'upcoming' ? 'À venir' : 'Terminé'}
                                                </Badge>
                                            </div>
                                            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1 flex flex-wrap items-center gap-3">
                                <span className="flex items-center gap-1">
                                    <Calendar size={14}/>
                                    {apt.date ? format(parseISO(apt.date), 'dd MMM yyyy', {locale: fr}) : '-'}
                                </span>
                                                <span className="flex items-center gap-1">
                                    <Clock size={14}/>
                                                    {apt.date ? format(parseISO(apt.date), 'HH:mm') : '-'}
                                </span>
                                                <span className="flex items-center gap-1">
                                    <User size={14}/>
                                                    {apt.vet?.name || "Vétérinaire inconnu"}
                                </span>
                                            </p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="text-center py-10">
                                <Calendar size={40} className="mx-auto text-gray-300 mb-2"/>
                                <p className="text-gray-400">Aucun rendez-vous enregistré.</p>
                            </div>
                        )}
                    </Card>
                </TabsContent>

                {/* 3. VACCINS (Corrigé pour afficher même si null) */}
                <TabsContent value="vaccinations" className="animate-in fade-in slide-in-from-bottom-4">
                    <Card
                        className="p-6 bg-white dark:bg-slate-900 border-white dark:border-slate-800 shadow-sm rounded-3xl">
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="font-bold text-gray-800 dark:text-white">Carnet de vaccination</h3>
                            <AddVaccineModal petId={pet.id} onSuccess={fetchPet}/>
                        </div>

                        {/* Vérification explicite du tableau */}
                        {pet.vaccines && pet.vaccines.length > 0 ? (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                {pet.vaccines.map((vac) => (
                                    <div key={vac.id}
                                         className="p-4 border border-emerald-100 dark:border-emerald-900/50 bg-emerald-50/30 dark:bg-emerald-900/10 rounded-2xl">
                                        <div className="flex items-center gap-3 mb-3">
                                            <div
                                                className="w-10 h-10 bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 rounded-xl flex items-center justify-center">
                                                <Syringe size={18}/>
                                            </div>
                                            <div className="font-bold text-gray-800 dark:text-gray-200">{vac.name}</div>
                                        </div>
                                        <div className="text-sm text-gray-600 dark:text-gray-400 space-y-1">
                                            <div className="flex justify-between">
                                                <span>Fait le :</span>
                                                <span>{format(parseISO(vac.date), 'dd/MM/yyyy')}</span>
                                            </div>
                                            {vac.nextDate && (
                                                <div
                                                    className="flex justify-between font-medium text-emerald-700 dark:text-emerald-400">
                                                    <span>Rappel :</span>
                                                    <span>{format(parseISO(vac.nextDate), 'dd/MM/yyyy')}</span>
                                                </div>

                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div
                                className="col-span-full text-center py-12 bg-gray-50 dark:bg-slate-900/50 rounded-3xl border-2 border-dashed border-gray-200 dark:border-slate-800">
                                <Syringe size={32} className="mx-auto text-gray-300 mb-3"/>
                                <p className="text-gray-500">Aucun vaccin enregistré.</p>
                            </div>
                        )}
                    </Card>
                </TabsContent>

                {/* 4. POIDS (Corrigé avec Tri) */}
                <TabsContent value="weight" className="animate-in fade-in slide-in-from-bottom-4">
                    <Card
                        className="p-8 bg-white dark:bg-slate-900 border-white dark:border-slate-800 shadow-sm rounded-3xl">

                        <div
                            className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
                            <div>
                                <h3 className="text-xl font-bold text-gray-800 dark:text-white">Courbe de poids</h3>
                                <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Suivi de l'évolution du
                                    poids de {pet.name}</p>
                            </div>

                            <div className="flex items-center gap-6">
                                {currentWeight && (
                                    <div className="flex flex-col items-end justify-center hidden sm:flex">
                                        <span className="text-xs text-gray-400 mb-1 font-medium">Poids actuel</span>
                                        <div className="flex items-center gap-3">
                                            <span
                                                className="text-3xl font-bold text-gray-800 dark:text-white">{currentWeight} kg</span>
                                            {trend && (
                                                <Badge variant="secondary"
                                                       className="bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 hover:bg-emerald-100 border-none px-2 py-1 rounded-lg text-xs">
                                                    <TrendingUp size={12} className="mr-1"/> {trend} kg
                                                </Badge>
                                            )}
                                        </div>
                                    </div>
                                )}
                                <AddWeightModal petId={pet.id} onSuccess={fetchPet}/>
                            </div>
                        </div>

                        <div className="h-[350px] w-full mb-10">
                            {chartData.length > 0 ? (
                                <ResponsiveContainer width="100%" height="100%">
                                    <AreaChart data={chartData} margin={{top: 10, right: 10, left: -20, bottom: 0}}>
                                        <defs>
                                            <linearGradient id="colorWeight" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="5%" stopColor="#f43f5e" stopOpacity={0.3}/>
                                                <stop offset="95%" stopColor="#f43f5e" stopOpacity={0}/>
                                            </linearGradient>
                                        </defs>
                                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)"/>
                                        <XAxis
                                            dataKey="date"
                                            axisLine={false}
                                            tickLine={false}
                                            tick={{fill: '#9ca3af', fontSize: 12}}
                                            dy={10}
                                        />
                                        <YAxis
                                            axisLine={false}
                                            tickLine={false}
                                            tick={{fill: '#9ca3af', fontSize: 12}}
                                            unit=" kg"
                                            domain={['dataMin - 1', 'dataMax + 1']}
                                        />
                                        <Tooltip
                                            contentStyle={{
                                                backgroundColor: 'var(--card)',
                                                borderRadius: '12px',
                                                border: '1px solid var(--border)',
                                                boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)'
                                            }}
                                            labelStyle={{
                                                color: 'var(--foreground)',
                                                fontWeight: 'bold',
                                                marginBottom: '4px'
                                            }}
                                            formatter={(value) => [`${value} kg`, "Poids"]}
                                        />
                                        <Area
                                            type="monotone"
                                            dataKey="poids"
                                            stroke="#f43f5e"
                                            strokeWidth={3}
                                            fillOpacity={1}
                                            fill="url(#colorWeight)"
                                            activeDot={{r: 6, strokeWidth: 0, fill: "#f43f5e"}}
                                        />
                                    </AreaChart>
                                </ResponsiveContainer>
                            ) : (
                                <div
                                    className="h-full flex items-center justify-center text-gray-400 border-2 border-dashed rounded-xl dark:border-slate-800 bg-gray-50/50 dark:bg-slate-900/50">
                                    Ajoutez une première pesée pour voir le graphique
                                </div>
                            )}
                        </div>

                        {historyWeights.length > 0 && (
                            <div className="pt-6 border-t border-gray-100 dark:border-slate-800">
                                <h4 className="font-bold text-gray-800 dark:text-white mb-6">Historique des pesées</h4>
                                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                                    {historyWeights.map((w) => (
                                        <div key={w.id}
                                             className="flex justify-between items-center p-4 bg-gray-50 dark:bg-slate-950 rounded-2xl hover:bg-rose-50 dark:hover:bg-rose-900/10 transition-colors">
                                <span className="text-sm text-gray-500 dark:text-gray-400 capitalize">
                                    {format(parseISO(w.date), 'MMM yyyy', {locale: fr})}
                                </span>
                                            <span className="font-bold text-gray-800 dark:text-white">
                                    {w.weight} <span className="text-xs font-normal text-gray-400">kg</span>
                                </span>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </Card>
                </TabsContent>
            </Tabs>
        </div>
    );
}

const Row = ({label, value}: { label: string, value: string | undefined }) => (
    <div className="flex justify-between border-b border-gray-50 dark:border-slate-800 py-2 last:border-0">
        <span className="text-gray-500 dark:text-gray-400">{label}</span>
        <span className="font-medium text-gray-800 dark:text-gray-200">{value || "Non renseigné"}</span>
    </div>
);