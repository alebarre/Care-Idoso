CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS idosos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    data_nascimento DATE,
    sexo VARCHAR(20),
    condicoes VARCHAR(1000),
    observacoes VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medicamentos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(500),
    dosagem VARCHAR(50),
    unidade VARCHAR(30),
    estoque_atual INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medicacoes (
    id BIGSERIAL PRIMARY KEY,
    idoso_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    dosagem_admin VARCHAR(50) NOT NULL,
    horario TIMESTAMP NOT NULL,
    via VARCHAR(50),
    observacao VARCHAR(1000),
    cuidadora_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicacoes_idoso FOREIGN KEY (idoso_id) REFERENCES idosos(id),
    CONSTRAINT fk_medicacoes_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamentos(id),
    CONSTRAINT fk_medicacoes_cuidadora FOREIGN KEY (cuidadora_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS medicoes (
    id BIGSERIAL PRIMARY KEY,
    idoso_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor VARCHAR(30) NOT NULL,
    unidade VARCHAR(20),
    observacao VARCHAR(1000),
    cuidadora_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicoes_idoso FOREIGN KEY (idoso_id) REFERENCES idosos(id),
    CONSTRAINT fk_medicoes_cuidadora FOREIGN KEY (cuidadora_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS observacoes (
    id BIGSERIAL PRIMARY KEY,
    idoso_id BIGINT NOT NULL,
    texto VARCHAR(3000) NOT NULL,
    cuidadora_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_observacoes_idoso FOREIGN KEY (idoso_id) REFERENCES idosos(id),
    CONSTRAINT fk_observacoes_cuidadora FOREIGN KEY (cuidadora_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS lembretes (
    id BIGSERIAL PRIMARY KEY,
    idoso_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    horario VARCHAR(10) NOT NULL,
    dias_semana VARCHAR(30),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    observacao VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lembretes_idoso FOREIGN KEY (idoso_id) REFERENCES idosos(id),
    CONSTRAINT fk_lembretes_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamentos(id)
);

CREATE TABLE IF NOT EXISTS pedidos (
    id BIGSERIAL PRIMARY KEY,
    cuidadora_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    observacao VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedidos_cuidadora FOREIGN KEY (cuidadora_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS pedido_itens (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    observacao VARCHAR(500),
    CONSTRAINT fk_pedido_itens_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_pedido_itens_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamentos(id)
);
