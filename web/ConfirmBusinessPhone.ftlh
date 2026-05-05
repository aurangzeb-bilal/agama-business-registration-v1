<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Confirm Your Business Phone</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
  </head>
  <body>
    <div class="registration-container">
      <div align="center" class="flex">
        <img src="https://gluu.org/wp-content/uploads/2020/12/logo.png" id="inh1m">
      </div>
      <div class="header">
        <i class="fas fa-mobile-alt"></i>Confirm Your Business Phone
      </div>
      [#if errorMessage?has_content]
      <div role="alert" align="center" class="alert alert-danger mt-3 error">
        <p>${errorMessage}</p>
      </div>
      [/#if]
      <div class="content">
        <p class="explainer">A confirmation OTP will be sent to the phone number on file for your personal account.</p>
        <form method="post" enctype="application/x-www-form-urlencoded" id="confirmPhoneForm">
          <div class="form-group">
            <label class="form-label">Phone Number:</label>
            <div class="phone-display">[#if phone??]${phone}[#else]Loading...[/#if]</div>
          </div>
          <div class="form-group">
            <label class="form-label">Receive Confirmation Code via: <span class="required">*</span></label>
            <div class="radio-group">
              <label><input type="radio" name="verificationMethod" value="sms" checked> SMS</label>
              <label><input type="radio" name="verificationMethod" value="whatsapp"> WhatsApp</label>
            </div>
          </div>
          <div class="buttons">
            <button type="submit" class="btn btn-primary">Send Confirmation Code</button>
          </div>
        </form>
      </div>
    </div>
  </body>
  <style>
    body { font-family: Arial, sans-serif; background-color: #f8f9fa; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
    .flex { padding: 10px; margin-top: auto; }
    .registration-container { width: 100%; max-width: 600px; background: white; border-radius: 5px; box-shadow: 0 4px 8px rgba(0,0,0,0.2); overflow: hidden; }
    .header { background: #28a745; padding: 15px; color: white; font-size: 20px; font-weight: bold; display: flex; align-items: center; }
    .header i { margin-right: 10px; }
    .content { padding: 20px; }
    .explainer { color: #555; margin-bottom: 16px; font-size: 14px; }
    .form-group { margin-bottom: 15px; }
    .form-label { font-weight: bold; display: block; margin-bottom: 5px; }
    .phone-display { padding: 10px; background-color: #f1f3f5; border-radius: 5px; font-family: monospace; font-size: 16px; color: #212529; }
    .required { color: red; }
    .radio-group label { margin-right: 15px; font-weight: normal; }
    .error { font-weight: bold; color: red; }
    .buttons { display: flex; gap: 10px; }
    .btn { padding: 10px 15px; font-size: 16px; border: none; border-radius: 5px; cursor: pointer; }
    .btn-primary { background-color: #007bff; color: white; }
    .btn-primary:hover { background-color: #0056b3; }
  </style>
</html>
