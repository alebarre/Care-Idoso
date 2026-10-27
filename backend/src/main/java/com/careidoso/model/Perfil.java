package com.careidoso.model;

/**
 * Perfis de acesso. ADMIN tem acesso total (inclui gestão de usuários); CUIDADORA
 * registra rotinas, medições, medicações, observações e pedidos. Mapeado como
 * {@code ROLE_<nome>} no Spring Security.
 */
public enum Perfil {
    ADMIN,
    CUIDADORA
}
