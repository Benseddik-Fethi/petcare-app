import { useState, useEffect } from "react";
import { api } from "@/lib/api";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon, Clock, MapPin, User, Dog } from "lucide-react";
import { format, addMonths, subMonths, startOfMonth, endOfMonth, startOfWeek, endOfWeek, eachDayOfInterval, isSameMonth, isSameDay, isToday } from "date-fns";
import { fr } from "date-fns/locale";
import { cn } from "@/lib/utils";
import {NewAppointmentModal} from "@/components/NewAppointmentModal.tsx";

// --- TYPES ---
interface Appointment {
    id: string;
    date: string;
    type: string;
    status: string;
    pet: { name: string; avatar?: string };
    vet: { name: string; clinic: { name: string } };
}

// --- HELPER COULEURS (Compatible Dark Mode) ---
const getTypeColor = (type: string) => {
    const t = type.toLowerCase();
    // On définit les styles pour Light Mode ET Dark Mode
    if (t.includes('vaccin')) return "bg-rose-100 dark:bg-rose-950/50 text-rose-600 dark:text-rose-400 border-rose-200 dark:border-rose-900";
    if (t.includes('contrôle')) return "bg-sky-100 dark:bg-sky-950/50 text-sky-600 dark:text-sky-400 border-sky-200 dark:border-sky-900";
    if (t.includes('dent')) return "bg-amber-100 dark:bg-amber-950/50 text-amber-600 dark:text-amber-400 border-amber-200 dark:border-amber-900";
    if (t.includes('urg')) return "bg-red-100 dark:bg-red-950/50 text-red-600 dark:text-red-400 border-red-200 dark:border-red-900";

    // Défaut (Emerald)
    return "bg-emerald-100 dark:bg-emerald-950/50 text-emerald-600 dark:text-emerald-400 border-emerald-200 dark:border-emerald-900";
};

