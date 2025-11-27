-- CreateEnum
CREATE TYPE "Role" AS ENUM ('OWNER', 'ADMIN', 'VET');

-- CreateEnum
CREATE TYPE "AuthProvider" AS ENUM ('EMAIL', 'GOOGLE', 'FACEBOOK');

-- CreateTable
CREATE TABLE "User"
(
    "id"                  TEXT           NOT NULL,
    "email"               TEXT           NOT NULL,
    "passwordHash"        TEXT,
    "firstName"           TEXT,
    "lastName"            TEXT,
    "avatar"              TEXT,
    "role"                "Role"         NOT NULL DEFAULT 'OWNER',
    "provider"            "AuthProvider" NOT NULL DEFAULT 'EMAIL',
    "googleId"            TEXT,
    "facebookId"          TEXT,
    "failedLoginAttempts" INTEGER        NOT NULL DEFAULT 0,
    "lastFailedLogin"     TIMESTAMP(3),
    "lockedUntil"         TIMESTAMP(3),
    "createdAt"           TIMESTAMP(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt"           TIMESTAMP(3)   NOT NULL,

    CONSTRAINT "User_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Session"
(
    "id"               TEXT         NOT NULL,
    "userId"           TEXT         NOT NULL,
    "refreshTokenHash" TEXT         NOT NULL,
    "ipAddress"        TEXT,
    "userAgent"        TEXT,
    "expiresAt"        TIMESTAMP(3) NOT NULL,
    "createdAt"        TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "revokedAt"        TIMESTAMP(3),
    "revokedReason"    TEXT,

    CONSTRAINT "Session_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "AuditLog"
(
    "id"        TEXT         NOT NULL,
    "userId"    TEXT,
    "action"    TEXT         NOT NULL,
    "metadata"  JSONB,
    "ipAddress" TEXT,
    "userAgent" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "AuditLog_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Account"
(
    "id"                TEXT           NOT NULL,
    "userId"            TEXT           NOT NULL,
    "provider"          "AuthProvider" NOT NULL,
    "providerAccountId" TEXT           NOT NULL,
    "createdAt"         TIMESTAMP(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt"         TIMESTAMP(3)   NOT NULL,

    CONSTRAINT "Account_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Pet"
(
    "id"        TEXT         NOT NULL,
    "name"      TEXT         NOT NULL,
    "species"   TEXT         NOT NULL,
    "breed"     TEXT,
    "birthDate" TIMESTAMP(3),
    "gender"    TEXT,
    "microchip" TEXT,
    "color"     TEXT,
    "avatar"    TEXT,
    "ownerId"   TEXT         NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Pet_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Vaccine"
(
    "id"       TEXT         NOT NULL,
    "name"     TEXT         NOT NULL,
    "date"     TIMESTAMP(3) NOT NULL,
    "nextDate" TIMESTAMP(3) NOT NULL,
    "status"   TEXT         NOT NULL DEFAULT 'valid',
    "petId"    TEXT         NOT NULL,

    CONSTRAINT "Vaccine_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "WeightLog"
(
    "id"     TEXT             NOT NULL,
    "weight" DOUBLE PRECISION NOT NULL,
    "date"   TIMESTAMP(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "petId"  TEXT             NOT NULL,

    CONSTRAINT "WeightLog_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Clinic"
(
    "id"        TEXT         NOT NULL,
    "name"      TEXT         NOT NULL,
    "address"   TEXT         NOT NULL,
    "phone"     TEXT         NOT NULL,
    "email"     TEXT,
    "image"     TEXT,
    "rating"    DOUBLE PRECISION,
    "reviews"   INTEGER      NOT NULL DEFAULT 0,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "Clinic_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Vet"
(
    "id"        TEXT    NOT NULL,
    "name"      TEXT    NOT NULL,
    "specialty" TEXT    NOT NULL,
    "phone"     TEXT,
    "email"     TEXT,
    "available" BOOLEAN NOT NULL DEFAULT true,
    "clinicId"  TEXT    NOT NULL,

    CONSTRAINT "Vet_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Appointment"
(
    "id"        TEXT         NOT NULL,
    "date"      TIMESTAMP(3) NOT NULL,
    "type"      TEXT         NOT NULL,
    "status"    TEXT         NOT NULL DEFAULT 'upcoming',
    "notes"     TEXT,
    "reason"    TEXT,
    "petId"     TEXT         NOT NULL,
    "vetId"     TEXT         NOT NULL,
    "userId"    TEXT         NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Appointment_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "User_email_key" ON "User" ("email");

-- CreateIndex
CREATE UNIQUE INDEX "User_googleId_key" ON "User" ("googleId");

-- CreateIndex
CREATE UNIQUE INDEX "User_facebookId_key" ON "User" ("facebookId");

-- CreateIndex
CREATE UNIQUE INDEX "Account_provider_providerAccountId_key" ON "Account" ("provider", "providerAccountId");

-- AddForeignKey
ALTER TABLE "Session"
    ADD CONSTRAINT "Session_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "AuditLog"
    ADD CONSTRAINT "AuditLog_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User" ("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Account"
    ADD CONSTRAINT "Account_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Pet"
    ADD CONSTRAINT "Pet_ownerId_fkey" FOREIGN KEY ("ownerId") REFERENCES "User" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Vaccine"
    ADD CONSTRAINT "Vaccine_petId_fkey" FOREIGN KEY ("petId") REFERENCES "Pet" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "WeightLog"
    ADD CONSTRAINT "WeightLog_petId_fkey" FOREIGN KEY ("petId") REFERENCES "Pet" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Vet"
    ADD CONSTRAINT "Vet_clinicId_fkey" FOREIGN KEY ("clinicId") REFERENCES "Clinic" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Appointment"
    ADD CONSTRAINT "Appointment_petId_fkey" FOREIGN KEY ("petId") REFERENCES "Pet" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Appointment"
    ADD CONSTRAINT "Appointment_vetId_fkey" FOREIGN KEY ("vetId") REFERENCES "Vet" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Appointment"
    ADD CONSTRAINT "Appointment_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;
