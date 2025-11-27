import {prisma} from "../config/prisma";

export class AppointmentRepository {

    async findAllClinics() {
        return prisma.clinic.findMany({
            include: {vets: true}
        });
    }

    async create(data: any) {
        return prisma.appointment.create({data});
    }

    async findAllByUser(userId: string) {
        return prisma.appointment.findMany({
            where: {userId},
            include: {
                pet: true,
                vet: {include: {clinic: true}}
            },
            orderBy: {date: 'asc'}
        });
    }

    async findVetById(id: string) {
        return prisma.vet.findUnique({where: {id}});
    }
}
