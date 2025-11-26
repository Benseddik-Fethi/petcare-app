import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { api } from "@/lib/api";
import { Plus, Loader2, TrendingUp, Scale, Calendar as CalendarIcon } from "lucide-react";
import { format } from "date-fns";
import { fr } from "date-fns/locale";
import { cn } from "@/lib/utils";

interface AddWeightModalProps {
    petId: string;
    onSuccess: () => void;
}

export function AddWeightModal({ petId, onSuccess }: Readonly<AddWeightModalProps>) {
    const [open, setOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    const [weight, setWeight] = useState("");

    // On utilise un objet Date pour le calendrier
    const [date, setDate] = useState<Date | undefined>(new Date());

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!date) return;

        setLoading(true);
        try {
            await api.post(`/pets/${petId}/weights`, {
                weight,
                // On formate la date pour l'API
                date: date.toISOString()
            });
            setOpen(false);
            setWeight("");
            onSuccess();
        } catch (e) { console.error(e); }
        finally { setLoading(false); }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button className="bg-gradient-to-r from-rose-400 to-pink-400 hover:from-rose-500 hover:to-pink-500 text-white rounded-2xl shadow-md hover:shadow-lg hover:-translate-y-0.5 transition-all px-6 font-medium">
                    <Plus size={18} className="mr-2" /> Ajouter pesée
                </Button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-[400px] bg-white dark:bg-slate-900 border-none shadow-2xl rounded-[32px] p-0 overflow-hidden">

                {/* Header */}
                <div className="bg-gradient-to-r from-emerald-50 to-teal-50 dark:from-emerald-950/30 dark:to-teal-950/30 p-6 pb-8">
                    <DialogHeader>
                        <DialogTitle className="flex flex-col items-center gap-3 text-center">
                            <div className="w-14 h-14 bg-white dark:bg-slate-800 rounded-2xl flex items-center justify-center shadow-sm text-emerald-500">
                                <Scale size={28} />
                            </div>
                            <div className="space-y-1">
                                <h2 className="text-xl font-bold text-gray-800 dark:text-white">Nouvelle pesée</h2>
                                <p className="text-sm text-gray-500 dark:text-gray-400 font-normal">Suivez l'évolution de son poids</p>
                            </div>
                        </DialogTitle>
                    </DialogHeader>
                </div>

                <form onSubmit={handleSubmit} className="p-6 pt-2 space-y-5">
                    <div className="space-y-4">

                        {/* Poids */}
                        <div className="space-y-2">
                            <Label className="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">Poids (kg)</Label>
                            <div className="relative">
                                <div className="absolute left-4 top-1/2 -translate-y-1/2 text-emerald-500">
                                    <TrendingUp size={20} />
                                </div>
                                <Input
                                    type="number"
                                    step="0.1"
                                    placeholder="0.00"
                                    value={weight} onChange={(e) => setWeight(e.target.value)}
                                    required
                                    className="h-14 pl-12 bg-gray-50 dark:bg-slate-950 border-transparent focus:border-emerald-200 focus:bg-white dark:focus:bg-slate-900 rounded-2xl text-lg font-bold text-gray-700 dark:text-white transition-all shadow-inner"
                                />
                                <div className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 font-medium text-sm">kg</div>
                            </div>
                        </div>

                        {/* Date Picker */}
                        <div className="space-y-2 flex flex-col">
                            <Label className="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">Date de la pesée</Label>
                            <Popover>
                                <PopoverTrigger asChild>
                                    <Button
                                        variant={"outline"}
                                        className={cn(
                                            "h-12 pl-4 justify-start text-left font-medium rounded-2xl border-transparent bg-gray-50 dark:bg-slate-950 shadow-sm hover:bg-gray-100 dark:hover:bg-slate-900 transition-all",
                                            !date && "text-muted-foreground"
                                        )}
                                    >
                                        <CalendarIcon className="mr-2 h-4 w-4 text-gray-400" />
                                        {date ? <span className="text-gray-700 dark:text-white">{format(date, "P", { locale: fr })}</span> : <span>Choisir...</span>}
                                    </Button>
                                </PopoverTrigger>
                                <PopoverContent className="w-auto p-0 rounded-2xl border-none shadow-xl" align="start">
                                    <Calendar
                                        mode="single"
                                        selected={date}
                                        onSelect={setDate}
                                        autoFocus
                                        locale={fr}
                                        disabled={(date) => date > new Date()} // Pas de pesée dans le futur
                                        className="p-3"
                                    />
                                </PopoverContent>
                            </Popover>
                        </div>
                    </div>

                    <Button type="submit" className="w-full h-12 bg-gradient-to-r from-emerald-400 to-teal-500 hover:from-emerald-500 hover:to-teal-600 text-white rounded-xl font-bold shadow-lg shadow-emerald-100 dark:shadow-none mt-2" disabled={loading}>
                        {loading ? <Loader2 className="animate-spin" /> : "Enregistrer"}
                    </Button>
                </form>
            </DialogContent>
        </Dialog>
    );
}