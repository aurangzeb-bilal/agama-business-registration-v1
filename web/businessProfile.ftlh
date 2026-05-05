<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Business Registration</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
  </head>
  <body>
    <div class="registration-container">
      <div align="center" class="flex">
        <img src="https://gluu.org/wp-content/uploads/2020/12/logo.png" id="inh1m">
      </div>
      <div class="header">
        <i class="fas fa-briefcase"></i>Business Registration
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
        <form method="post" enctype="application/x-www-form-urlencoded" id="businessForm">
          <div class="form-group">
            <label class="form-label">Business Name: <span class="required">*</span></label>
            <input type="text" id="o" name="o" pattern="^[A-Za-z0-9 .&'\-,]{2,100}$" title="2-100 characters" required="" placeholder="Acme Corporation Ltd." class="form-control">
          </div>
          <div class="form-group">
            <label class="form-label">Business Username: <span class="required">*</span></label>
            <input type="text" id="uid" name="uid" pattern="^[A-Za-z][A-Za-z0-9]{5,19}$" title="6-20 characters, start with a letter, letters and digits only" required="" placeholder="acmecorp" class="form-control">
          </div>
          <div class="form-group">
            <label class="form-label">Business Email: <span class="required">*</span></label>
            <input type="email" id="mail" name="mail" required="" placeholder="ops@acme.example" class="form-control">
          </div>
          <div class="form-group">
            <label class="form-label">Residence Country (ISO 2-letter): <span class="required">*</span></label>
            <input type="text" id="residenceCountry" name="residenceCountry" pattern="^[A-Z]{2}$" title="2 uppercase letters (e.g., US, GB, AE)" required="" placeholder="US" class="form-control">
          </div>
          <div class="form-group">
            <label class="form-label">Language: <span class="required">*</span></label>
            <select id="lang" name="lang" required class="form-control">
              <option value="en">English</option>
              <option value="ar">العربية</option>
              <option value="es">Español</option>
              <option value="fr">Français</option>
              <option value="id">Bahasa Indonesia</option>
              <option value="pt">Português</option>
            </select>
          </div>
          <div class="buttons">
            <button type="submit" class="btn btn-primary">Continue</button>
          </div>
        </form>
      </div>
    </div>
  </body>
  <style>
    body { font-family: Arial, sans-serif; background-color: #f8f9fa; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; padding: 20px; }
    .flex { padding: 10px; margin-top: auto; }
    .registration-container { width: 100%; max-width: 600px; background: white; border-radius: 5px; box-shadow: 0 4px 8px rgba(0,0,0,0.2); overflow: hidden; }
    .header { background: #28a745; padding: 15px; color: white; font-size: 20px; font-weight: bold; display: flex; align-items: center; }
    .header i { margin-right: 10px; }
    .content { padding: 20px; }
    .form-group { margin-bottom: 15px; }
    .form-label { font-weight: bold; display: block; margin-bottom: 5px; }
    .form-control { width: 95%; padding: 10px; font-size: 14px; border: 1px solid #ced4da; border-radius: 5px; }
    .required { color: red; }
    .error { font-weight: bold; color: red; }
    .info { color: #0c5460; }
    .buttons { display: flex; gap: 10px; }
    .btn { padding: 10px 15px; font-size: 16px; border: none; border-radius: 5px; cursor: pointer; }
    .btn-primary { background-color: #007bff; color: white; }
    .btn-primary:hover { background-color: #0056b3; }
  </style>
</html>
