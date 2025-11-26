import { useState, useEffect } from "react";
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "@/components/ui/dialog.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Textarea } from "@/components/ui/textarea.tsx";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover.tsx";
import { Calendar } from "@/components/ui/calendar.tsx";
import { api } from "@/lib/api.ts";
import { Check, ChevronRight, ChevronLeft, Calendar as CalendarIcon, Clock, MapPin, Stethoscope, Syringe, Heart, Pill, Bell, Dog, FileText, Plus } from "lucide-react";
import { format } from "date-fns";
import { fr } from "date-fns/locale";
import { cn } from "@/lib/utils.ts";

// ... (Types et configuration inchangés) ...
interface Pet { id: string; name: string; breed?: string; avatar?: string; }
interface Vet { id: string; name: string; specialty: string; clinic: { id: string; name: string; address: string }; }

const appointmentTypes = [
    { id: "vaccination", label: "Vaccination", icon: Syringe, color: "rose" },
    { id: "checkup", label: "Contrôle annuel", icon: Stethoscope, color: "sky" },
    { id: "dental", label: "Soins dentaires", icon: Heart, color: "amber" },
    { id: "surgery", label: "Chirurgie", icon: Pill, color: "purple" },
    { id: "emergency", label: "Urgence", icon: Bell, color: "red" },
    { id: "grooming", label: "Toilettage", icon: Dog, color: "emerald" },
    { id: "other", label: "Autre", icon: FileText, color: "gray" },
];

const getColors = (color: string, isSelected: boolean) => {
    if (isSelected) return "bg-white border-rose-500 shadow-md text-rose-600";
    const maps: Record<string, string> = {
        rose: "bg-rose-50 text-rose-600 border-rose-100",
        sky: "bg-sky-50 text-sky-600 border-sky-100",
        amber: "bg-amber-50 text-amber-600 border-amber-100",
        purple: "bg-purple-50 text-purple-600 border-purple-100",
        red: "bg-red-50 text-red-600 border-red-100",
        emerald: "bg-emerald-50 text-emerald-600 border-emerald-100",
        gray: "bg-gray-50 text-gray-600 border-gray-100",
    };
    return maps[color] || maps.gray;
};

