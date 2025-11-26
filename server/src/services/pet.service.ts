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

    async createPet(userId: string, data: { name: string; species: string; breed?: string; weight?: string | number; birthDate?: string; gender?: string; color?: string; microchip?: string }) {
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
        if (weight !== undefined) {
            const weightValue = typeof weight === 'number' ? weight : parseFloat(weight);
            if (!isNaN(weightValue) && weightValue > 0) {
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

        // Validation du poids
        if (weight <= 0 || isNaN(weight)) {
            throw new AppError("Le poids doit être un nombre positif valide", 400);
        }

        // Validation de la date
        const weightDate = new Date(date);
        if (isNaN(weightDate.getTime())) {
            throw new AppError("La date de pesée est invalide", 400);
        }

        return this.petRepo.addWeight(petId, weight, weightDate);
    }

    async addVaccine(userId: string, petId: string, data: { name: string, date: string, nextDate?: string }) {
        await this.getPetDetails(petId, userId);

        // Validation des données obligatoires
        if (!data.name || !data.date) {
            throw new AppError("Le nom du vaccin et la date sont obligatoires", 400);
        }

        // Validation de la date du vaccin
        const vaccineDate = new Date(data.date);
        if (isNaN(vaccineDate.getTime())) {
            throw new AppError("La date du vaccin est invalide", 400);
        }

        // Validation de la date de rappel (si fournie)
        let nextVaccineDate: Date | undefined;
        if (data.nextDate) {
            nextVaccineDate = new Date(data.nextDate);
            if (isNaN(nextVaccineDate.getTime())) {
                throw new AppError("La date de rappel est invalide", 400);
            }
        }

        return this.petRepo.addVaccine(
            petId,
            data.name,
            vaccineDate,
            nextVaccineDate
        );
    }
}