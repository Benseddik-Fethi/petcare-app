import { Request, Response, NextFunction } from 'express';
import { PetService } from '../services/pet.service';

const petService = new PetService();

export class PetController {

    static async getMyPets(req: Request, res: Response, next: NextFunction) {
        try {
            const pets = await petService.getUserPets(req.userId!);
            res.json(pets);
        } catch (error) { next(error); }
    }

    static async create(req: Request, res: Response, next: NextFunction) {
        try {
            const pet = await petService.createPet(req.userId!, req.body);
            res.status(201).json(pet);
        } catch (error) { next(error); }
    }
    static async getOne(req: Request, res: Response, next: NextFunction) {
        try {
            const { id } = req.params;
            const pet = await petService.getPetDetails(id, req.userId!);
            res.json(pet);
        } catch (error) { next(error); }
    }

    static async addWeight(req: Request, res: Response, next: NextFunction) {
        try {
            const { id } = req.params;
            const { weight, date } = req.body;
            // Note: Pense à parser 'weight' en float si envoyé en string
            const result = await petService.addWeight(req.userId!, id, parseFloat(weight), date);
            res.status(201).json(result);
        } catch (error) { next(error); }
    }

    static async addVaccine(req: Request, res: Response, next: NextFunction) {
        try {
            const { id } = req.params;
            const result = await petService.addVaccine(req.userId!, id, req.body);
            res.status(201).json(result);
        } catch (error) { next(error); }
    }
}