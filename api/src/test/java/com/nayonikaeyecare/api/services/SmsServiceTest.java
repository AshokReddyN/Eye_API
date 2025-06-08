package com.nayonikaeyecare.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class SmsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SmsService smsService;

    private final String testApiKey = "TEST_API_KEY";
    private final String phoneNumber = "1234567890";
    private final String otpValue = "1234";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Manually inject mock RestTemplate and set apiKey via reflection
        // as @InjectMocks creates the service but we need to ensure RestTemplate is the mock
        // and apiKey is set before any test method runs.
        // @InjectMocks handles the restTemplate injection if the constructor is used.
        // For apiKey, @Value field needs to be set.
        smsService = new SmsService(restTemplate); // Ensure constructor injection for RestTemplate
        ReflectionTestUtils.setField(smsService, "apiKey", testApiKey);
    }

    private URI buildExpectedUri(String apiKey, String route, String variablesValues, String numbers, String flash) {
        return UriComponentsBuilder.fromHttpUrl("https://www.fast2sms.com/dev/bulkV2")
                .queryParam("authorization", apiKey)
                .queryParam("route", route)
                .queryParam("variables_values", variablesValues)
                .queryParam("numbers", numbers)
                .queryParam("flash", flash)
                .build().toUri();
    }

    @Test
    void testSendOtp_Success() {
        URI expectedUri = buildExpectedUri(testApiKey, "otp", otpValue, phoneNumber, "0");
        String successResponse = "{\"return\":true,\"request_id\":\"some_id\",\"message\":[\"SMS sent successfully\"]}";

        when(restTemplate.getForObject(eq(expectedUri.toString()), eq(String.class))).thenReturn(successResponse);

        smsService.sendOtp(phoneNumber, otpValue);

        verify(restTemplate, times(1)).getForObject(eq(expectedUri.toString()), eq(String.class));
    }

    @Test
    void testSendOtp_ApiError() {
        URI expectedUri = buildExpectedUri(testApiKey, "otp", otpValue, phoneNumber, "0");
        String errorResponse = "{\"return\":false,\"status_code\":400,\"message\":\"Invalid API key\"}";

        when(restTemplate.getForObject(eq(expectedUri.toString()), eq(String.class))).thenReturn(errorResponse);

        smsService.sendOtp(phoneNumber, otpValue);

        verify(restTemplate, times(1)).getForObject(eq(expectedUri.toString()), eq(String.class));
        // Further assertions could involve checking log output if a mock logger was injected and verified
    }

    @Test
    void testSendOtp_NetworkError() {
        URI expectedUri = buildExpectedUri(testApiKey, "otp", otpValue, phoneNumber, "0");

        when(restTemplate.getForObject(eq(expectedUri.toString()), eq(String.class)))
                .thenThrow(new RestClientException("Network error"));

        smsService.sendOtp(phoneNumber, otpValue);

        verify(restTemplate, times(1)).getForObject(eq(expectedUri.toString()), eq(String.class));
        // Assert that the exception is caught and logged (implicitly tested by not throwing RestClientException)
        // Further assertions could involve checking log output
    }
}
