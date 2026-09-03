package com.careidoso.service;

import com.careidoso.model.TipoCodigoVerificacao;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${careidoso.email.from}")
    private String from;

    public void enviarCodigoVerificacao(String email, String codigo, TipoCodigoVerificacao tipo) {
        String assunto = tipo == TipoCodigoVerificacao.ESQUECI_SENHA
                ? "Redefinição de senha — Care Idoso"
                : "Confirme sua nova conta — Care Idoso";

        String corpo = tipo == TipoCodigoVerificacao.ESQUECI_SENHA
                ? templateRedefinicaoSenha(codigo)
                : templateNovaConta(codigo);

        enviar(email, assunto, corpo);
    }

    private void enviar(String para, String assunto, String corpoHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from);
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);

            mailSender.send(message);
            log.info("E-mail de verificação enviado");
        } catch (MessagingException e) {
            log.error("Falha ao enviar e-mail de verificação", e);
            throw new IllegalStateException("Não foi possível enviar o e-mail de verificação. Tente novamente mais tarde.");
        }
    }

    private String templateRedefinicaoSenha(String codigo) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Redefinição de senha</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f3f3f3;font-family:Inter,-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                      <td align="center" style="padding:40px 16px;">
                        <table role="presentation" width="100%%" max-width="480" cellspacing="0" cellpadding="0" border="0" style="max-width:480px;background-color:#000000;border-radius:16px;overflow:hidden;">
                          <tr>
                            <td style="padding:40px 32px;text-align:center;color:#ffffff;">
                              <h1 style="margin:0 0 8px;font-size:24px;font-weight:700;letter-spacing:-0.02em;">Care Idoso</h1>
                              <p style="margin:0;font-size:14px;color:#d2d2d2;">Controle de rotinas de cuidadoras</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:32px;background-color:#ffffff;text-align:center;">
                              <h2 style="margin:0 0 16px;font-size:20px;font-weight:600;color:#000000;">Redefinição de senha</h2>
                              <p style="margin:0 0 24px;font-size:15px;color:#333333;line-height:1.5;">
                                Você solicitou a redefinição da sua senha. Use o código abaixo para continhar. Ele expira em <strong>1 minuto</strong>.
                              </p>
                              <div style="display:inline-block;padding:16px 32px;background-color:#f3f3f3;border-radius:12px;letter-spacing:8px;font-size:32px;font-weight:700;color:#000000;">
                                %s
                              </div>
                              <p style="margin:24px 0 0;font-size:13px;color:#6b6b6b;line-height:1.5;">
                                Não compartilhe este código com ninguém. Se você não solicitou a redefinição, ignore este e-mail.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 32px;background-color:#000000;text-align:center;color:#d2d2d2;font-size:12px;line-height:1.5;">
                              © Care Idoso. Este código é válido por 1 minuto e pode ser usado apenas uma vez.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(codigo);
    }

    private String templateNovaConta(String codigo) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Confirme sua conta</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f3f3f3;font-family:Inter,-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                      <td align="center" style="padding:40px 16px;">
                        <table role="presentation" width="100%%" max-width="480" cellspacing="0" cellpadding="0" border="0" style="max-width:480px;background-color:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #d2d2d2;">
                          <tr>
                            <td style="padding:40px 32px;text-align:center;background-color:#05944F;">
                              <h1 style="margin:0 0 8px;font-size:24px;font-weight:700;letter-spacing:-0.02em;color:#ffffff;">Care Idoso</h1>
                              <p style="margin:0;font-size:14px;color:#e6f4ea;">Controle de rotinas de cuidadoras</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:32px;text-align:center;">
                              <h2 style="margin:0 0 16px;font-size:20px;font-weight:600;color:#000000;">Confirme sua nova conta</h2>
                              <p style="margin:0 0 24px;font-size:15px;color:#333333;line-height:1.5;">
                                Seu cadastro está quase pronto. Insira o código abaixo no aplicativo para ativar sua conta. Ele expira em <strong>1 minuto</strong>.
                              </p>
                              <div style="display:inline-block;padding:16px 32px;background-color:#e6f4ea;border-radius:12px;letter-spacing:8px;font-size:32px;font-weight:700;color:#05944F;">
                                %s
                              </div>
                              <p style="margin:24px 0 0;font-size:13px;color:#6b6b6b;line-height:1.5;">
                                Não compartilhe este código. Se você não criou uma conta, ignore este e-mail.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 32px;text-align:center;color:#6b6b6b;font-size:12px;line-height:1.5;">
                              © Care Idoso. Código válido por 1 minuto e de uso único.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(codigo);
    }
}
