import { Request, Response, NextFunction } from 'express';
import { AppointmentService } from '../services/appointment.service';

const service = new AppointmentService();

export class AppointmentController {

    static async getClinics(req: Request, res: Response, next: NextFunction) {
        try {
            const clinics = await service.getClinics();
            res.json(clinics);
        } catch (error) { next(error); }
    }

    static async getMyAppointments(req: Request, res: Response, next: NextFunction) {
        try {
            const appts = await service.getMyAppointments(req.user.id);
            res.json(appts);
        } catch (error) { next(error); }
    }

    static async create(req: Request, res: Response, next: NextFunction) {
        try {
            // TODO: Ajouter Zod validation ici
            const appt = await service.createAppointment(req.user.id, req.body);
            res.status(201).json(appt);
        } catch (error) { next(error); }
    }
}