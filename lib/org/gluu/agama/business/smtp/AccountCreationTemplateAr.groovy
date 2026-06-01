package org.gluu.agama.business.smtp;



class AccountCreationTemplateAr {

    static Map<String, String> get(String userName) {

        String html = """
<table role="presentation" cellspacing="0" cellpadding="0" width="100%" style="background-color:#F2F4F6;margin:0;padding:0;width:100%;">
  <tbody>
    <tr>
      <td align="center">
        <table role="presentation" cellspacing="0" cellpadding="0" width="100%" style="margin:0;padding:0;">
          <tbody>
            <!-- Logo -->
            <tr>
              <td align="center" style="padding:25px 0;text-align:center;">
                <img src="https://storage.googleapis.com/email_template_staticfiles/Phi_logo320x132_Aug2024.png" width="160" alt="Phi Logo" style="border:none;">
              </td>
            </tr>

            <!-- Main Email Body -->
            <tr>
              <td style="width:100%;margin:0;padding:0;">
                <table role="presentation" dir="rtl" cellspacing="0" cellpadding="0" width="570" align="center" style="background-color:#FFFFFF;margin:0 auto;padding:0;border-radius:4px;">
                  <tbody>
                    <tr>
                      <td style="padding:45px;font-family:'Nunito Sans',Helvetica,Arial,sans-serif;color:#51545E;font-size:16px;line-height:1.8;text-align:right;">
                        <p>مرحبًا،</p>
                        <p>أهلاً بك في <strong>Phi Wallet Business</strong>! حسابك التجاري نشط الآن.</p>

                        <p><strong>اسم المستخدم التجاري الخاص بك:</strong></p>
                        <div style="text-align:center;margin:30px 0;">
                          <div style="display:inline-block;background-color:#f5f5f5;color:#AD9269;font-size:28px;font-weight:600;letter-spacing:2px;padding:10px 20px;border-radius:4px;">
                            """ + userName + """
                          </div>
                        </div>

                        <p><strong>الخطوة التالية: التحقق من هويتك التجارية</strong></p>
                        <p>لتفعيل نشاطك التجاري وفتح الوصول الكامل، نحتاج إلى التحقق منه. تؤكد هذه الخطوة الملكية وتحمي أصولك التجارية.</p>

                        <div style="text-align:center;margin:30px 0;">
                          <a href="https://link.business.phiwallet.com/app" style="background-color:#AD9269;color:#ffffff;padding:14px 28px;text-decoration:none;border-radius:4px;font-weight:600;display:inline-block;">
                            افتح التطبيق
                          </a>
                        </div>
                        <p>إذا كانت لديك أي أسئلة، نحن على بُعد رسالة. شكراً لاختيارك Phi Wallet Business!</p>

                        <p style="margin-top:30px;">مع أطيب التحيات،<br>فريق Phi Wallet Business</p>

                      </td>
                    </tr>
                  </tbody>
                </table>
              </td>
            </tr>

            <!-- Footer -->
            <tr>
              <td>
                <table role="presentation" cellspacing="0" cellpadding="0" width="570" align="center" style="margin:0 auto;padding:0;text-align:center;">
                  <tbody>
                    <tr>
                      <td style="padding:20px;font-size:12px;color:#666;">
                        <p style="margin:0 0 10px 0;font-size:14px;font-weight:bold;color:#565555;">تابعنا على:</p>
                        <p>
                          <a href="https://www.facebook.com/PhiWallet" style="margin:0 5px;"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/facebook.png" style="height:20px;"></a>
                          <a href="https://x.com/PhiWallet" style="margin:0 5px;"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/twitter.png" style="height:20px;"></a>
                          <a href="https://www.instagram.com/phi.wallet" style="margin:0 5px;"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/instagram.png" style="height:20px;"></a>
                          <a href="https://www.linkedin.com/company/phiwallet" style="margin:0 5px;"><img src="https://storage.googleapis.com/mwapp_prod_bucket/social_icon_images/linkedin.png" style="height:20px;"></a>
                        </p>
                        <p style="margin-top:10px;line-height:20px;color:#A8AAAF;font-size:12px;">
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
            "subject", "ابدأ مع Phi Wallet Business",
            "body", html
        );
    }
}