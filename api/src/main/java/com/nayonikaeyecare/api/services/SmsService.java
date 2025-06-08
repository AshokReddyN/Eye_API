package com.nayonikaeyecare.api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${fast2sms.apikey}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public SmsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendOtp(String phoneNumber, String otpValue) {
        String apiUrl = "https://www.fast2sms.com/dev/bulkV2";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("authorization", apiKey)
                .queryParam("route", "otp")
                .queryParam("variables_values", otpValue)
                .queryParam("numbers", phoneNumber)
                .queryParam("flash", "0");

        String uri = builder.toUriString();
        // Mask API key for logging
        String loggedUri = uri.replaceFirst("authorization=([^&]+)", "authorization=REDACTED");
        logger.info("Calling Fast2SMS API: {}", loggedUri);

        try {
            String response = restTemplate.getForObject(uri, String.class);
            logger.info("Fast2SMS response: {}", response);
        } catch (RestClientException e) {
            logger.error("Error calling Fast2SMS API: {}", e.getMessage());
        }
    }
}
