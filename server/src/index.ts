import express from 'express';
import cors from 'cors';
import cookieParser from 'cookie-parser';
import helmet from 'helmet';
import {httpLogger, logger} from './utils/logger';
import {env} from './config/env';
import {apiLimiter} from './middlewares/rateLimiter';
import authRoutes from './routes/auth.routes';
import petRoutes from './routes/pet.routes';
import appointmentRoutes from './routes/appointment.routes';
import {doubleCsrfProtection, generateToken} from "./middlewares/csrf.middleware";
import {AppError} from "./utils/AppError";
import {ZodError} from "zod";

const app = express();

app.use(helmet());
app.use(cors({
    origin: env.FRONTEND_URL,
    credentials: true,
}));
app.use(apiLimiter);
app.use(cookieParser());
app.use(express.json({limit: '10kb'})); // Protection payload

app.use(httpLogger);

app.get('/api/csrf-token', (req, res) => {
    const csrfToken = generateToken(req, res);
    res.json({csrfToken});
});

app.use(doubleCsrfProtection);
// 🛣️ Routes
app.use('/api/auth', authRoutes);
app.use('/api/pets', petRoutes);
app.use('/api/appointments', appointmentRoutes);

// 🚨 Error Handler
app.use((err: any, req: any, res: any, next: any) => {
    const statusCode = err instanceof AppError ? err.statusCode : err instanceof ZodError ? 400 : err.statusCode || 500;
    const isServerError = statusCode >= 500;

    if (isServerError) logger.error(err);

    const message = isServerError ? "Erreur interne" : err.message || "Erreur";
    const payload: Record<string, unknown> = {message};

    if (err instanceof ZodError) {
        payload.validation = err.issues.map(issue => ({
            path: issue.path.join('.') || 'root',
            message: issue.message,
        }));
    }

    res.status(statusCode).json(payload);
});

app.listen(env.PORT, () => {
    logger.info(`Server running on port ${env.PORT}`);
});
