INSERT INTO idosos (nome, data_nascimento, sexo, condicoes, observacoes)
VALUES (
    'Maria das Dores Silva',
    '1942-03-15',
    'Feminino',
    'Hipertensão, diabetes tipo 2, mobilidade reduzida.',
    'Prefere medicações com líquido quando possível. Dorme pouco à noite.'
);

INSERT INTO medicamentos (nome, descricao, dosagem, unidade, estoque_atual)
VALUES
    ('Losartana', 'Anti-hipertensivo', '50mg', 'comprimido', 30),
    ('Metformina', 'Antidiabético', '500mg', 'comprimido', 60),
    ('Dipirona', 'Analgésico / antitérmico', '1g', 'comprimido', 20),
    ('Insulina NPH', 'Insulina de ação intermediária', '10U', 'injeção', 2),
    ('Soro Fisiológico 0,9%', 'Solução para limpeza de feridas', '100ml', 'frasco', 5);

INSERT INTO lembretes (idoso_id, medicamento_id, horario, dias_semana, ativo, observacao)
VALUES
    (1, 1, '08:00', '1,2,3,4,5,6,7', true, 'Tomar em jejum'),
    (1, 2, '12:00', '1,2,3,4,5,6,7', true, 'Tomar durante o almoço'),
    (1, 3, '20:00', '1,2,3,4,5,6,7', true, 'Só se sentir dor'),
    (1, 4, '07:30', '1,2,3,4,5,6,7', true, 'Aplicar antes do café');
