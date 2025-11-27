import {Router} from 'express';
import {PetController} from '../controllers/pet.controller';
import {authenticate} from '../middlewares/auth.middleware';

const router = Router();

router.use(authenticate);

router.get('/', PetController.getMyPets);
router.post('/', PetController.create);
router.get('/:id', PetController.getOne);
router.post('/:id/weights', PetController.addWeight);
router.post('/:id/vaccines', PetController.addVaccine);

export default router;
