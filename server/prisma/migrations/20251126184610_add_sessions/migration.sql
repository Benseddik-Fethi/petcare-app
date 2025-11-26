/*
  Warnings:

  - You are about to drop the column `revokedReason` on the `Session` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE "Session" DROP COLUMN "revokedReason";
