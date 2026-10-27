package com.careidoso.model;

/**
 * Finalidade de um código de verificação. Os valores também estão num CHECK
 * constraint do banco (migration V3): novos tipos exigem nova migration.
 */
public enum TipoCodigoVerificacao {
    ESQUECI_SENHA,
    NOVA_CONTA
}
