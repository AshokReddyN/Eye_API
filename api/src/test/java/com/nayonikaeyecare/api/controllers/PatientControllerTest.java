package com.nayonikaeyecare.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayonikaeyecare.api.dto.patient.PatientRequest;
import com.nayonikaeyecare.api.dto.patient.PatientResponse;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.services.PatientService;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    private PatientRequest testRequest;
    private PatientResponse testResponse;
    private Referral testReferral;

    @BeforeEach
    void setUp() {
        testRequest = PatientRequest.builder()
                .name("John Doe")
                .phone("+91-9876543210")
                .age("5-10")
                .city("Bangalore")
                .state("Karnataka")
                .build();

        testResponse = PatientResponse.builder()
                .id("test-id")
                .name("John Doe")
                .phone("+91-9876543210")
                .age("5-10")
                .city("Bangalore")
                .state("Karnataka")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        // testReferral = Referral.builder()
        //         .id("report-id")
        //         .patientId("test-id")
        //         .hospitalId("hospital-id")
        //         .treatment("Eye Check")
        //         .createdAt(new Date())
        //         .build();
    }

    @Test
    void createPatient_WithValidData_ShouldReturnCreated() throws Exception {
        when(patientService.createPatient(any())).thenReturn(testResponse);

        mockMvc.perform(post("/api/patient/addPatient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testResponse.getId()))
                .andExpect(jsonPath("$.name").value(testResponse.getName()));
    }

    @Test
    void getPatientById_WhenExists_ShouldReturnPatient() throws Exception {
        when(patientService.getPatientById("test-id")).thenReturn(testResponse);

        mockMvc.perform(get("/api/patient/{id}", "test-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testResponse.getId()));
    }

    @Test
    void getAllPatients_ShouldReturnList() throws Exception {
        List<PatientResponse> patients = Arrays.asList(testResponse);
        when(patientService.getAllPatients()).thenReturn(patients);

        mockMvc.perform(get("/api/patient/getAllPatients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testResponse.getId()));
    }

    @Test
    void deletePatient_ShouldReturnNoContent() throws Exception {
        doNothing().when(patientService).deletePatient("test-id");

        mockMvc.perform(delete("/api/patient/{id}", "test-id"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getPatientReports_WhenExists_ShouldReturnReports() throws Exception {
        List<Referral> reports = Arrays.asList(testReferral);
        when(patientService.getPatientReports("test-id", "hospital-id"))
                .thenReturn(reports);

        mockMvc.perform(get("/api/patient/reports")
                .param("id", "test-id")
                .param("hospitalId", "hospital-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testReferral.getId()));
    }

    @Test
    void getPatientReports_WhenEmpty_ShouldReturnNoContent() throws Exception {
        when(patientService.getPatientReports("test-id", "hospital-id"))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/patient/reports")
                .param("id", "test-id")
                .param("hospitalId", "hospital-id"))
                .andExpect(status().isNoContent());
    }

//     @Test
//     void createReferral_ShouldReturnSuccess() throws Exception {
//         ReferralRequest referralRequest = new ReferralRequest();
//         doNothing().when(patientService).createReferalForPatient(eq("test-id"), any());

//         mockMvc.perform(put("/api/patient/createReferral/{id}", "test-id")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(referralRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string("Referral created successfully for the patient"));
//     }

    @Test
    void filterPatients_ShouldReturnPagedResults() throws Exception {
        Page<PatientResponse> pagedResponse = new PageImpl<>(
                Arrays.asList(testResponse),
                PageRequest.of(0, 10),
                1);

        when(patientService.filterPatients(
                eq("Karnataka"), eq("Bangalore"), eq("John"), eq("amb-id"), any()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/patient/filter")
                .param("state", "Karnataka")
                .param("city", "Bangalore")
                .param("name", "John")
                .param("ambassadorId", "amb-id")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testResponse.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}