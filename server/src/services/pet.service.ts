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

    async createPet(userId: string, data: { name: string; species: string; breed?: string; weight?: string }) {
        if (!data.name || !data.species) {
            throw new AppError("Le nom et l'espèce sont obligatoires", 400);
        }

        // On lie l'animal à l'utilisateur connecté
        return this.petRepo.create({
            ...data,
            owner: { connect: { id: userId } },
            avatar: data.species === 'Chien' ? '🐕' : '🐈'
        });
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