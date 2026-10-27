-- Endurece o OTP: código de 6 dígitos e contador de tentativas erradas por código.
ALTER TABLE codigos_verificacao ALTER COLUMN codigo TYPE VARCHAR(6);
ALTER TABLE codigos_verificacao ADD COLUMN tentativas INT NOT NULL DEFAULT 0;
