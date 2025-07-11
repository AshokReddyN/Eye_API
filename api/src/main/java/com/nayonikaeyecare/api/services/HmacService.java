package com.nayonikaeyecare.api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class HmacService {

    private static final Logger logger = LoggerFactory.getLogger(HmacService.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKeySpec;

    public HmacService(@Value("${app.security.hmac-secret-key}") String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) {
            logger.error("HMAC secret key is not configured. Please set app.security.hmac-secret-key.");
            throw new IllegalArgumentException("HMAC secret key is not configured.");
        }
        // Ensure the key is of a reasonable length for HMACSHA256, though the constructor will use it as is.
        // For production, ensure this key is strong and securely managed.
        if ("your-super-secret-hmac-key-change-me-in-prod".equals(secretKey)) {
            logger.warn("Default HMAC secret key is being used. THIS IS NOT SECURE FOR PRODUCTION. Please change app.security.hmac-secret-key.");
        }
        this.secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    public String generateHmac(String data) {
        if (data == null) {
            return null; // Or throw IllegalArgumentException based on desired behavior
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException e) {
            // This should not happen if HmacSHA256 is a standard algorithm
            logger.error("HmacSHA256 algorithm not found", e);
            throw new RuntimeException("HmacSHA256 algorithm not found", e);
        } catch (InvalidKeyException e) {
            // This might happen if the key is somehow invalid for the algorithm
            logger.error("Invalid HMAC key", e);
            throw new RuntimeException("Invalid HMAC key", e);
        }
    }
}
