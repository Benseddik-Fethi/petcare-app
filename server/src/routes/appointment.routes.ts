import {Router} from 'express';
import {AppointmentController} from '../controllers/appointment.controller';
import {authenticate} from '../middlewares/auth.middleware';

const router = Router();

router.use(authenticate); // Tout est protégé

router.get('/clinics', AppointmentController.getClinics); // Pour remplir le select du wizard
router.get('/', AppointmentController.getMyAppointments); // Pour le calendrier
router.post('/', AppointmentController.create); // Valider le wizard

export default router;
