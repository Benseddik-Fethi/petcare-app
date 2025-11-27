import {Router} from 'express';
import {AuthController} from '../controllers/auth.controller';
import {authenticate} from '../middlewares/auth.middleware';
import {authLimiter} from '../middlewares/rateLimiter';

const router = Router();

// Routes Publiques (avec Rate Limit strict)
router.post('/register', authLimiter, AuthController.register);
router.post('/login', authLimiter, AuthController.login);
router.post('/social', authLimiter, AuthController.social); // ✅ C'est réparé ici

// Gestion de Session
router.post('/refresh', AuthController.refresh);
router.post('/logout', AuthController.logout);

// Routes Protégées
router.get('/me', authenticate, AuthController.me);

export default router;
