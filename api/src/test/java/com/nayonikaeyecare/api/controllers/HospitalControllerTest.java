package com.nayonikaeyecare.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayonikaeyecare.api.dto.hospital.HospitalRequest;
import com.nayonikaeyecare.api.dto.hospital.HospitalResponse;
import com.nayonikaeyecare.api.entities.Address;
import com.nayonikaeyecare.api.services.HospitalService;
import com.nayonikaeyecare.api.exceptions.ResourceMissingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HospitalController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
@ActiveProfiles("test")
class HospitalControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private HospitalService hospitalService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void createHospital_ShouldReturnCreatedHospital() throws Exception {
                // Arrange
                HospitalRequest request = HospitalRequest.builder()
                                .name("Test Hospital")
                                .hospitalCode("HOSP123")
                                .status(true)
                                .services(Arrays.asList("Threatment"))
                                .coordinator("John Doe")
                                .coordinator_phonenumber("9845678765")
                                .coordinator_email("sss@gmail.com")
                                .googleLink("https://maps.app.goo.gl/6gT9bpxnuWQU9aAJ6")
                                .registration_date("2023-10-01")
                                .address(Address.builder()
                                                .address1("Test address1")
                                                .address2("Test address2")
                                                .city("Test City")
                                                .state("Test State")
                                                .pincode("560987").build())
                                .build();

                HospitalResponse response = HospitalResponse.builder()
                                .id("1")
                                .name("Test Hospital")
                                .hospitalCode("HOSP123")
                                .status(true)
                                .services(Arrays.asList("Threatment"))
                                .coordinator("John Doe")
                                .coordinator_phonenumber("9845678765")
                                .coordinator_email("sss@gmail.com")
                                .googleLink("https://maps.app.goo.gl/6gT9bpxnuWQU9aAJ6")
                                .address(Address.builder()
                                                .address1("Test address1")
                                                .address2("Test address2")
                                                .city("Test City")
                                                .state("Test State")
                                                .pincode("560987").build())
                                .registration_date("2023-10-01")
                                .build();

                when(hospitalService.createHospital(any(HospitalRequest.class)))
                                .thenReturn(response);

                // Act & Assert
                mockMvc.perform(post("/api/hospital")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value("1"))
                                .andExpect(jsonPath("$.name").value("Test Hospital"));
        }

        @Test
        void getAllHospitals_ShouldReturnList() throws Exception {
                // Arrange
                List<HospitalResponse> hospitals = Arrays.asList(
                                HospitalResponse.builder().id("1").name("Hospital 1").build(),
                                HospitalResponse.builder().id("2").name("Hospital 2").build());

                when(hospitalService.getAllHospitals()).thenReturn(hospitals);

                // Act & Assert
                mockMvc.perform(get("/api/hospital"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value("1"))
                                .andExpect(jsonPath("$[1].id").value("2"));
        }

        @Test
        void getHospitalById_ShouldReturnHospital() throws Exception {
                // Arrange
                String hospitalId = "1";
                HospitalResponse response = HospitalResponse.builder()
                                .id(hospitalId)
                                .name("Test Hospital")
                                .build();

                when(hospitalService.getHospitalById(hospitalId)).thenReturn(response);

                // Act & Assert
                mockMvc.perform(get("/api/hospital/{id}", hospitalId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(hospitalId));
        }

        @Test
        void getHospitalById_WhenNotFound_ShouldReturn404() throws Exception {
                // Arrange
                String hospitalId = "999";
                when(hospitalService.getHospitalById(hospitalId))
                                .thenThrow(new ResourceMissingException("Hospital not found"));

                // Act & Assert
                mockMvc.perform(get("/api/hospital/{id}", hospitalId))
                                .andExpect(status().isNotFound());
        }

        @Test
        void deleteHospital_ShouldReturnSuccess() throws Exception {
                // Arrange
                String hospitalId = "1";
                doNothing().when(hospitalService).deleteHospitalById(hospitalId);

                // Act & Assert
                mockMvc.perform(delete("/api/hospital/{id}", hospitalId))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Hospital deleted successfully with id: " + hospitalId));

                verify(hospitalService, times(1)).deleteHospitalById(hospitalId);
        }

        @Test
        void updateHospital_ShouldReturnUpdatedHospital() throws Exception {
                // Arrange
                String hospitalId = "1";
                HospitalRequest request = HospitalRequest.builder()
                                // .id("1")
                                .name("Test Hospital")
                                .hospitalCode("HOSP123")
                                .status(true)
                                .services(Arrays.asList("Threatment"))
                                .coordinator("John Doe")
                                .coordinator_phonenumber("9845678765")
                                .coordinator_email("sss@gmail.com")
                                .googleLink("https://maps.app.goo.gl/6gT9bpxnuWQU9aAJ6")
                                .registration_date("2023-10-01")
                                .address(Address.builder()
                                                .address1("Test address1")
                                                .address2("Test address2")
                                                .city("Test City")
                                                .state("Test State")
                                                .pincode("560987").build())
                                .build();

                HospitalResponse response = HospitalResponse.builder()
                                .id(hospitalId)
                                .id("1")
                                .name("Updated Hospital")
                                .status(true)
                                .services(Arrays.asList("Threatment"))
                                .coordinator("John Doe")
                                .googleLink("https://maps.app.goo.gl/6gT9bpxnuWQU9aAJ6")
                                .address(Address.builder()
                                                .address1("Test address1")
                                                .address2("Test address2")
                                                .city("Test City")
                                                .state("Test State")
                                                .pincode("560987").build())
                                .build();

                when(hospitalService.updateHospital(eq(hospitalId), any(HospitalRequest.class)))
                                .thenReturn(response);

                // Act & Assert
                mockMvc.perform(put("/api/hospital/{id}", hospitalId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(hospitalId))
                                .andExpect(jsonPath("$.name").value("Updated Hospital"));
        }

        @Test
        void createHospital_WithInvalidData_ShouldReturnBadRequest() throws Exception {
                // Arrange
                HospitalRequest request = HospitalRequest.builder().build(); // Empty request

                // Act & Assert
                mockMvc.perform(post("/api/hospital")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }
}