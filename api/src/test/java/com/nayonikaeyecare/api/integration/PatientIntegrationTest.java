package com.nayonikaeyecare.api.integration;

import com.nayonikaeyecare.api.config.TestConfig;
import com.nayonikaeyecare.api.dto.patient.PatientRequest;
import com.nayonikaeyecare.api.entities.Gender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
class PatientIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

   // @Test
    void shouldCreateAndRetrievePatient() {
        // Arrange
        PatientRequest request = PatientRequest.builder()
                .name("Integration Test Patient")
                .ageRange("5-10")
                .gender(Gender.FEMALE)
                .phone("9876543210")
                .build();

        // Act
        ResponseEntity<?> createResponse = restTemplate.postForEntity(
                "/api/patient/addPatient",
                request,
                Object.class);

        // Assert
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String locationUrl = createResponse.getHeaders().getLocation().toString();

        ResponseEntity<?> getResponse = restTemplate.getForEntity(
                locationUrl,
                Object.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}