import { AppointmentRepository } from '../repositories/appointment.repository';

export class AppointmentService {
    private readonly repo: AppointmentRepository;

    constructor() {
        this.repo = new AppointmentRepository();
    }

    async getClinics() {
        return this.repo.findAllClinics();
    }

    async getMyAppointments(userId: string) {
        return this.repo.findAllByUser(userId);
    }

    async createAppointment(userId: string, data: any) {

        const isoDateTime = new Date(`${data.date}T${data.time}:00.000Z`);

        return this.repo.create({
            date: isoDateTime,
            type: data.type.label || data.type, // Selon format front
            reason: data.symptoms?.join(', '),
            notes: data.notes,
            user: { connect: { id: userId } },
            pet: { connect: { id: data.petId } },
            vet: { connect: { id: data.vetId } },
            status: "upcoming"
        });
    }
}