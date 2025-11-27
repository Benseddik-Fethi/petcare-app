import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {api} from "@/lib/api";
import {Card} from "@/components/ui/card";
import {Input} from "@/components/ui/input";
import {AddPetModal} from "@/components/AddPetModal";
import {ChevronRight, Dog, Search} from "lucide-react";

// --- TYPES ---
interface Pet {
    id: string;
    name: string;
    species: string;
    breed?: string;
    weight?: string;
    avatar?: string;
}

export default function PetsPage() {
    const navigate = useNavigate();
    const [pets, setPets] = useState<Pet[]>([]);
    const [search, setSearch] = useState("");
    const [loading, setLoading] = useState(true);

    // Chargement
    useEffect(() => {
        const fetchPets = async () => {
            try {
                const {data} = await api.get<Pet[]>('/pets');
                setPets(data);
            } catch (e) {
                console.error(e);
            } finally {
                setLoading(false);
            }
        };
        fetchPets();
    }, []);

    // Ajout dynamique (évite le rechargement)
    const handleNewPet = (newPet: Pet) => {
        setPets([newPet, ...pets]);
    };

    // Filtrage
    const filteredPets = pets.filter(p =>
        p.name.toLowerCase().includes(search.toLowerCase()) ||
        p.breed?.toLowerCase().includes(search.toLowerCase())
    );

    const getSpeciesIcon = (species: string) => {
        const s = species.toLowerCase();
        if (s.includes('chat')) return "🐈";
        if (s.includes('lapin')) return "🐇";
        return "🐕";
    };

    return (
        <div className="space-y-8 pb-10">

            {/* Header & Actions */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold text-gray-800 dark:text-white">Mes Animaux</h1>
                    <p className="text-gray-500 dark:text-gray-400 mt-1">Gérez les profils de tous vos compagnons</p>
                </div>
                <div className="flex items-center gap-3">
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 h-4 w-4"/>
                        <Input
                            placeholder="Rechercher..."
                            className="pl-9 w-full md:w-64 bg-white dark:bg-slate-900 border-gray-200 dark:border-slate-800 rounded-xl"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                        />
                    </div>
                    <AddPetModal onPetAdded={handleNewPet}/>
                </div>
            </div>

            {/* Contenu Grille */}
            {loading ? (
                <div className="text-center py-20 text-gray-400">Chargement de la meute...</div>
            ) : filteredPets.length === 0 ? (
                <div
                    className="flex flex-col items-center justify-center py-20 bg-white/50 dark:bg-slate-900/50 border-2 border-dashed border-gray-200 dark:border-slate-800 rounded-3xl">
                    <div
                        className="w-16 h-16 bg-gray-100 dark:bg-slate-800 rounded-full flex items-center justify-center mb-4">
                        <Dog size={32} className="text-gray-400"/>
                    </div>
                    <h3 className="text-lg font-semibold text-gray-700 dark:text-gray-300">Aucun animal trouvé</h3>
                    <p className="text-gray-500 text-sm mt-1 mb-6">
                        {search ? "Aucun résultat pour votre recherche." : "Commencez par ajouter votre premier compagnon !"}
                    </p>
                    {!search && <AddPetModal onPetAdded={handleNewPet}/>}
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {filteredPets.map((pet) => (
                        <Card
                            key={pet.id}
                            onClick={() => navigate(`/pets/${pet.id}`)}
                            className="group relative overflow-hidden p-6 cursor-pointer hover:shadow-xl hover:shadow-rose-100/50 dark:hover:shadow-none hover:-translate-y-1 transition-all duration-300 bg-white dark:bg-slate-900 border-white dark:border-slate-800 shadow-sm rounded-3xl"
                        >
                            <div className="flex items-start gap-5">
                                <div
                                    className="w-20 h-20 bg-gradient-to-br from-amber-100 to-rose-100 dark:from-slate-800 dark:to-slate-700 rounded-2xl flex items-center justify-center text-4xl shadow-inner group-hover:scale-110 transition-transform duration-300">
                                    {pet.avatar || getSpeciesIcon(pet.species)}
                                </div>
                                <div className="flex-1 min-w-0 pt-1">
                                    <h3 className="text-xl font-bold text-gray-800 dark:text-white truncate mb-1">{pet.name}</h3>
                                    <p className="text-sm text-gray-500 dark:text-gray-400 truncate flex items-center gap-1">
                                        {pet.breed || "Race inconnue"}
                                    </p>

                                    <div className="flex flex-wrap gap-2 mt-3">
                    <span
                        className="px-2.5 py-1 bg-sky-50 dark:bg-sky-900/30 text-sky-600 dark:text-sky-400 rounded-lg text-xs font-semibold border border-sky-100 dark:border-sky-800">
                        {pet.species}
                    </span>
                                        {pet.weight && (
                                            <span
                                                className="px-2.5 py-1 bg-emerald-50 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 rounded-lg text-xs font-semibold border border-emerald-100 dark:border-emerald-800">
                        {pet.weight}
                        </span>
                                        )}
                                    </div>
                                </div>
                                <div
                                    className="absolute top-6 right-6 opacity-0 group-hover:opacity-100 transition-opacity -translate-x-2 group-hover:translate-x-0 duration-300">
                                    <ChevronRight className="text-rose-400"/>
                                </div>
                            </div>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}