package com.careidoso.util;

/**
 * Mascara dados sensíveis (PII) antes de irem para o log, conforme
 * as regras de segurança do projeto (nunca logar e-mail, senha ou token em claro).
 */
public final class LogSanitizer {

    private LogSanitizer() {
    }

    /**
     * Mascara o e-mail para log, preservando o domínio e apenas a primeira e a última
     * letra do usuário (ex.: {@code a***o@dominio.com}). Usuários com até 2 caracteres
     * são totalmente mascarados; valor nulo ou sem {@code @} vira {@code ***}.
     *
     * Deve ser usado em todo log que cite e-mail. Não é anonimização forte: serve para
     * reduzir exposição em logs, não como identificador seguro.
     */
    public static String mascararEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }

        String[] partes = email.split("@", 2);
        String usuario = partes[0];
        String dominio = partes[1];

        String usuarioMascarado = usuario.length() <= 2
                ? "*".repeat(usuario.length())
                : usuario.charAt(0) + "*".repeat(usuario.length() - 2) + usuario.charAt(usuario.length() - 1);

        return usuarioMascarado + "@" + dominio;
    }
}
