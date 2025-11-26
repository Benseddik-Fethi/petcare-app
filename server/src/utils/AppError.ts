export class AppError extends Error {
    statusCode: number;
    code?: string;

    constructor(message: string, statusCode = 500, code?: string) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
        Object.setPrototypeOf(this, new.target.prototype);
    }
}

export class BadRequestError extends AppError { constructor(m = "Bad request") { super(m, 400, "BAD_REQUEST"); } }
export class UnauthorizedError extends AppError { constructor(m = "Unauthorized") { super(m, 401, "UNAUTHORIZED"); } }
export class ForbiddenError extends AppError { constructor(m = "Forbidden") { super(m, 403, "FORBIDDEN"); } }
export class NotFoundError extends AppError { constructor(m = "Not found") { super(m, 404, "NOT_FOUND"); } }
export class ConflictError extends AppError { constructor(m = "Conflict") { super(m, 409, "CONFLICT"); } }
