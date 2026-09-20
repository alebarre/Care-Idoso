package com.careidoso.util;

/**
 * Mascara dados sensíveis (PII) antes de irem para o log, conforme
 * as regras de segurança do projeto (nunca logar e-mail, senha ou token em claro).
 */
public final class LogSanitizer {

    private LogSanitizer() {
    }

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
