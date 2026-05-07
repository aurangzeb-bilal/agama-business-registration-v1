package org.gluu.agama.businessregistration;

import io.jans.as.common.model.common.User;
import io.jans.as.common.service.common.UserService;
import io.jans.orm.model.base.CustomObjectAttribute;
import io.jans.orm.exception.operation.EntryNotFoundException;
import io.jans.service.MailService;
import io.jans.model.SmtpConfiguration;
import io.jans.service.cdi.util.CdiUtil;
import io.jans.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import org.gluu.agama.businessuser.BusinessUserRegistration;
import io.jans.agama.engine.script.LogUtils;
import io.jans.as.common.service.common.ConfigurationService;
import java.security.SecureRandom;
import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.regex.Pattern;

import org.gluu.agama.smtp.*;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


public class JansBusinessUserRegistration extends BusinessUserRegistration {

    private static final Logger logger = LoggerFactory.getLogger(JansBusinessUserRegistration.class);

    private static final String CONFIRM_PASSWORD = "confirmPassword";
    private static final String LANG = "lang";
    private static final String RESIDENCE_COUNTRY = "residenceCountry";
    private static final String PHONE_NUMBER = "telephoneNumber"; // business phone — distinct from personal "mobile"
    private static final String ORG_NAME = "businessNam";
    private static final String MAIL = "mail";
    private static final String UID = "uid";
    private static final String PASSWORD = "userPassword";
    private static final String INUM_ATTR = "inum";
    private static final String LINK_ATTR = "businessLink";
    private static final String CREATOR_PREFIX = "businessCreator:";
    private static final String MEMBER_PREFIX = "businessMember:";
    private static final String EMAIL_VERIFIED = "emailVerified";
    private static final String PHONE_VERIFIED = "phoneNumberVerified";
    private static final int OTP_LENGTH = 6;
    public static final int OTP_CODE_LENGTH = 6;
    private static final SecureRandom RAND = new SecureRandom();

    private static JansBusinessUserRegistration INSTANCE = null;
    private Map<String, String> flowConfig;
    private static final Map<String, String> userCodes = new HashMap<>();

    public JansBusinessUserRegistration() {
        this.flowConfig = new HashMap<>();
        logger.info("Initialized JansBusinessUserRegistration with empty config");
    }

    public JansBusinessUserRegistration(Map<String, String> config) {
        this.flowConfig = config;
        logger.info("JansBusinessUserRegistration initialized. Twilio account SID configured: {}", config.get("ACCOUNT_SID") != null);
    }

