CREATE TABLE IF NOT EXISTS codigos_verificacao (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    codigo VARCHAR(5) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    expiracao TIMESTAMP NOT NULL,
    utilizado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_codigos_verificacao_tipo CHECK (tipo IN ('ESQUECI_SENHA', 'NOVA_CONTA'))
);

CREATE INDEX IF NOT EXISTS idx_codigos_verificacao_email_tipo
    ON codigos_verificacao (email, tipo);

CREATE INDEX IF NOT EXISTS idx_codigos_verificacao_validacao
    ON codigos_verificacao (email, tipo, utilizado, expiracao);
