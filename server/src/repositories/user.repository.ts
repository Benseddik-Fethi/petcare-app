import { prisma } from "../config/prisma";

export class UserRepository {
    async findByEmail(email: string) {
        return prisma.user.findUnique({ where: { email } });
    }

    async create(data: any) {
        const { password, ...safeData } = data;
        return prisma.user.create({ data: { ...safeData, passwordHash: password } });
    }

    async findById(id: string) {
        return prisma.user.findUnique({
            where: { id },
            select: { id: true, email: true, firstName: true, lastName: true, role: true, avatar: true, createdAt: true }
        });
    }

    async updateLockout(id: string, data: any) {
        return prisma.user.update({ where: { id }, data });
    }
}