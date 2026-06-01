package org.gluu.agama.business.smtp;



class AccountCreationTemplatePt {

    static Map<String, String> get(String userName) {

        String html = """
<table role="presentation" cellspacing="0" cellpadding="0" width="100%" style="background-color:#F2F4F6;">
  <tbody>
    <tr>
      <td align="center">
        <table role="presentation" cellspacing="0" cellpadding="0" width="570" align="center" style="background-color:#FFFFFF;border-radius:4px;margin:20px 0;">
          <tbody>
            <tr>
              <td align="center" style="padding:25px 0;">
                <img src="https://storage.googleapis.com/email_template_staticfiles/Phi_logo320x132_Aug2024.png" width="160" alt="Phi Logo">
              </td>
            </tr>
            <tr>
              <td style="padding:45px;font-family:'Nunito Sans',Helvetica,Arial,sans-serif;font-size:16px;color:#51545E;line-height:1.625;">
                <p>Olá,</p>
                <p>Bem-vindo à <strong>Phi Wallet Business</strong>! A sua conta empresarial está agora ativa.</p>

                <p><strong>O seu nome de utilizador empresarial:</strong></p>
                <div style="text-align:center;margin:30px 0;">
                  <div style="display:inline-block;background-color:#f5f5f5;color:#AD9269;font-size:28px;font-weight:600;letter-spacing:2px;padding:10px 20px;border-radius:4px;">
                    """ + userName + """
                  </div>
                </div>

                <p><strong>Próximo passo: Verificar a identidade da sua empresa</strong></p>
                <p>Para ativar a sua conta e obter acesso total, precisamos de verificar a sua empresa. Este passo confirma a titularidade e protege os ativos da sua empresa.</p>

                <div style="text-align:center;margin:30px 0;">
                  <a href="https://link.business.phiwallet.com/app" style="background-color:#AD9269;color:#ffffff;padding:14px 28px;text-decoration:none;border-radius:4px;font-weight:600;">
                    Abrir a aplicação
                  </a>
                </div>

                <p>Se tiver alguma questão, estamos à distância de uma mensagem. Obrigado por escolher a Phi Wallet Business!</p>
                <p style="margin-top:30px;">Com os melhores cumprimentos,<br>Equipa Phi Wallet Business</p>
              </td>
            </tr>
          </tbody>
        </table>

        <table role="presentation" width="570" align="center" style="text-align:center;">
          <tbody>
            <tr>
              <td style="padding:20px;font-size:12px;color:#666;">
                <p style="font-size:14px;font-weight:bold;color:#565555;">Siga-nos em:</p>
                <p>
                  <a href="https://www.facebook.com/PhiWallet"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/facebook.png" style="height:20px;margin:0 5px;"></a>
                  <a href="https://x.com/PhiWallet"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/twitter.png" style="height:20px;margin:0 5px;"></a>
                  <a href="https://www.instagram.com/phi.wallet"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/instagram.png" style="height:20px;margin:0 5px;"></a>
                  <a href="https://www.linkedin.com/company/phiwallet"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/linkedin.png" style="height:20px;margin:0 5px;"></a>
                </p>
                <p style="margin-top:10px;color:#A8AAAF;">
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
""";

        return Map.of(
            "subject", "Comece com a Phi Wallet Business",
            "body", html
        );
    }
}