    public static synchronized BusinessUserRegistration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JansBusinessUserRegistration(new HashMap<>());
        }
        return INSTANCE;
    }

    public static synchronized BusinessUserRegistration getInstance(Map<String, String> config) {
        if (INSTANCE == null) {
            INSTANCE = new JansBusinessUserRegistration(config);
        }
        return INSTANCE;
    }

    // ---------------------------------------------------------------
    // MWAPP gate — checks all three verification flags
    // ---------------------------------------------------------------

    @Override
    public boolean isPersonalVerified(String personalUid) {
        try {
            String publicKey = flowConfig.get("PHIWALLET_PUBLIC_KEY");
            String privateKey = flowConfig.get("PHIWALLET_PRIVATE_KEY");
            String mwappBaseUrl = flowConfig.get("MWAPP_BASE_URL");
            String mwappPath = flowConfig.get("MWAPP_USER_PATH");

            if (publicKey == null || privateKey == null || mwappBaseUrl == null || mwappPath == null) {
                logger.error("MWAPP API not configured (PHIWALLET_PUBLIC_KEY / PHIWALLET_PRIVATE_KEY / MWAPP_BASE_URL / MWAPP_USER_PATH)");
                return false;
            }

            if (personalUid == null || personalUid.trim().isEmpty()) {
                logger.error("personalUid is null/empty");
                return false;
            }

            // HMAC-SHA256(username, secretKey).hex.lowercase
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(privateKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hashBytes = mac.doFinal(personalUid.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            String signature = hex.toString().toLowerCase();

            String encodedUid = java.net.URLEncoder.encode(personalUid, "UTF-8");
            String url = mwappBaseUrl + String.format(mwappPath, encodedUid);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-AUTH-CLIENT", publicKey)
                .header("X-HMAC-SIGNATURE", signature)
                .GET()
                .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("MWAPP API status={} body={}", response.statusCode(), response.body());

            if (response.statusCode() != 200) return false;

            String body = response.body() == null ? "" : response.body();
            // Lightweight regex parse — avoids pulling in JSON dependency at the Agama engine layer.
            boolean phoneOk = body.matches('''(?s).*"phone_verified"\\s*:\\s*true.*''');
            boolean faceOk = body.matches('''(?s).*"face_verified"\\s*:\\s*true.*''');
            boolean kycOk = body.matches('''(?s).*"kyc_verified"\\s*:\\s*true.*''');

            // Extract user_id for logging + defensive cross-check.
            // mwapp's user_id is documented to match the Jans inum.
            String mwappUserId = null;
            java.util.regex.Matcher uidMatch = java.util.regex.Pattern
                .compile('''"user_id"\\s*:\\s*"([^"]+)"''')
                .matcher(body);
            if (uidMatch.find()) {
                mwappUserId = uidMatch.group(1);
                try {
                    UserService userService = CdiUtil.bean(UserService.class);
                    User probe = userService.getUser(personalUid, "uid", "inum");
                    if (probe != null) {
                        String jansInum = getSingleValuedAttr(probe, INUM_ATTR);
                        if (jansInum != null && !jansInum.equalsIgnoreCase(mwappUserId)) {
                            logger.warn("Identity mismatch: mwapp user_id={} but Jans inum={} for uid={}", mwappUserId, jansInum, personalUid);
                        }
                    }
                } catch (Exception ignore) {
                    // Cross-check is best-effort; never let it block the gate decision
                }
            }

            if (!phoneOk || !faceOk || !kycOk) {
                logger.info("MWAPP gate failed for uid={} user_id={}: phone_verified={} face_verified={} kyc_verified={}",
                    personalUid, mwappUserId, phoneOk, faceOk, kycOk);
                return false;
            }
            logger.info("MWAPP gate passed for uid={} user_id={}", personalUid, mwappUserId);
            return true;
        } catch (Exception ex) {
            logger.error("MWAPP gate failed for uid={}: {}", personalUid, ex.getMessage(), ex);
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Jans lookup — reads phone + lang from personal user entry
    // ---------------------------------------------------------------

    @Override
    public Map<String, String> getPersonalUserDetails(String personalUid) {
        Map<String, String> result = new HashMap<>();
        try {
            UserService userService = CdiUtil.bean(UserService.class);

            // Try BOTH common load mechanisms. One of these must surface 'mobile'.
            User fullUser = getUser(UID, personalUid);
            logger.info("DEBUG getUser(UID, {}) dn={} status={}", personalUid,
                fullUser != null ? fullUser.getDn() : "null",
                fullUser != null && fullUser.getStatus() != null ? fullUser.getStatus().getValue() : "null");

            if (fullUser == null) {
                return result;
            }

            // DUMP every dynamic attribute on the loaded user
            List<CustomObjectAttribute> all = fullUser.getCustomAttributes();
            logger.info("DEBUG customAttrs count for {}: {}", personalUid, all != null ? all.size() : -1);
            if (all != null) {
                for (CustomObjectAttribute a : all) {
                    logger.info("DEBUG attr name={} value={} values={}", a.getName(), a.getValue(), a.getValues());
                }
            }

            String status = getSingleValuedAttr(fullUser, "jansStatus");
            if (status != null && !"active".equalsIgnoreCase(status)) {
                logger.info("Personal user uid={} not active (status={})", personalUid, status);
                return result;
            }

            // Multi-valued read attempt
            CustomObjectAttribute mobileAttr = userService.getCustomAttribute(fullUser, "mobile");
            logger.info("DEBUG getCustomAttribute(mobile) -> attr={} value={} values={}",
                mobileAttr,
                mobileAttr != null ? mobileAttr.getValue() : null,
                mobileAttr != null ? mobileAttr.getValues() : null);

            String mobile = null;
            if (mobileAttr != null) {
                if (mobileAttr.getValue() != null && !mobileAttr.getValue().isEmpty()) {
                    mobile = mobileAttr.getValue();
                } else if (mobileAttr.getValues() != null && !mobileAttr.getValues().isEmpty()) {
                    Object first = mobileAttr.getValues().get(0);
                    if (first != null) mobile = first.toString();
                }
            }

            if (mobile == null || mobile.trim().isEmpty()) {
                logger.info("Personal user uid={} has no mobile attribute", personalUid);
                return result;
            }

            String inum = getSingleValuedAttr(fullUser, INUM_ATTR);
            String lang = getSingleValuedAttr(fullUser, "lang");

            result.put("inum", inum);
            result.put("mobile", mobile);
            result.put("lang", lang != null ? lang : "en");
            logger.info("getPersonalUserDetails: uid={} inum={} mobile={} lang={}", personalUid, inum, mobile, lang);
            return result;
        } catch (Exception ex) {
            logger.error("getPersonalUserDetails failed for uid={}: {}", personalUid, ex.getMessage(), ex);
            return result;
        }
    }

    // ---------------------------------------------------------------
    // OTP — Twilio SMS / WhatsApp (mirrors personal flow)
    // ---------------------------------------------------------------

    @Override
    public String sendOTPCode(String phone, String lang, String verificationMethod) {
        try {
            logger.info("Sending OTP to phone: {} via {}", phone, verificationMethod);

            String otpCode = generateSMSOTpCode(OTP_CODE_LENGTH);
            logger.info("Generated OTP {} for phone {}", otpCode, phone);

            String preferredLang = (lang != null && !lang.isEmpty()) ? lang.toLowerCase() : "en";

            Map<String, String> messages = new HashMap<>();
            messages.put("ar", "رمز التحقق OTP الخاص بك من Phi Wallet هو " + otpCode + ". لا تشاركه مع أي شخص.");
            messages.put("en", "Your Phi Wallet OTP is " + otpCode + ". Do not share it with anyone.");
            messages.put("es", "Tu código de Phi Wallet es " + otpCode + ". No lo compartas con nadie.");
            messages.put("fr", "Votre code Phi Wallet est " + otpCode + ". Ne le partagez avec personne.");
            messages.put("id", "Kode Phi Wallet Anda adalah " + otpCode + ". Jangan bagikan kepada siapa pun.");
            messages.put("pt", "O seu código da Phi Wallet é " + otpCode + ". Não o partilhe com ninguém.");

            String message = messages.getOrDefault(preferredLang, messages.get("en"));

            associateGeneratedCodeToPhone(phone, otpCode);
            sendTwilioSms(phone, message, verificationMethod, otpCode, preferredLang);

            return phone;
        } catch (Exception ex) {
            logger.error("Failed to send OTP to phone: {}: {}", phone, ex.getMessage(), ex);
            return null;
        }
    }

    private String generateSMSOTpCode(int codeLength) {
        String numbers = "0123456789";
        SecureRandom random = new SecureRandom();
        char[] otp = new char[codeLength];
        for (int i = 0; i < codeLength; i++) {
            otp[i] = numbers.charAt(random.nextInt(numbers.length()));
        }
        return new String(otp);
    }

    private boolean associateGeneratedCodeToPhone(String phone, String code) {
        try {
            userCodes.put(phone, code);
            return true;
        } catch (Exception e) {
            logger.error("Error associating OTP for phone {}: {}", phone, e.getMessage(), e);
            return false;
        }
    }

    private boolean sendTwilioSms(String phone, String message, String verificationMethod, String otpCode, String lang) {
        try {
            String fromNumber = getFromNumberForPhone(phone);

            if (fromNumber == null || fromNumber.trim().isEmpty()) {
                logger.error("FROM_NUMBER null/empty for phone {}", phone);
                return false;
            }

            boolean isWhatsApp = "whatsapp".equalsIgnoreCase(verificationMethod);

            if (isWhatsApp) {
                String whatsappFrom = flowConfig.get("FROM_NUMBER_WHATSAPP");
                if (whatsappFrom != null && !whatsappFrom.trim().isEmpty()) {
                    fromNumber = whatsappFrom;
                }
                fromNumber = "whatsapp:" + fromNumber;
                phone = "whatsapp:" + phone;
                logger.info("Using WhatsApp channel for OTP delivery");

                String contentSid = getWhatsAppContentSid(lang);
                if (contentSid == null || contentSid.trim().isEmpty()) {
                    logger.error("No WhatsApp content SID for lang={}", lang);
                    return false;
                }

                String accountSid = flowConfig.get("ACCOUNT_SID");
                String authToken = flowConfig.get("AUTH_TOKEN");
                String credentials = Base64.getEncoder().encodeToString(
                    (accountSid + ":" + authToken).getBytes(java.nio.charset.StandardCharsets.UTF_8)
                );

                String encodedTo = java.net.URLEncoder.encode(phone, "UTF-8");
                String encodedFrom = java.net.URLEncoder.encode(fromNumber, "UTF-8");
                String encodedSid = java.net.URLEncoder.encode(contentSid, "UTF-8");
                String encodedVars = java.net.URLEncoder.encode("{\"1\":\"" + otpCode + "\"}", "UTF-8");
                String formBody = "To=" + encodedTo + "&From=" + encodedFrom + "&ContentSid=" + encodedSid + "&ContentVariables=" + encodedVars;

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json"))
                    .header("Authorization", "Basic " + credentials)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

                HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

                logger.info("Twilio WhatsApp API status={} body={}", response.statusCode(), response.body());

                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    logger.error("WhatsApp send failed: {}", response.body());
                    return false;
                }
            } else {
                PhoneNumber FROM_NUMBER = new com.twilio.type.PhoneNumber(fromNumber);
                logger.info("Sending from: {}", fromNumber);
                PhoneNumber TO_NUMBER = new com.twilio.type.PhoneNumber(phone);
                logger.info("Sending to: {}", phone);
                Twilio.init(flowConfig.get("ACCOUNT_SID"), flowConfig.get("AUTH_TOKEN"));
                Message.creator(TO_NUMBER, FROM_NUMBER, message).create();
            }

            logger.info("OTP successfully sent to {}", phone);
            return true;
        } catch (Exception exception) {
            logger.error("Error sending OTP to {}: {}", phone, exception.getMessage(), exception);
            return false;
        }
    }

    private String getWhatsAppContentSid(String lang) {
        String suffix = (lang != null && !lang.isEmpty()) ? lang.toUpperCase() : "EN";
        String sid = flowConfig.get("WHATSAPP_CONTENT_SID_" + suffix);
        if (sid == null || sid.trim().isEmpty()) {
            logger.info("No WhatsApp SID for lang {}, falling back to EN", suffix);
            sid = flowConfig.get("WHATSAPP_CONTENT_SID_EN");
        }
        return sid;
    }

    private String getFromNumberForPhone(String phone) {
        try {
            String defaultFromNumber = flowConfig.get("FROM_NUMBER");
            String usCountryCodes = flowConfig.get("US_COUNTRY_CODES");
            String restrictedCodes = flowConfig.get("RESTRICTED_COUNTRY_CODES");

            if (defaultFromNumber == null || defaultFromNumber.trim().isEmpty()) {
                logger.error("FROM_NUMBER not configured");
                return null;
            }

            Set<String> usCountrySet = new HashSet<>();
            if (usCountryCodes != null && !usCountryCodes.trim().isEmpty()) {
                usCountrySet = Arrays.stream(usCountryCodes.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());
            }

            Set<String> restrictedSet = new HashSet<>();
            if (restrictedCodes != null && !restrictedCodes.trim().isEmpty()) {
                restrictedSet = Arrays.stream(restrictedCodes.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());
            }

            Set<String> allKnownCodes = new HashSet<>();
            allKnownCodes.addAll(usCountrySet);
            allKnownCodes.addAll(restrictedSet);

            String countryCode = extractCountryCode(phone, allKnownCodes);

            if (countryCode == null || countryCode.isEmpty()) {
                return defaultFromNumber;
            }

            if (usCountrySet.contains(countryCode)) {
                String usFromNumber = flowConfig.get("FROM_NUMBER_US");
                if (usFromNumber != null && !usFromNumber.trim().isEmpty()) {
                    return usFromNumber;
                }
            }

            if (restrictedSet.contains(countryCode)) {
                String restrictedFromNumber = flowConfig.get("FROM_NUMBER_RESTRICTED_COUNTRIES");
                if (restrictedFromNumber != null && !restrictedFromNumber.trim().isEmpty()) {
                    return restrictedFromNumber;
                }
            }

            return defaultFromNumber;
        } catch (Exception ex) {
            logger.error("Error in getFromNumberForPhone: {}", ex.getMessage(), ex);
            return flowConfig.get("FROM_NUMBER");
        }
    }

    private String extractCountryCode(String phone, Set<String> knownCodes) {
        if (phone == null || phone.trim().isEmpty()) return null;

        String cleaned = phone.startsWith("+") ? phone.substring(1) : phone;

        if (cleaned.length() < 2) return null;

        if (cleaned.startsWith("1") && cleaned.length() > 1 && Character.isDigit(cleaned.charAt(1))) {
            return "1";
        }

        if (cleaned.length() >= 3 && knownCodes != null && !knownCodes.isEmpty()) {
            String threeDigit = cleaned.substring(0, 3);
            if (knownCodes.contains(threeDigit)) {
                return threeDigit;
            }
        }

        return cleaned.substring(0, 2);
    }

    @Override
    public boolean validateOTPCode(String phone, String code) {
        try {
            String storedCode = userCodes.getOrDefault(phone, "NULL");
            if (storedCode.equalsIgnoreCase(code)) {
                userCodes.remove(phone);
                return true;
            }
            return false;
        } catch (Exception ex) {
            logger.error("Error validating OTP for phone {}: {}", phone, ex.getMessage(), ex);
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Email OTP — reuses personal flow's email templates
    // ---------------------------------------------------------------

    @Override
    public String sendEmail(String to, String lang) {
        try {
            ConfigurationService configService = CdiUtil.bean(ConfigurationService.class);
            SmtpConfiguration smtpConfig = configService.getConfiguration().getSmtpConfiguration();

            if (smtpConfig == null) {
                LogUtils.log("SMTP configuration is missing.");
                return null;
            }

            String preferredLang = (lang != null && !lang.isEmpty()) ? lang.toLowerCase() : "en";

            String otp = IntStream.range(0, OTP_LENGTH)
                    .mapToObj(i -> String.valueOf(RAND.nextInt(10)))
                    .collect(Collectors.joining());

            Map<String, String> templateData;
            switch (preferredLang) {
                case "ar":
                    templateData = EmailRegistrationOtpAr.get(otp);
                    break;
                case "es":
                    templateData = EmailRegistrationOtpEs.get(otp);
                    break;
                case "fr":
                    templateData = EmailRegistrationOtpFr.get(otp);
                    break;
                case "id":
                    templateData = EmailRegistrationOtpId.get(otp);
                    break;
                case "pt":
                    templateData = EmailRegistrationOtpPt.get(otp);
                    break;
                default:
                    templateData = EmailRegistrationOtpEn.get(otp);
                    break;
            }

            String subject = templateData.get("subject");
            String htmlBody = templateData.get("body");
            String textBody = htmlBody.replaceAll("\\<.*?\\>", "");

            MailService mailService = CdiUtil.bean(MailService.class);
            boolean sent = mailService.sendMailSigned(
                    smtpConfig.getFromEmailAddress(),
                    smtpConfig.getFromName(),
                    to,
                    null,
                    subject,
                    textBody,
                    htmlBody);

            if (sent) {
                LogUtils.log("Business email OTP sent to %", to);
                return otp;
            } else {
                LogUtils.log("Failed to send business email OTP to %", to);
                return null;
            }
        } catch (Exception e) {
            LogUtils.log("Failed to send business email OTP: %", e.getMessage());
            return null;
        }
    }

    // ---------------------------------------------------------------
    // Business profile validation + creation
    // ---------------------------------------------------------------

    @Override
    public Map<String, Object> validateBusinessInputs(Map<String, String> profile) {
        LogUtils.log("Validate business inputs");
        Map<String, Object> result = new HashMap<>();

        if (profile.get(UID) == null || !Pattern.matches('''^[A-Za-z][A-Za-z0-9]{5,19}$''', profile.get(UID))) {
            result.put("valid", false);
            result.put("message", "Invalid business username. Must be 6-20 characters, start with a letter, and contain only letters and digits.");
            return result;
        }

        if (profile.get(MAIL) == null || !Pattern.matches('''^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$''', profile.get(MAIL))) {
            result.put("valid", false);
            result.put("message", "Invalid business email address.");
            return result;
        }

        if (profile.get(ORG_NAME) == null || !Pattern.matches('''^[-A-Za-z0-9 .&',]{2,100}$''', profile.get(ORG_NAME))) {
            result.put("valid", false);
            result.put("message", "Invalid organization name. Must be 2-100 characters using letters, digits, spaces, and . & ' - ,");
            return result;
        }

        if (profile.get(LANG) == null || !Pattern.matches('''^(ar|en|es|fr|pt|id)$''', profile.get(LANG))) {
            result.put("valid", false);
            result.put("message", "Invalid language code. Must be one of ar, en, es, fr, pt, or id.");
            return result;
        }

        if (profile.get(RESIDENCE_COUNTRY) == null || !Pattern.matches('''^[A-Z]{2}$''', profile.get(RESIDENCE_COUNTRY))) {
            result.put("valid", false);
            result.put("message", "Invalid residence country. Must be exactly two uppercase letters.");
            return result;
        }

        result.put("valid", true);
        result.put("message", "");
        return result;
    }

    @Override
    public Map<String, String> getUserEntityByMail(String email) {
        User user = getUser(MAIL, email);
        boolean local = user != null;
        LogUtils.log("There is % local account for %", local ? "a" : "no", email);

        if (local) {
            String uid = getSingleValuedAttr(user, UID);
            String inum = getSingleValuedAttr(user, INUM_ATTR);
            Map<String, String> userMap = new HashMap<>();
            userMap.put(UID, uid);
            userMap.put(INUM_ATTR, inum);
            userMap.put("email", email);
            return userMap;
        }

        return new HashMap<>();
    }

    @Override
    public Map<String, String> getUserEntityByUsername(String username) {
        User user = getUser(UID, username);
        boolean local = user != null;
        LogUtils.log("There is % local account for %", local ? "a" : "no", username);

        if (local) {
            String email = getSingleValuedAttr(user, MAIL);
            String inum = getSingleValuedAttr(user, INUM_ATTR);
            String uid = getSingleValuedAttr(user, UID);
            Map<String, String> userMap = new HashMap<>();
            userMap.put(UID, uid);
            userMap.put(INUM_ATTR, inum);
            userMap.put("email", email);
            return userMap;
        }

        return new HashMap<>();
    }

    @Override
    public String addNewBusinessUser(Map<String, String> profile, String personalInum, String phone) {
        try {
            Set<String> attributes = Set.of("uid", "mail", "userPassword", "o", "lang", "residenceCountry");
            User user = new User();

            attributes.forEach(attr -> {
                String val = profile.get(attr);
                if (StringHelper.isNotEmpty(val)) {
                    user.setAttribute(attr, val);
                }
            });

            // If no password supplied on the form, generate a server-side random one so the LDAP entry is valid.
            if (!StringHelper.isNotEmpty(profile.get(PASSWORD))) {
                byte[] randomBytes = new byte[24];
                new SecureRandom().nextBytes(randomBytes);
                String generatedPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
                user.setAttribute(PASSWORD, generatedPassword);
            }

            user.setAttribute(EMAIL_VERIFIED, Boolean.TRUE);
            user.setAttribute(PHONE_VERIFIED, Boolean.FALSE);

            // Multi-valued jansExtUid linkage:
            //   businessCreator:<personalInum>  -> who created the business
            //   businessMember:<personalInum>   -> creator is also the first employee
            // Additional employees added later via addMemberToBusiness().
            user.setAttribute(LINK_ATTR, java.util.List.of(
                CREATOR_PREFIX + personalInum,
                MEMBER_PREFIX + personalInum
            ));

            UserService userService = CdiUtil.bean(UserService.class);
            user = userService.addUser(user, true);

            if (user == null) {
                logger.error("addNewBusinessUser: addUser returned null for uid={}", profile.get(UID));
                return null;
            }

            return getSingleValuedAttr(user, INUM_ATTR);
        } catch (Exception ex) {
            // Returning null instead of throwing keeps the .flow file simple
            // (no need for the dual-assignment "result | E = Call ..." syntax,
            // which Agama Lab's "Create flow by code" import doesn't parse).
            // The flow's `When businessInum is not null` check handles the failure case.
            logger.error("addNewBusinessUser failed for uid={}: {}", profile.get(UID), ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public String markPhoneAsVerified(String userName, String phone) {
        try {
            UserService userService = CdiUtil.bean(UserService.class);
            User user = getUser(UID, userName);
            if (user == null) {
                logger.error("User not found for username {}", userName);
                return "User not found.";
            }

            user.setAttribute(PHONE_NUMBER, phone);
            user.setAttribute(PHONE_VERIFIED, Boolean.TRUE);

            userService.updateUser(user);
            logger.info("Phone verification set to TRUE for UID {}", userName);
            return "Phone " + phone + " verified successfully for user " + userName;
        } catch (Exception e) {
            logger.error("Error marking phone verified for {}: {}", userName, e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    // ---------------------------------------------------------------
    // Welcome email — reuses personal flow's account-creation templates
    // ---------------------------------------------------------------

    @Override
    public boolean sendAccountCreationNotificationEmail(String to, String username, String lang) {
        try {
            ConfigurationService configService = CdiUtil.bean(ConfigurationService.class);
            SmtpConfiguration smtpConfig = configService.getConfiguration().getSmtpConfiguration();

            if (smtpConfig == null) {
                logger.error("SMTP configuration missing.");
                return false;
            }

            String preferredLang = (lang != null && !lang.isEmpty()) ? lang.toLowerCase() : "en";
            Map<String, String> templateData;

            switch (preferredLang) {
                case "ar":
                    templateData = AccountCreationTemplateAr.get(username);
                    break;
                case "es":
                    templateData = AccountCreationTemplateEs.get(username);
                    break;
                case "fr":
                    templateData = AccountCreationTemplateFr.get(username);
                    break;
                case "id":
                    templateData = AccountCreationTemplateId.get(username);
                    break;
                case "pt":
                    templateData = AccountCreationTemplatePt.get(username);
                    break;
                default:
                    templateData = AccountCreationTemplateEn.get(username);
                    break;
            }

            if (templateData == null || !templateData.containsKey("body")) {
                logger.error("No email template for lang {}", preferredLang);
                return false;
            }

            String subject = templateData.getOrDefault("subject", "Your Username Information");
            String htmlBody = templateData.get("body");

            if (htmlBody == null || htmlBody.isEmpty()) {
                logger.error("Email body empty for lang {}", preferredLang);
                return false;
            }

            String textBody = htmlBody.replaceAll("\\<.*?\\>", "");

            MailService mailService = CdiUtil.bean(MailService.class);
            boolean sent = mailService.sendMailSigned(
                    smtpConfig.getFromEmailAddress(),
                    smtpConfig.getFromName(),
                    to,
                    null,
                    subject,
                    textBody,
                    htmlBody);

            return sent;
        } catch (Exception e) {
            LogUtils.log("Failed to send account creation email: %", e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Membership helpers (not invoked by signup flow; for future surfaces)
    // ---------------------------------------------------------------

    @Override
    public List<String> findBusinessesCreatedBy(String personalInum) {
        try {
            UserService us = CdiUtil.bean(UserService.class);
            List<User> hits = us.getUsersByAttribute(LINK_ATTR, CREATOR_PREFIX + personalInum, true, 100);
            List<String> result = new ArrayList<>();
            if (hits == null) return result;
            for (User u : hits) result.add(getSingleValuedAttr(u, INUM_ATTR));
            return result;
        } catch (Exception e) {
            logger.error("findBusinessesCreatedBy failed: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> findBusinessesMemberOf(String personalInum) {
        try {
            UserService us = CdiUtil.bean(UserService.class);
            List<User> hits = us.getUsersByAttribute(LINK_ATTR, MEMBER_PREFIX + personalInum, true, 100);
            List<String> result = new ArrayList<>();
            if (hits == null) return result;
            for (User u : hits) result.add(getSingleValuedAttr(u, INUM_ATTR));
            return result;
        } catch (Exception e) {
            logger.error("findBusinessesMemberOf failed: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addMemberToBusiness(String businessUid, String personalInum) {
        try {
            UserService us = CdiUtil.bean(UserService.class);
            User business = getUser(UID, businessUid);
            if (business == null) return false;

            Object existing = business.getAttribute(LINK_ATTR, false, false);
            List<String> values = new ArrayList<>();
            if (existing instanceof List) {
                for (Object v : (List<?>) existing) values.add(v.toString());
            } else if (existing != null) {
                values.add(existing.toString());
            }

            String token = MEMBER_PREFIX + personalInum;
            if (values.contains(token)) return true;
            values.add(token);

            business.setAttribute(LINK_ATTR, values);
            us.updateUser(business);
            return true;
        } catch (Exception e) {
            logger.error("addMemberToBusiness failed: {}", e.getMessage(), e);
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private String getSingleValuedAttr(User user, String attribute) {
        Object value = null;

        if (attribute.equals(UID)) {
            value = user.getUserId();
        } else if (attribute.equals("jansStatus")) {
            value = user.getStatus() != null ? user.getStatus().getValue() : null;
        } else {
            value = user.getAttribute(attribute, true, false);
        }

        if (value == null) {
            UserService userService = CdiUtil.bean(UserService.class);
            CustomObjectAttribute customAttr = userService.getCustomAttribute(user, attribute);
            if (customAttr != null) {
                value = customAttr.getValue();
            }
        }

        return value == null ? null : value.toString();
    }

    private static User getUser(String attributeName, String value) {
        UserService userService = CdiUtil.bean(UserService.class);
        return userService.getUserByAttribute(attributeName, value, true);
    }
}