export function NewAppointmentModal() {
    const [isOpen, setIsOpen] = useState(false);
    const [step, setStep] = useState(1);
    const [loading, setLoading] = useState(false);
    const [pets, setPets] = useState<Pet[]>([]);
    const [vets, setVets] = useState<Vet[]>([]);

    // Update: date est maintenant un objet Date | undefined
    const [formData, setFormData] = useState<{
        petId: string;
        type: string;
        date: Date | undefined;
        time: string;
        vetId: string;
        notes: string;
    }>({ petId: "", type: "", date: undefined, time: "", vetId: "", notes: "" });

    // ... (useEffect chargement inchangé) ...
    useEffect(() => {
        if (isOpen) {
            const loadData = async () => {
                try {
                    const [petsRes, clinicsRes] = await Promise.all([
                        api.get('/pets'),
                        api.get('/appointments/clinics')
                    ]);
                    setPets(petsRes.data);
                    const allVets: Vet[] = [];
                    clinicsRes.data.forEach((clinic: any) => {
                        clinic.vets.forEach((v: any) => {
                            allVets.push({
                                ...v,
                                clinic: { id: clinic.id, name: clinic.name, address: clinic.address }
                            });
                        });
                    });
                    setVets(allVets);
                } catch (e) { console.error(e); }
            };
            loadData();
        }
    }, [isOpen]);

    const handleSubmit = async () => {
        if (!formData.date) return;
        setLoading(true);
        try {
            const selectedVet = vets.find(v => v.id === formData.vetId);
            await api.post('/appointments', {
                ...formData,
                date: format(formData.date, 'yyyy-MM-dd'), // Format API
                clinicId: selectedVet?.clinic.id
            });
            setIsOpen(false);
            setStep(1);
            setFormData({ petId: "", type: "", date: undefined, time: "", vetId: "", notes: "" });
            window.location.reload();
        } catch (e) { console.error(e); alert("Erreur"); }
        finally { setLoading(false); }
    };

    return (
        <Dialog open={isOpen} onOpenChange={(val) => { setIsOpen(val); if(!val) setStep(1); }}>
            <DialogTrigger asChild>
                <Button className="bg-gradient-to-r from-rose-400 to-pink-400 text-white hover:from-rose-500 hover:to-pink-500 rounded-xl shadow-sm transition-all hover:shadow-md">
                    <Plus size={20} className="mr-2" /> Nouveau RDV
                </Button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-2xl p-0 gap-0 bg-white/95 backdrop-blur-xl border-white/20 rounded-3xl shadow-2xl overflow-hidden">

                {/* ... (Header inchangé) ... */}
                <div className="px-6 py-4 border-b border-rose-100 bg-rose-50/30 flex items-center justify-between">
                    <DialogTitle className="text-xl font-bold text-gray-800">Nouveau rendez-vous</DialogTitle>
                    <div className="flex items-center gap-2">
                        {[1, 2, 3].map((s) => (
                            <div key={s} className="flex items-center">
                                <div className={cn(
                                    "w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm transition-all",
                                    step >= s ? "bg-gradient-to-r from-rose-400 to-pink-400 text-white shadow-sm" : "bg-gray-100 text-gray-400"
                                )}>
                                    {step > s ? <Check size={14} /> : s}
                                </div>
                                {s < 3 && <div className={cn("w-6 h-1 mx-1 rounded-full transition-all", step > s ? "bg-rose-300" : "bg-gray-200")} />}
                            </div>
                        ))}
                    </div>
                </div>

                <div className="p-6 max-h-[60vh] overflow-y-auto">
                    {/* ... (ETAPE 1 inchangée) ... */}
                    {step === 1 && (
                        <div className="space-y-6 animate-in slide-in-from-right-8 fade-in duration-300">
                            {/* ... Copie le contenu de l'étape 1 précédente ici ... */}
                            <div className="space-y-3">
                                <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">1. Choisissez l'animal</h3>
                                <div className="grid grid-cols-2 gap-3">
                                    {pets.map((pet) => (
                                        <button
                                            key={pet.id}
                                            onClick={() => setFormData({...formData, petId: pet.id})}
                                            className={cn(
                                                "flex items-center gap-3 p-3 rounded-2xl border-2 transition-all text-left",
                                                formData.petId === pet.id
                                                    ? "border-rose-400 bg-rose-50 shadow-sm"
                                                    : "border-transparent bg-gray-50 hover:bg-gray-100"
                                            )}
                                        >
                                            <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-xl shadow-sm">
                                                {pet.avatar || "🐾"}
                                            </div>
                                            <div>
                                                <p className="font-bold text-gray-800">{pet.name}</p>
                                                <p className="text-xs text-gray-500">{pet.breed}</p>
                                            </div>
                                            {formData.petId === pet.id && <Check size={16} className="ml-auto text-rose-500"/>}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div className="space-y-3">
                                <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">2. Motif de consultation</h3>
                                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                                    {appointmentTypes.map((type) => {
                                        const isSelected = formData.type === type.label;
                                        return (
                                            <button
                                                key={type.id}
                                                onClick={() => setFormData({...formData, type: type.label})}
                                                className={cn(
                                                    "flex flex-col items-center gap-2 p-3 rounded-2xl border-2 transition-all",
                                                    getColors(type.color, isSelected)
                                                )}
                                            >
                                                <type.icon size={24} />
                                                <span className="text-xs font-medium">{type.label}</span>
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>
                        </div>
                    )}

                    {/* ÉTAPE 2 : DATE & HEURE (MISE À JOUR) */}
                    {step === 2 && (
                        <div className="space-y-6 animate-in slide-in-from-right-8 fade-in duration-300">
                            <div className="text-center mb-4">
                                <h3 className="text-lg font-bold text-gray-800">Quand êtes-vous disponible ?</h3>
                                <p className="text-sm text-gray-500">Sélectionnez une date et un créneau horaire</p>
                            </div>

                            <div className="bg-gray-50 p-6 rounded-3xl border border-gray-100 space-y-4">

                                {/* Date Picker Shadcn */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">Date</label>
                                    <Popover>
                                        <PopoverTrigger asChild>
                                            <Button
                                                variant={"outline"}
                                                className={cn(
                                                    "w-full h-12 pl-4 justify-start text-left font-medium rounded-xl border-gray-200 bg-white shadow-sm hover:bg-gray-50",
                                                    !formData.date && "text-muted-foreground"
                                                )}
                                            >
                                                <CalendarIcon className="mr-2 h-4 w-4 text-rose-400" />
                                                {formData.date ? format(formData.date, "PPP", { locale: fr }) : <span>Choisir une date</span>}
                                            </Button>
                                        </PopoverTrigger>
                                        <PopoverContent className="w-auto p-0 rounded-2xl border-none shadow-xl" align="start">
                                            <Calendar
                                                mode="single"
                                                selected={formData.date}
                                                onSelect={(d) => setFormData({...formData, date: d})}
                                                initialFocus
                                                locale={fr}
                                                disabled={(date) => date < new Date()}
                                                className="p-3"
                                            />
                                        </PopoverContent>
                                    </Popover>
                                </div>

                                {/* Time Picker Stylisé */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">Heure</label>
                                    <div className="relative">
                                        <Clock className="absolute left-3 top-1/2 -translate-y-1/2 text-rose-400" size={18} />
                                        {/* Input natif time mais stylisé comme le bouton du haut */}
                                        <input
                                            type="time"
                                            className="w-full pl-10 pr-4 h-12 rounded-xl border border-gray-200 bg-white shadow-sm outline-none focus:ring-2 focus:ring-rose-200 focus:border-rose-300 transition-all text-gray-700 font-medium"
                                            value={formData.time}
                                            onChange={(e) => setFormData({...formData, time: e.target.value})}
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* ÉTAPE 3 : VÉTÉRINAIRE (inchangée) */}
                    {step === 3 && (
                        <div className="space-y-6 animate-in slide-in-from-right-8 fade-in duration-300">
                            {/* ... Copie le contenu de l'étape 3 précédente ici (Liste vétos + Notes) ... */}
                            <div className="space-y-3">
                                <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Choisissez un praticien</h3>
                                <div className="space-y-2 max-h-48 overflow-y-auto pr-2">
                                    {vets.map((vet) => (
                                        <button
                                            key={vet.id}
                                            onClick={() => setFormData({...formData, vetId: vet.id})}
                                            className={cn(
                                                "w-full flex items-start gap-3 p-3 rounded-2xl border-2 transition-all text-left",
                                                formData.vetId === vet.id
                                                    ? "border-emerald-400 bg-emerald-50 shadow-sm"
                                                    : "border-transparent bg-gray-50 hover:bg-gray-100"
                                            )}
                                        >
                                            <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-emerald-500 shadow-sm">
                                                <Stethoscope size={20} />
                                            </div>
                                            <div className="flex-1">
                                                <p className="font-bold text-gray-800">{vet.name}</p>
                                                <p className="text-xs text-gray-500">{vet.specialty}</p>
                                                <div className="flex items-center gap-1 mt-1 text-xs text-gray-400">
                                                    <MapPin size={10} /> {vet.clinic.name}
                                                </div>
                                            </div>
                                            {formData.vetId === vet.id && <Check size={16} className="text-emerald-600"/>}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Notes pour le vétérinaire</label>
                                <Textarea
                                    placeholder="Symptômes, comportement inhabituel..."
                                    className="bg-gray-50 border-gray-200 rounded-xl focus:border-rose-300 resize-none"
                                    value={formData.notes}
                                    onChange={(e) => setFormData({...formData, notes: e.target.value})}
                                />
                            </div>
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="p-4 bg-gray-50 border-t border-gray-100 flex justify-between items-center">
                    {step > 1 ? (
                        <Button variant="ghost" onClick={() => setStep(step - 1)} className="text-gray-500 hover:text-gray-800 hover:bg-white">
                            <ChevronLeft size={18} className="mr-1" /> Retour
                        </Button>
                    ) : ( <span /> )}

                    {step < 3 ? (
                        <Button
                            className="bg-gray-900 text-white hover:bg-gray-800 rounded-xl px-6"
                            onClick={() => setStep(step + 1)}
                            disabled={
                                (step === 1 && (!formData.petId || !formData.type)) ||
                                (step === 2 && (!formData.date || !formData.time))
                            }
                        >
                            Suivant <ChevronRight size={18} className="ml-1" />
                        </Button>
                    ) : (
                        <Button
                            className="bg-gradient-to-r from-emerald-400 to-teal-500 hover:from-emerald-500 hover:to-teal-600 text-white rounded-xl px-6 shadow-lg shadow-emerald-200"
                            onClick={handleSubmit}
                            disabled={!formData.vetId || loading}
                        >
                            {loading ? "..." : "Confirmer le RDV"}
                        </Button>
                    )}
                </div>
            </DialogContent>
        </Dialog>
    );
}