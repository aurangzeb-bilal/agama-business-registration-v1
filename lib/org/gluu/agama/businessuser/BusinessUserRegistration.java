package org.gluu.agama.businessuser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gluu.agama.businessregistration.JansBusinessUserRegistration;

public abstract class BusinessUserRegistration {

    // MWAPP gate.
    // GET https://api.phiwallet.dev/v1/webhooks/users/{username}
    //   X-AUTH-CLIENT: <public-key>
    //   X-HMAC-SIGNATURE: hmac-sha256(username, secretKey).hex.lowercase
    // Response: { "phone_verified": true|false, "face_verified": true|false, "kyc_verified": true|false }
    // Returns true only when ALL three are true. False on any other outcome.
    public abstract boolean isPersonalVerified(String personalUid);

    // Jans lookup for the personal user — needed to obtain mobile + lang for sending the OTP.
    // Returns {inum, mobile, lang} only when:
    //   - user exists
    //   - jansStatus == "active"
    //   - mobile attribute is non-empty
    // Returns an empty Map otherwise (caller surfaces "Please use a verified account").
    // Note: phoneNumberVerified is NOT checked here — MWAPP's phone_verified is the authoritative source.
    public abstract Map<String, String> getPersonalUserDetails(String personalUid);

    // OTP send/validate (shared between personal-phone and business-phone OTPs).
    public abstract String sendOTPCode(String phone, String lang, String verificationMethod);
    public abstract boolean validateOTPCode(String phone, String code);

    // Email OTP for business email.
    public abstract String sendEmail(String to, String lang);

    // Business profile validation + creation.
    public abstract Map<String, Object> validateBusinessInputs(Map<String, String> profile);
    public abstract Map<String, String> getUserEntityByMail(String email);
    public abstract Map<String, String> getUserEntityByUsername(String username);
    public abstract String addNewBusinessUser(Map<String, String> profile, String personalInum, String phone) throws Exception;
    public abstract String markPhoneAsVerified(String userName, String phone);

    public abstract boolean sendAccountCreationNotificationEmail(String to, String userName, String lang);

    // Membership helpers (not used by signup flow but exposed for future "manage team" / "list my businesses" surfaces).
    // All keys are Jans personal inums.
    public abstract List<String> findBusinessesCreatedBy(String personalInum);
    public abstract List<String> findBusinessesMemberOf(String personalInum);
    public abstract boolean addMemberToBusiness(String businessUid, String personalInum);

    public static BusinessUserRegistration getInstance(HashMap config) {
        return new JansBusinessUserRegistration(config);
    }
}
