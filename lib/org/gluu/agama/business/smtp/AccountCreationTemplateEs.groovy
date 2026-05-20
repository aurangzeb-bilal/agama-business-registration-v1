package org.gluu.agama.business.smtp;



class AccountCreationTemplateEs {

    static Map<String, String> get(String userName) {

        String html = """
<table role="presentation" cellspacing="0" cellpadding="0" width="100%" style="background-color:#F2F4F6;margin:0;padding:0;width:100%;">
  <tbody>
    <tr>
      <td align="center">
        <table role="presentation" cellspacing="0" cellpadding="0" width="100%" style="margin:0;padding:0;">
          <tbody>
            <tr>
              <td align="center" style="padding:25px 0;text-align:center;">
                <img src="https://storage.googleapis.com/email_template_staticfiles/Phi_logo320x132_Aug2024.png" width="160" alt="Phi Logo" style="border:none;">
              </td>
            </tr>

            <tr>
              <td style="width:100%;margin:0;padding:0;">
                <table role="presentation" cellspacing="0" cellpadding="0" width="570" align="center" style="background-color:#FFFFFF;margin:0 auto;padding:0;border-radius:4px;">
                  <tbody>
                    <tr>
                      <td style="padding:45px;font-family:'Nunito Sans',Helvetica,Arial,sans-serif;color:#51545E;font-size:16px;line-height:1.625;">
                        <p>Hola,</p>
                        <p>¡Bienvenido/a a <strong>Phi Wallet Business</strong>! Tu cuenta empresarial ya está activa.</p>

                        <p><strong>Tu nombre de usuario empresarial:</strong></p>
                        <div style="text-align:center;margin:30px 0;">
                          <div style="display:inline-block;background-color:#f5f5f5;color:#AD9269;font-size:28px;font-weight:600;letter-spacing:2px;padding:10px 20px;border-radius:4px;">
                            """ + userName + """
                          </div>
                        </div>

                        <p><strong>Siguiente paso: Verifica tu identidad empresarial</strong></p>
                        <p>Para activar tu empresa y obtener acceso completo, necesitamos verificarla. Este paso confirma la titularidad y protege los activos de tu empresa.</p>

                        <div style="text-align:center;margin:30px 0;">
                          <a href="https://link.business.phiwallet.com/vll3ylhkeqb" style="background-color:#AD9269;color:#ffffff;padding:14px 28px;text-decoration:none;border-radius:4px;font-weight:600;display:inline-block;">
                            Abrir la aplicación
                          </a>
                        </div>

                        <p>Si tienes alguna pregunta, estamos a un mensaje de distancia. ¡Gracias por elegir Phi Wallet Business!</p>
                        <p style="margin-top:30px;">Saludos,<br>Equipo Phi Wallet Business</p>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </td>
            </tr>

            <tr>
              <td>
                <table role="presentation" cellspacing="0" cellpadding="0" width="570" align="center" style="margin:0 auto;padding:0;text-align:center;">
                  <tbody>
                    <tr>
                      <td style="padding:20px;font-size:12px;color:#666;">
                        <p style="margin:0 0 10px 0;font-size:14px;font-weight:bold;color:#565555;">Síguenos en:</p>
                        <p>
                          <a href="https://www.facebook.com/PhiWallet"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/facebook.png" style="height:20px;margin:0 5px;"></a>
                          <a href="https://x.com/PhiWallet"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/twitter.png" style="height:20px;margin:0 5px;"></a>
                          <a href="https://www.instagram.com/phi.wallet"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/instagram.png" style="height:20px;margin:0 5px;"></a>
                          <a href="https://www.linkedin.com/company/phiwallet"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/linkedin.png" style="height:20px;margin:0 5px;"></a>
                        </p>
                        <p style="margin-top:10px;color:#A8AAAF;font-size:12px;">
                          Phi Wallet, Unipessoal, LDA<br>
                          Avenida Dom João II, Lote 11902/A Escritório 2.10<br>
                          Lisboa - 1990-366<br>
                          Portugal
                        </p>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </td>
            </tr>

          </tbody>
        </table>
      </td>
    </tr>
  </tbody>
</table>
""";

        return Map.of(
            "subject", "Comienza con Phi Wallet Business",
            "body", html
        );
    }
}