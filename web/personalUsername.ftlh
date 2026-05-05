<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify Your Personal Account</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
  </head>
  <body>
    <div class="registration-container">
      <div align="center" class="flex">
        <img src="https://gluu.org/wp-content/uploads/2020/12/logo.png" id="inh1m">
      </div>
      <div class="header">
        <i class="fas fa-user-shield"></i>Verify Your Personal Account
      </div>
      [#if errorMessage?has_content]
      <div role="alert" align="center" class="alert alert-danger mt-3 error">
        <p>${errorMessage}</p>
      </div>
      [/#if]
      [#if infoMessage?has_content]
      <div role="alert" align="center" class="alert alert-info mt-3 info">
        <p>${infoMessage}</p>
      </div>
      [/#if]
      <div class="content">
        <form method="post" enctype="application/x-www-form-urlencoded" id="personalForm">
          <div class="form-group">
            <label class="form-label">Personal Username: <span class="required">*</span></label>
            <input type="text" id="personalUid" name="personalUid" pattern="^[A-Za-z][A-Za-z0-9]{5,19}$" title="6-20 characters, start with a letter, letters and digits only" required="" placeholder="your-personal-username" class="form-control">
          </div>
          <div class="form-group">
            <label class="form-label">Receive Verification Code via: <span class="required">*</span></label>
            <div class="radio-group">
              <label><input type="radio" name="verificationMethod" value="sms" checked> SMS</label>
              <label><input type="radio" name="verificationMethod" value="whatsapp"> WhatsApp</label>
            </div>
          </div>
          <div class="buttons">
            <button type="submit" class="btn btn-primary">Continue</button>
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
    .form-group { margin-bottom: 15px; }
    .form-label { font-weight: bold; display: block; margin-bottom: 5px; }
    .form-control { width: 95%; padding: 10px; font-size: 14px; border: 1px solid #ced4da; border-radius: 5px; }
    .required { color: red; }
    .radio-group label { margin-right: 15px; font-weight: normal; }
    .error { font-weight: bold; color: red; }
    .info { color: #0c5460; }
    .buttons { display: flex; gap: 10px; }
    .btn { padding: 10px 15px; font-size: 16px; border: none; border-radius: 5px; cursor: pointer; }
    .btn-primary { background-color: #007bff; color: white; }
    .btn-primary:hover { background-color: #0056b3; }
  </style>
</html>
