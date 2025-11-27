import {NextFunction, Request, Response} from 'express';
import {PetService} from '../services/pet.service';
import {z} from "zod";

const createPetSchema = z.object({
    name: z.string().trim().min(1),
    species: z.string().trim().min(1),
    breed: z.string().trim().optional(),
    weight: z.union([z.string(), z.number()]).optional(),
    birthDate: z.string().trim().optional(),
    gender: z.string().trim().optional(),
    color: z.string().trim().optional(),
    microchip: z.string().trim().optional(),
});

const weightSchema = z.object({
    weight: z.preprocess((val) => typeof val === 'string' ? parseFloat(val) : val, z.number().positive()),
    date: z.string().trim().min(1),
});

const vaccineSchema = z.object({
    name: z.string().trim().min(1),
    date: z.string().trim().min(1),
    nextDate: z.string().trim().optional(),
});

const petService = new PetService();

export class PetController {

    static async getMyPets(req: Request, res: Response, next: NextFunction) {
        try {
            const pets = await petService.getUserPets(req.userId!);
            res.json(pets);
        } catch (error) {
            next(error);
        }
    }

    static async create(req: Request, res: Response, next: NextFunction) {
        try {
            const payload = createPetSchema.parse(req.body);
            const pet = await petService.createPet(req.userId!, payload);
            res.status(201).json(pet);
        } catch (error) {
            next(error);
        }
    }

    static async getOne(req: Request, res: Response, next: NextFunction) {
        try {
            const {id} = req.params;
            const pet = await petService.getPetDetails(id, req.userId!);
            res.json(pet);
        } catch (error) {
            next(error);
        }
    }

    static async addWeight(req: Request, res: Response, next: NextFunction) {
        try {
            const {id} = req.params;
            const {weight, date} = weightSchema.parse(req.body);
            // Note: Pense à parser 'weight' en float si envoyé en string
            const result = await petService.addWeight(req.userId!, id, weight, date);
            res.status(201).json(result);
        } catch (error) {
            next(error);
        }
    }

    static async addVaccine(req: Request, res: Response, next: NextFunction) {
        try {
            const {id} = req.params;
            const payload = vaccineSchema.parse(req.body);
            const result = await petService.addVaccine(req.userId!, id, payload);
            res.status(201).json(result);
        } catch (error) {
            next(error);
        }
    }
}
