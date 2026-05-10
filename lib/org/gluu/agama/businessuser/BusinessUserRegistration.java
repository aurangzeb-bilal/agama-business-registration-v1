package org.gluu.agama.businessuser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gluu.agama.businessregistration.JansBusinessUserRegistration;

public abstract class BusinessUserRegistration {

    public abstract boolean isPersonalVerified(String personalUid);
    public abstract Map<String, String> getPersonalUserDetails(String personalUid);

    public abstract String sendOTPCode(String phone, String lang, String verificationMethod);
    public abstract boolean validateOTPCode(String phone, String code);
    

    public abstract String sendEmail(String to, String lang);


    public abstract Map<String, Object> validateBusinessInputs(Map<String, String> profile);
    public abstract Map<String, String> getUserEntityByMail(String email);
    public abstract Map<String, String> getUserEntityByUsername(String username);
    public abstract String addNewBusinessUser(Map<String, String> profile, String personalInum, String phone);
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
