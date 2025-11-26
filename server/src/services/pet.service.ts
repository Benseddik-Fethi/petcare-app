import { PetRepository } from '../repositories/pet.repository';
import { AppError } from '../utils/AppError';

export class PetService {
    private readonly petRepo: PetRepository;

    constructor() {
        this.petRepo = new PetRepository();
    }

    async getUserPets(userId: string) {
        return this.petRepo.findAllByOwner(userId);
    }

    async createPet(userId: string, data: { name: string; species: string; breed?: string; weight?: string; birthDate?: string; gender?: string; color?: string; microchip?: string }) {
        if (!data.name || !data.species) {
            throw new AppError("Le nom et l'espèce sont obligatoires", 400);
        }

        // Extraire le poids car il n'est pas un champ direct du modèle Pet
        const { weight, ...petData } = data;

        // Créer l'animal avec les champs valides uniquement
        const pet = await this.petRepo.create({
            name: petData.name,
            species: petData.species,
            breed: petData.breed,
            birthDate: petData.birthDate ? new Date(petData.birthDate) : undefined,
            gender: petData.gender,
            color: petData.color,
            microchip: petData.microchip,
            avatar: data.species === 'Chien' ? '🐕' : '🐈',
            owner: { connect: { id: userId } }
        });

        // Si un poids est fourni, créer une entrée initiale dans WeightLog
        if (weight) {
            const weightValue = parseFloat(weight);
            if (!isNaN(weightValue)) {
                await this.petRepo.addWeight(pet.id, weightValue, new Date());
            }
        }

        return pet;
    }
    async getPetDetails(petId: string, userId: string) {
        const pet = await this.petRepo.findByIdWithDetails(petId);

        if (!pet) throw new AppError('Animal introuvable', 404);

        if (pet.ownerId !== userId) {
            throw new AppError('Accès interdit', 403);
        }

        return pet;
    }

    async addWeight(userId: string, petId: string, weight: number, date: string) {
        // Vérif propriété
        await this.getPetDetails(petId, userId); // Réutilise la vérif d'accès existante

        return this.petRepo.addWeight(petId, weight, new Date(date));
    }

    async addVaccine(userId: string, petId: string, data: { name: string, date: string, nextDate: string }) {
        await this.getPetDetails(petId, userId);

        return this.petRepo.addVaccine(
            petId,
            data.name,
            new Date(data.date),
            new Date(data.nextDate)
        );
    }
}