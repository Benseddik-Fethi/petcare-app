import {useState} from "react";
import {
    Dialog as DialogRoot,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger
} from "@/components/ui/dialog"; // Correction import
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {api} from "@/lib/api";
import {Loader2, Plus} from "lucide-react";

interface Pet {
    id: string;
    name: string;
    species: string;
    breed?: string;
    age?: string;
    weight?: string;
    gender?: string;
    avatar?: string;
    ownerId: string;
    createdAt: string;
    updatedAt: string;
}

interface AddPetModalProps {
    onPetAdded: (newPet: Pet) => void;
}

export function AddPetModal({onPetAdded}: Readonly<AddPetModalProps>) {
    const [open, setOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const [formData, setFormData] = useState({
        name: "",
        species: "Chien",
        breed: "",
        weight: "",
        age: ""
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const {data} = await api.post('/pets', formData);
            onPetAdded(data);
            setOpen(false);
            setFormData({name: "", species: "Chien", breed: "", weight: "", age: ""});
        } catch (error) {
            console.error(error);
            alert("Erreur lors de la création");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <DialogRoot open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button
                    className="bg-gradient-to-r from-rose-400 to-pink-400 text-white rounded-xl shadow-md hover:shadow-lg transition-all">
                    <Plus size={18} className="mr-2"/>
                    Ajouter un animal
                </Button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>Ajouter un compagnon 🐾</DialogTitle>
                </DialogHeader>

                <form onSubmit={handleSubmit} className="grid gap-4 py-4">

                    {/* Nom */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="name" className="text-right">Nom *</Label>
                        <Input
                            id="name"
                            value={formData.name}
                            onChange={(e) => setFormData({...formData, name: e.target.value})}
                            className="col-span-3"
                            required
                        />
                    </div>

                    {/* Espèce */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="species" className="text-right">Espèce</Label>
                        <div className="col-span-3">
                            <Select
                                value={formData.species}
                                onValueChange={(val) => setFormData({...formData, species: val})}
                            >
                                <SelectTrigger>
                                    <SelectValue placeholder="Choisir..."/>
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="Chien">🐕 Chien</SelectItem>
                                    <SelectItem value="Chat">🐈 Chat</SelectItem>
                                    <SelectItem value="Lapin">🐇 Lapin</SelectItem>
                                    <SelectItem value="Autre">🦜 Autre</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                    </div>

                    {/* Race */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="breed" className="text-right">Race</Label>
                        <Input
                            id="breed"
                            value={formData.breed}
                            onChange={(e) => setFormData({...formData, breed: e.target.value})}
                            className="col-span-3"
                            placeholder="Ex: Labrador"
                        />
                    </div>

                    {/* Poids */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="weight" className="text-right">Poids</Label>
                        <Input
                            id="weight"
                            value={formData.weight}
                            onChange={(e) => setFormData({...formData, weight: e.target.value})}
                            className="col-span-3"
                            placeholder="Ex: 15 kg"
                        />
                    </div>

                    <DialogFooter>
                        <Button type="submit" disabled={isLoading}>
                            {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin"/>}
                            Enregistrer
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </DialogRoot>
    );
}