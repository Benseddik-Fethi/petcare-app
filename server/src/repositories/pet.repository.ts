import { PrismaClient, Pet, Prisma } from '@prisma/client';

const prisma = new PrismaClient();

export class PetRepository {
    // Trouver tous les animaux d'un propriétaire
    async findAllByOwner(ownerId: string): Promise<Pet[]> {
        return prisma.pet.findMany({
            where: { ownerId },
            orderBy: { createdAt: 'desc' }
        });
    }

    // Créer un animal
    async create(data: Prisma.PetCreateInput): Promise<Pet> {
        return prisma.pet.create({ data });
    }

    // ✅ C'EST ICI QUE LA MAGIE OPÈRE
    // On récupère un animal AVEC tous ses détails (RDV, Vaccins, Poids)
    async findByIdWithDetails(id: string) {
        return prisma.pet.findUnique({
            where: { id },
            include: {
                // 1. Inclure les RDV
                appointments: {
                    include: { vet: true }, // Avec le nom du véto
                    orderBy: { date: 'desc' }
                },
                // 2. Inclure les Vaccins (C'est ce qui te manquait sûrement !)
                vaccines: {
                    orderBy: { nextDate: 'asc' }
                },
                // 3. Inclure l'historique de poids
                weights: {
                    orderBy: { date: 'asc' }
                }
            }
        });
    }

    // Ajouter une pesée
    async addWeight(petId: string, weight: number, date: Date) {
        return prisma.weightLog.create({
            data: { petId, weight, date }
        });
    }

    // Ajouter un vaccin
    async addVaccine(petId: string, name: string, date: Date, nextDate?: Date) {
        return prisma.vaccine.create({
            data: { petId, name, date, nextDate }
        });
    }
}