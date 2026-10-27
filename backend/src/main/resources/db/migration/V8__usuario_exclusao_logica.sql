-- Exclusão lógica de usuários: medicações, medições, observações e pedidos referenciam
-- usuarios(id), então a linha é preservada e apenas marcada como excluída.
ALTER TABLE usuarios ADD COLUMN excluido BOOLEAN NOT NULL DEFAULT FALSE;
