import { AppointmentRepository } from '../repositories/appointment.repository';
import { PetRepository } from "../repositories/pet.repository";
import { BadRequestError, ForbiddenError, NotFoundError } from "../utils/AppError";

type AppointmentPayload = {
    petId: string;
    vetId: string;
    clinicId?: string;
    date: string;
    time: string;
    type: string | { label: string };
    symptoms?: string[];
    notes?: string;
};

export class AppointmentService {
    private readonly repo: AppointmentRepository;
    private readonly petRepo: PetRepository;

    constructor() {
        this.repo = new AppointmentRepository();
        this.petRepo = new PetRepository();
    }

    async getClinics() {
        return this.repo.findAllClinics();
    }

    async getMyAppointments(userId: string) {
        return this.repo.findAllByUser(userId);
    }

    async createAppointment(userId: string, data: AppointmentPayload) {
        const isoDateTime = new Date(`${data.date}T${data.time}:00.000Z`);

        if (Number.isNaN(isoDateTime.getTime())) {
            throw new BadRequestError("La date ou l'heure du rendez-vous est invalide");
        }

        const now = new Date();
        if (isoDateTime.getTime() < now.getTime()) {
            throw new BadRequestError("Le rendez-vous doit être planifié dans le futur");
        }

        const pet = await this.petRepo.findOwnerById(data.petId);
        if (!pet) throw new NotFoundError('Animal introuvable');
        if (pet.ownerId !== userId) throw new ForbiddenError('Cet animal ne vous appartient pas');

        const vet = await this.repo.findVetById(data.vetId);
        if (!vet) throw new NotFoundError('Vétérinaire introuvable');
        if (data.clinicId && vet.clinicId !== data.clinicId) {
            throw new BadRequestError('Le vétérinaire ne correspond pas à la clinique sélectionnée');
        }

        const type = typeof data.type === 'string' ? data.type : data.type.label;
        const reason = data.symptoms?.length ? data.symptoms.join(', ') : undefined;
        const notes = data.notes?.trim() || undefined;

        return this.repo.create({
            date: isoDateTime,
            type,
            reason,
            notes,
            user: { connect: { id: userId } },
            pet: { connect: { id: data.petId } },
            vet: { connect: { id: data.vetId } },
            status: "upcoming"
        });
    }
}