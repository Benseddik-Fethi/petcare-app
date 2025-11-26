import { PrismaClient, Appointment, Prisma, Clinic } from '@prisma/client';

const prisma = new PrismaClient();

export class AppointmentRepository {

    async findAllClinics(): Promise<Clinic[]> {
        return prisma.clinic.findMany({
            include: { vets: true }
        });
    }

    async create(data: Prisma.AppointmentCreateInput): Promise<Appointment> {
        return prisma.appointment.create({ data });
    }

    async findAllByUser(userId: string): Promise<Appointment[]> {
        return prisma.appointment.findMany({
            where: { userId },
            include: {
                pet: true,
                vet: { include: { clinic: true } }
            },
            orderBy: { date: 'asc' }
        });
    }
}