import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
    console.log('🌱 Seeding database...');

    // Nettoyage (Optionnel, attention en prod !)
    // await prisma.appointment.deleteMany();
    // await prisma.vet.deleteMany();
    // await prisma.clinic.deleteMany();

    const clinic1 = await prisma.clinic.create({
        data: {
            name: "Clinique des Animaux",
            address: "12 rue des Fleurs, 75001 Paris",
            phone: "01 23 45 67 89",
            image: "🏥",
            rating: 4.8,
            vets: {
                create: [
                    { name: "Dr. Sophie Martin", specialty: "Médecine générale" },
                    { name: "Dr. Pierre Dubois", specialty: "Chirurgie" },
                ]
            }
        }
    });

    const clinic2 = await prisma.clinic.create({
        data: {
            name: "Vétérinaire du Parc",
            address: "45 avenue du Parc, 75002 Paris",
            phone: "01 98 76 54 32",
            image: "🏨",
            rating: 4.5,
            vets: {
                create: [
                    { name: "Dr. Jean Bernard", specialty: "Dermatologie" }
                ]
            }
        }
    });

    console.log('✅ Database seeded!');
}

main()
    .catch((e) => { console.error(e); process.exit(1); })
    .finally(async () => { await prisma.$disconnect(); });