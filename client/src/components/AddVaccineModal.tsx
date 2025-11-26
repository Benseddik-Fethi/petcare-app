import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"; // Nouveau
import { Calendar } from "@/components/ui/calendar"; // Nouveau
import { api } from "@/lib/api";
import { Plus, Loader2, Syringe, FileSignature, Calendar as CalendarIcon } from "lucide-react";
import { format } from "date-fns";
import { fr } from "date-fns/locale"; // Pour le français
import { cn } from "@/lib/utils";

interface AddVaccineModalProps {
    petId: string;
    onSuccess: () => void;
}

export function AddVaccineModal({ petId, onSuccess }: AddVaccineModalProps) {
    const [open, setOpen] = useState(false);
    const [loading, setLoading] = useState(false);

    // États séparés pour gérer les objets Date proprement
    const [name, setName] = useState("");
    const [date, setDate] = useState<Date | undefined>(new Date());
    const [nextDate, setNextDate] = useState<Date | undefined>(undefined);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!date) return; // Sécurité

        setLoading(true);
        try {
            await api.post(`/pets/${petId}/vaccines`, {
                name,
                // Conversion Date -> String ISO pour le backend
                date: date.toISOString(),
                nextDate: nextDate ? nextDate.toISOString() : undefined
            });

            setOpen(false);
            // Reset form
            setName("");
            setNextDate(undefined);
            onSuccess();
        } catch (e) { console.error(e); }
        finally { setLoading(false); }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button variant="outline" className="border-rose-200 text-rose-600 hover:bg-rose-50 hover:border-rose-300 dark:border-rose-800 dark:text-rose-400 dark:hover:bg-rose-900/20 rounded-xl shadow-sm transition-all">
                    <Plus size={16} className="mr-2" /> Ajouter
                </Button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-[400px] bg-white dark:bg-slate-900 border-none shadow-2xl rounded-[32px] p-0 overflow-hidden">

                {/* Header */}
                <div className="bg-gradient-to-r from-rose-50 to-purple-50 dark:from-rose-950/30 dark:to-purple-950/30 p-6 pb-8">
                    <DialogHeader>
                        <DialogTitle className="flex flex-col items-center gap-3 text-center">
                            <div className="w-14 h-14 bg-white dark:bg-slate-800 rounded-2xl flex items-center justify-center shadow-sm text-rose-500">
                                <Syringe size={28} />
                            </div>
                            <div className="space-y-1">
                                <h2 className="text-xl font-bold text-gray-800 dark:text-white">Nouveau vaccin</h2>
                                <p className="text-sm text-gray-500 dark:text-gray-400 font-normal">Enregistrez un rappel ou une injection</p>
                            </div>
                        </DialogTitle>
                    </DialogHeader>
                </div>

                <form onSubmit={handleSubmit} className="p-6 pt-2 space-y-5">

                    {/* Nom */}
                    <div className="space-y-2">
                        <Label className="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">Vaccin</Label>
                        <div className="relative">
                            <div className="absolute left-4 top-1/2 -translate-y-1/2 text-rose-400">
                                <FileSignature size={20} />
                            </div>
                            <Input
                                placeholder="ex: Rage, CHPLR"
                                value={name} onChange={(e) => setName(e.target.value)}
                                required
                                className="h-12 pl-12 bg-gray-50 dark:bg-slate-950 border-transparent focus:border-rose-200 focus:bg-white dark:focus:bg-slate-900 rounded-2xl text-gray-700 dark:text-white font-medium shadow-sm transition-all"
                            />
                        </div>
                    </div>

                    {/* Dates Grid */}
                    <div className="grid grid-cols-2 gap-4">

                        {/* Date Injection (Custom Date Picker) */}
                        <div className="space-y-2 flex flex-col">
                            <Label className="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">Fait le</Label>
                            <Popover>
                                <PopoverTrigger asChild>
                                    <Button
                                        variant={"outline"}
                                        className={cn(
                                            "h-12 rounded-2xl border-transparent bg-gray-50 dark:bg-slate-950 text-left font-medium shadow-sm hover:bg-gray-100 dark:hover:bg-slate-900",
                                            !date && "text-muted-foreground"
                                        )}
                                    >
                                        <CalendarIcon className="mr-2 h-4 w-4 text-rose-400" />
                                        {date ? format(date, "P", { locale: fr }) : <span>Choisir...</span>}
                                    </Button>
                                </PopoverTrigger>
                                <PopoverContent className="w-auto p-0 rounded-2xl border-none shadow-xl" align="start">
                                    <Calendar
                                        mode="single"
                                        selected={date}
                                        onSelect={setDate}
                                        initialFocus
                                        locale={fr}
                                        className="p-3"
                                    />
                                </PopoverContent>
                            </Popover>
                        </div>

                        {/* Date Rappel (Custom Date Picker) */}
                        <div className="space-y-2 flex flex-col">
                            <Label className="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">Rappel</Label>
                            <Popover>
                                <PopoverTrigger asChild>
                                    <Button
                                        variant={"outline"}
                                        className={cn(
                                            "h-12 rounded-2xl border-transparent bg-rose-50/50 dark:bg-rose-950/30 text-rose-700 dark:text-rose-300 text-left font-medium shadow-sm hover:bg-rose-100/50 dark:hover:bg-rose-900/50",
                                            !nextDate && "text-rose-400/70"
                                        )}
                                    >
                                        <CalendarIcon className="mr-2 h-4 w-4" />
                                        {nextDate ? format(nextDate, "P", { locale: fr }) : <span>(Optionnel)</span>}
                                    </Button>
                                </PopoverTrigger>
                                <PopoverContent className="w-auto p-0 rounded-2xl border-none shadow-xl" align="start">
                                    <Calendar
                                        mode="single"
                                        selected={nextDate}
                                        onSelect={setNextDate}
                                        initialFocus
                                        locale={fr}
                                        disabled={(date) => date < new Date()} // Impossible de mettre un rappel dans le passé
                                        className="p-3"
                                    />
                                </PopoverContent>
                            </Popover>
                        </div>
                    </div>

                    <Button type="submit" className="w-full h-12 bg-gradient-to-r from-rose-400 to-purple-500 hover:from-rose-500 hover:to-purple-600 text-white rounded-xl font-bold shadow-lg shadow-rose-100 dark:shadow-none mt-2" disabled={loading}>
                        {loading ? <Loader2 className="animate-spin" /> : "Enregistrer le vaccin"}
                    </Button>
                </form>
            </DialogContent>
        </Dialog>
    );
}