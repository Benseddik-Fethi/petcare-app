import {prisma} from "../config/prisma";

export class PetRepository {
    async findAllByOwner(ownerId: string) {
        return prisma.pet.findMany({
            where: {ownerId},
            orderBy: {createdAt: 'desc'}
        });
    }

    async create(data: any) {
        return prisma.pet.create({data});
    }

    async findByIdWithDetails(id: string) {
        return prisma.pet.findUnique({
            where: {id},
            include: {
                appointments: {
                    include: {vet: true},
                    orderBy: {date: 'desc'}
                },
                vaccines: {
                    orderBy: {nextDate: 'asc'}
                },
                weights: {
                    orderBy: {date: 'asc'}
                }
            }
        });
    }

    async findOwnerById(id: string) {
        return prisma.pet.findUnique({
            where: {id},
            select: {id: true, ownerId: true}
        });
    }

    async addWeight(petId: string, weight: number, date: Date) {
        return prisma.weightLog.create({
            data: {petId, weight, date}
        });
    }

    async addVaccine(petId: string, name: string, date: Date, nextDate?: Date) {
        return prisma.vaccine.create({
            data: {petId, name, date, nextDate}
        });
    }
}
