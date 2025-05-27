package com.nayonikaeyecare.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorRequest;
import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;
import com.nayonikaeyecare.api.exceptions.ResourceMissingException;
import com.nayonikaeyecare.api.services.VisionAmbassadorService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VisionAmbassadorControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private VisionAmbassadorService visionAmbassadorService;

        @Autowired
        private ObjectMapper objectMapper;

        private VisionAmbassadorRequest testRequest;
        private VisionAmbassadorResponse testResponse;

        @BeforeEach
        void setUp() {
                testRequest = VisionAmbassadorRequest.builder()
                                .name("John Doe")
                                .phoneNumber("+91-9876543210")
                                .city("Bangalore")
                                .state("Karnataka")
                                .status(true)
                                .language("EN")
                                .build();

                testResponse = VisionAmbassadorResponse.builder()
                                .id("test-id")
                                .name("John Doe")
                                .phoneNumber("+91-9876543210")
                                .city("Bangalore")
                                .state("Karnataka")
                                .status(true)
                                .language("EN")
                                .patientCount(0)
                                .createdAt(new Date())
                                .updatedAt(new Date())
                                .build();
        }

        @Test
        void createVisionAmbassador_WithValidData_ShouldReturnCreated() throws Exception {
                // Arrange
                doNothing().when(visionAmbassadorService).createVisionAmbassador(any());

                // Act & Assert
                mockMvc.perform(post("/api/visionAmbassador/addVisionAmbassador")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testRequest)))
                                .andExpect(status().isCreated());

                verify(visionAmbassadorService).createVisionAmbassador(any());
        }

        @Test
        void getAllVisionAmbassadors_ShouldReturnList() throws Exception {
                // Arrange
                List<VisionAmbassadorResponse> ambassadors = Arrays.asList(testResponse);
                when(visionAmbassadorService.getAllVisionAmbassadors()).thenReturn(ambassadors);

                // Act & Assert
                mockMvc.perform(get("/api/visionAmbassador/getAllVisionAmbassadors")
                                .with(user("test-user").roles("USER")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value(testResponse.getId()))
                                .andExpect(jsonPath("$[0].name").value(testResponse.getName()));
        }

        @Test
        void getVisionAmbassadorById_WhenExists_ShouldReturnAmbassador() throws Exception {
                // Arrange
                when(visionAmbassadorService.getVisionAmbassadorById("test-id"))
                                .thenReturn(testResponse);

                // Act & Assert
                mockMvc.perform(get("/api/visionAmbassador/{id}", "test-id")
                                .with(user("test-user").roles("USER")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(testResponse.getId()))
                                .andExpect(jsonPath("$.name").value(testResponse.getName()));
        }

        @Test
        void getVisionAmbassadorById_WhenNotFound_ShouldReturn404() throws Exception {
                // Arrange
                when(visionAmbassadorService.getVisionAmbassadorById("invalid-id"))
                                .thenThrow(new ResourceMissingException("Ambassador not found"));

                // Act & Assert
                mockMvc.perform(get("/api/visionAmbassador/{id}", "invalid-id")
                                .with(user("test-user").roles("USER")))
                                .andExpect(status().isNotFound());
        }

        @Test
        void updateVisionAmbassador_WithValidData_ShouldReturnUpdated() throws Exception {
                // Arrange
                when(visionAmbassadorService.updateVisionAmbassador(eq("test-id"), any()))
                                .thenReturn(testResponse);

                // Act & Assert
                mockMvc.perform(put("/api/visionAmbassador/{id}", "test-id")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(testResponse.getId()));
        }

        @Test
        void deleteVisionAmbassador_ShouldReturnNoContent() throws Exception {
                // Arrange
                doNothing().when(visionAmbassadorService).deleteVisionAmbassador("test-id");

                // Act & Assert
                mockMvc.perform(delete("/api/visionAmbassador/{id}", "test-id"))
                                .andExpect(status().isNoContent());

                verify(visionAmbassadorService).deleteVisionAmbassador("test-id");
        }

        @Test
        void filterVisionAmbassadors_ShouldReturnPagedResults() throws Exception {
                // Arrange
                Page<VisionAmbassadorResponse> pagedResponse = new PageImpl<>(
                                Arrays.asList(testResponse),
                                PageRequest.of(0, 10),
                                1);

                when(visionAmbassadorService.filterVisionAmbassador(
                                eq("Karnataka"), eq("Bangalore"), any()))
                                .thenReturn(pagedResponse);

                // Act & Assert
                mockMvc.perform(get("/api/visionAmbassador/filter")
                                .param("state", "Karnataka")
                                .param("city", "Bangalore")
                                .param("page", "0")
                                .param("size", "10")
                                .with(user("test-user").roles("USER")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(testResponse.getId()))
                                .andExpect(jsonPath("$.content[0].state").value(testResponse.getState()))
                                .andExpect(jsonPath("$.totalElements").value(1));
        }
}