export default function CalendarPage() {
    const [currentMonth, setCurrentMonth] = useState(new Date());
    const [selectedDate, setSelectedDate] = useState<Date>(new Date());
    const [appointments, setAppointments] = useState<Appointment[]>([]);
    const [loading, setLoading] = useState(true);

    // 1. Chargement des données
    useEffect(() => {
        const fetchAppointments = async () => {
            try {
                const { data } = await api.get<Appointment[]>('/appointments');
                setAppointments(data);
            } catch (e) {
                console.error(e);
            } finally {
                setLoading(false);
            }
        };
        fetchAppointments();
    }, []);

    // 2. Calcul de la grille
    const daysInGrid = eachDayOfInterval({
        start: startOfWeek(startOfMonth(currentMonth), { weekStartsOn: 1 }), // Lundi
        end: endOfWeek(endOfMonth(currentMonth), { weekStartsOn: 1 })
    });

    // 3. Filtres
    const selectedDayAppointments = appointments.filter(apt =>
        isSameDay(new Date(apt.date), selectedDate)
    );

    const upcomingAppointments = appointments
        .filter(apt => new Date(apt.date) >= new Date() && !isSameDay(new Date(apt.date), selectedDate))
        .slice(0, 3);

    // 4. Helper d'affichage extrait
    const renderAppointmentsList = () => {
        if (loading) {
            return <div className="text-center py-8 text-gray-400 dark:text-gray-600">Chargement...</div>;
        }

        if (selectedDayAppointments.length === 0) {
            return (
                <div className="text-center py-10 bg-gray-50 dark:bg-slate-950 rounded-2xl border-2 border-dashed border-gray-100 dark:border-slate-800">
                    <Clock size={32} className="mx-auto mb-2 text-gray-300 dark:text-gray-600" />
                    <p className="text-sm text-gray-500 dark:text-gray-400">Aucun rendez-vous ce jour</p>
                    <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">Profitez-en pour vous reposer !</p>
                </div>
            );
        }

        return (
            <div className="space-y-3">
                {selectedDayAppointments.map((apt) => (
                    <div key={apt.id} className="p-4 rounded-2xl bg-gray-50 dark:bg-slate-950 border border-gray-100 dark:border-slate-800 hover:bg-rose-50 dark:hover:bg-slate-900 hover:border-rose-100 dark:hover:border-slate-700 transition-all group">
                        <div className="flex justify-between items-start mb-2">
              <span className={cn("text-xs font-bold px-2 py-1 rounded-md border", getTypeColor(apt.type))}>
                {apt.type}
              </span>
                            <span className="text-sm font-bold text-gray-700 dark:text-gray-200">{format(new Date(apt.date), 'HH:mm')}</span>
                        </div>
                        <div className="space-y-1">
                            <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                                <Dog size={14} className="text-rose-400"/>
                                <span className="font-medium text-gray-700 dark:text-gray-300">{apt.pet.name}</span>
                            </div>
                            <div className="flex items-center gap-2 text-xs text-gray-500 dark:text-gray-500">
                                <User size={14}/> {apt.vet.name}
                            </div>
                            <div className="flex items-center gap-2 text-xs text-gray-400 dark:text-gray-600">
                                <MapPin size={14}/> {apt.vet.clinic.name}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div className="space-y-8 pb-10">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-gray-800 dark:text-white">Calendrier</h1>
                    <p className="text-gray-500 dark:text-gray-400 mt-1">Gérez les rendez-vous de vos animaux</p>
                </div>
                <NewAppointmentModal />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

                {/* CALENDRIER */}
                <div className="lg:col-span-2">
                    <Card className="p-6 border-white dark:border-slate-800 shadow-sm bg-white/80 dark:bg-slate-900/80 backdrop-blur-sm rounded-3xl">

                        {/* Navigation Mois */}
                        <div className="flex items-center justify-between mb-8">
                            <Button variant="ghost" onClick={() => setCurrentMonth(subMonths(currentMonth, 1))} className="hover:bg-rose-50 dark:hover:bg-slate-800 text-gray-600 dark:text-gray-300">
                                <ChevronLeft size={24} />
                            </Button>
                            <h2 className="text-2xl font-bold text-gray-800 dark:text-white capitalize">
                                {format(currentMonth, 'MMMM yyyy', { locale: fr })}
                            </h2>
                            <Button variant="ghost" onClick={() => setCurrentMonth(addMonths(currentMonth, 1))} className="hover:bg-rose-50 dark:hover:bg-slate-800 text-gray-600 dark:text-gray-300">
                                <ChevronRight size={24} />
                            </Button>
                        </div>

                        {/* Jours Semaine */}
                        <div className="grid grid-cols-7 mb-4 text-center">
                            {['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'].map(day => (
                                <div key={day} className="text-sm font-semibold text-gray-400 dark:text-gray-500 uppercase tracking-wide">
                                    {day}
                                </div>
                            ))}
                        </div>

                        {/* Grille Jours */}
                        <div className="grid grid-cols-7 gap-3">
                            {daysInGrid.map((day) => {
                                const dayAppts = appointments.filter(apt => isSameDay(new Date(apt.date), day));
                                const isSelected = isSameDay(day, selectedDate);
                                const isCurrentMonth = isSameMonth(day, currentMonth);
                                const isTodayDate = isToday(day);

                                return (
                                    <button
                                        key={day.toISOString()}
                                        type="button"
                                        onClick={() => setSelectedDate(day)}
                                        className={cn(
                                            "min-h-[100px] p-2 rounded-2xl transition-all border-2 relative group flex flex-col gap-1 w-full text-left align-top focus:outline-none focus:ring-2 focus:ring-rose-400 focus:ring-offset-2 dark:focus:ring-offset-slate-900",
                                            !isCurrentMonth
                                                ? "bg-gray-50/50 dark:bg-slate-900/30 border-transparent opacity-50"
                                                : "bg-white dark:bg-slate-950 border-gray-100 dark:border-slate-800 hover:border-rose-200 dark:hover:border-rose-900 hover:shadow-md dark:hover:shadow-none",
                                            isSelected && "border-rose-400 dark:border-rose-500 ring-2 ring-rose-100 dark:ring-rose-900 bg-rose-50/30 dark:bg-rose-900/20 z-10",
                                            isTodayDate && !isSelected && "bg-rose-50/20 dark:bg-rose-900/10 border-rose-200 dark:border-rose-800"
                                        )}
                                    >
                    <span className={cn(
                        "text-sm font-bold w-7 h-7 flex items-center justify-center rounded-full mb-1",
                        isSelected
                            ? "bg-rose-500 text-white"
                            : (isTodayDate ? "text-rose-500 bg-rose-100 dark:bg-rose-900/50" : "text-gray-700 dark:text-gray-300")
                    )}>
                      {format(day, 'd')}
                    </span>

                                        {/* Pastilles RDV */}
                                        {dayAppts.slice(0, 2).map((apt) => (
                                            <div key={apt.id} className={cn("text-[10px] font-medium px-2 py-1 rounded-lg truncate border w-full text-left", getTypeColor(apt.type))}>
                                                <span className="mr-1">{format(new Date(apt.date), 'HH:mm')}</span>
                                                {apt.pet.name}
                                            </div>
                                        ))}
                                        {dayAppts.length > 2 && (
                                            <div className="text-[10px] text-gray-400 dark:text-gray-500 pl-1">+{dayAppts.length - 2} autre(s)</div>
                                        )}
                                    </button>
                                );
                            })}
                        </div>
                    </Card>
                </div>

                {/* SIDEBAR DÉTAILS */}
                <div className="space-y-6">

                    {/* Détails du jour */}
                    <Card className="p-6 border-white dark:border-slate-800 shadow-sm bg-white dark:bg-slate-900 rounded-3xl h-fit">
                        <h3 className="text-lg font-bold text-gray-800 dark:text-white mb-4 flex items-center gap-2">
                            <CalendarIcon className="text-rose-500" size={20} />
                            {format(selectedDate, 'd MMMM yyyy', { locale: fr })}
                        </h3>

                        {renderAppointmentsList()}

                    </Card>

                    {/* Prochains RDV */}
                    {upcomingAppointments.length > 0 && (
                        <div className="pt-4">
                            <h4 className="text-sm font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-3">À venir bientôt</h4>
                            <div className="space-y-3">
                                {upcomingAppointments.map((apt) => (
                                    <div key={apt.id} className="flex items-center gap-3 p-3 rounded-2xl bg-white/60 dark:bg-slate-900/60 border border-white dark:border-slate-800 shadow-sm hover:shadow-md dark:hover:shadow-none transition-all cursor-pointer">
                                        <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-rose-100 to-pink-50 dark:from-rose-950 dark:to-rose-900 flex flex-col items-center justify-center text-rose-600 dark:text-rose-400 shrink-0">
                                            <span className="text-xs font-bold">{format(new Date(apt.date), 'dd')}</span>
                                            <span className="text-[9px] uppercase">{format(new Date(apt.date), 'MMM', { locale: fr })}</span>
                                        </div>
                                        <div className="min-w-0">
                                            <p className="font-bold text-gray-800 dark:text-gray-200 text-sm truncate">{apt.type}</p>
                                            <p className="text-xs text-gray-500 dark:text-gray-400 truncate">{apt.pet.name} • {format(new Date(apt.date), 'HH:mm')}</p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}