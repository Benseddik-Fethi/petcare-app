import {NextFunction, Request, Response} from 'express';
import {AppointmentService} from '../services/appointment.service';
import {z} from "zod";

const appointmentSchema = z.object({
    petId: z.string().uuid(),
    vetId: z.string().uuid(),
    clinicId: z.string().uuid().optional(),
    date: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
    time: z.string().regex(/^([01]\d|2[0-3]):[0-5]\d$/),
    type: z.union([
        z.string().trim().min(1),
        z.object({label: z.string().trim().min(1)})
    ]),
    symptoms: z.array(z.string().trim().min(1)).optional(),
    notes: z.string().trim().max(1000).optional(),
});

const service = new AppointmentService();

export class AppointmentController {

    static async getClinics(req: Request, res: Response, next: NextFunction) {
        try {
            const clinics = await service.getClinics();
            res.json(clinics);
        } catch (error) {
            next(error);
        }
    }

    static async getMyAppointments(req: Request, res: Response, next: NextFunction) {
        try {
            const appts = await service.getMyAppointments(req.userId!);
            res.json(appts);
        } catch (error) {
            next(error);
        }
    }

    static async create(req: Request, res: Response, next: NextFunction) {
        try {
            const payload = appointmentSchema.parse(req.body);
            const appt = await service.createAppointment(req.userId!, payload);
            res.status(201).json(appt);
        } catch (error) {
            next(error);
        }
    }
}
