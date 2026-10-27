-- Distingue "cadastro público ainda não confirmado por e-mail" (FALSE) de conta desativada
-- pelo ADMIN: só a primeira pode ser reaproveitada pelo auto-cadastro. Usuários existentes
-- ficam como confirmados (TRUE), então um cadastro público que estava pendente antes desta
-- migration precisa ser refeito pelo ADMIN.
ALTER TABLE usuarios ADD COLUMN cadastro_confirmado BOOLEAN NOT NULL DEFAULT TRUE;
