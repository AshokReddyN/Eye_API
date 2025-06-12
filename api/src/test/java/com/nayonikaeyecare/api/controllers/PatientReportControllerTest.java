package com.nayonikaeyecare.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.bulk.BulkWriteResult;
import com.nayonikaeyecare.api.dto.PatientReportRequestDto;
import com.nayonikaeyecare.api.entities.Status;
import com.nayonikaeyecare.api.services.PatientService;
import com.nayonikaeyecare.api.services.ReportJsonImportService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientReportControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ReportJsonImportService reportJsonImportService;

        @MockitoBean
        private PatientService patientService;

        @Autowired
        private ObjectMapper objectMapper;

        private List<PatientReportRequestDto> createTestReports() {
                PatientReportRequestDto report1 = new PatientReportRequestDto();
                report1.setId("1");
                report1.setChildName("vignesh kumar");
                report1.setSex("m");
                report1.setAge("5-10");
                report1.setCityName("puducherry");
                report1.setTreatment("Refraction");
                report1.setRightSph("0");
                report1.setRightCyl(-0.75);
                report1.setRightAxis(180);
                report1.setLeftSph("0");
                report1.setLeftCyl(-0.5);
                report1.setLeftAxis(180);

                PatientReportRequestDto report2 = new PatientReportRequestDto();
                report2.setId("2");
                report2.setChildName("Kirubashini");
                report2.setSex("F");
                report2.setAge("5-10");
                report2.setCityName("Mugaiyur");
                report2.setPhoneNo(9003396168L);
                report2.setTreatment("Surgery");
                report2.setRightSph("0");
                report2.setRightCyl(-4.5);
                report2.setRightAxis(10);
                report2.setLeftSph("0");
                report2.setLeftCyl(-4.5);
                report2.setLeftAxis(170);

                return Arrays.asList(report1, report2);
        }

        @Test
        void testImportJsonSuccess() throws Exception {
                // Arrange
                List<PatientReportRequestDto> reports = createTestReports();

                BulkWriteResult mockResult = mock(BulkWriteResult.class);
                when(mockResult.getModifiedCount()).thenReturn(2);
                when(mockResult.getMatchedCount()).thenReturn(2);

                when(reportJsonImportService.importReportsFromJson(anyList()))
                                .thenReturn(CompletableFuture.completedFuture(mockResult));

                // Act & Assert
                mockMvc.perform(post("/api/reports/upload")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reports)))
                                .andExpect(status().isOk())
                                .andExpect(content().string("JSON import completed. Modified: 2, Matched: 2"));

                verify(reportJsonImportService).importReportsFromJson(anyList());
        }

        @Test
        void testImportJson_WithEmptyList_ShouldReturnBadRequest() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/reports/upload")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[]"))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Reports cannot be empty"));

                verify(reportJsonImportService, never()).importReportsFromJson(anyList());
        }

        @Test
        void testImportJson_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/reports/upload")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("invalid-json"))
                                .andExpect(status().isInternalServerError());

                verify(reportJsonImportService, never()).importReportsFromJson(anyList());
        }

        @Test
        void testImportJson_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
                // Arrange
                List<PatientReportRequestDto> reports = Collections.singletonList(new PatientReportRequestDto());

                // Act & Assert
                mockMvc.perform(post("/api/reports/upload")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reports)))
                                .andExpect(status().isInternalServerError());

        }

        @Test
        void testImportJson_WithServiceError_ShouldReturnInternalServerError() throws Exception {
                // Arrange
                List<PatientReportRequestDto> reports = createTestReports();

                when(reportJsonImportService.importReportsFromJson(anyList()))
                                .thenReturn(CompletableFuture.failedFuture(
                                                new RuntimeException("Database error")));

                // Act & Assert
                mockMvc.perform(post("/api/reports/upload")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reports)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(content().string(
                                                "Failed to import JSON: java.lang.RuntimeException: Database error"));
        }

        @Test
        void updatePatientStatus_Success() throws Exception {
                // Arrange
                Status newStatus = Status.ACTIVE;
                List<String> reportIds = Arrays.asList("report1", "report2");

                doNothing().when(patientService).updateStatusForPatients(reportIds, newStatus);

                // Act & Assert
                mockMvc.perform(put("/api/reports/updateStatus/{status}", newStatus)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(reportIds)))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Reports added successfully"));

                verify(patientService, times(1)).updateStatusForPatients(reportIds, newStatus);
        }

        @Test
        void updatePatientStatus_WithInvalidStatus_ShouldReturnBadRequest() throws Exception {
                // Arrange
                List<String> reportIds = Arrays.asList("report1", "report2");

                // Act & Assert
                mockMvc.perform(put("/api/reports/updateStatus/INVALID_STATUS")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(reportIds)))
                                .andExpect(status().isInternalServerError());

                verify(patientService, never()).updateStatusForPatients(any(), any());
        }

        @Test
        void updatePatientStatus_WithEmptyIds_ShouldReturnBadRequest() throws Exception {
                // Arrange
                List<String> emptyIds = Collections.emptyList();

                // Act & Assert
                mockMvc.perform(put("/api/reports/updateStatus/{status}", Status.ACTIVE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(emptyIds)))
                                .andExpect(status().isBadRequest());

                verify(patientService, never()).updateStatusForPatients(any(), any());
        }

        @Test
        void updatePatientStatus_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
                // Arrange
                Status newStatus = Status.ACTIVE;
                List<String> reportIds = Arrays.asList("report1", "report2");

                doThrow(new RuntimeException("Database error"))
                                .when(patientService).updateStatusForPatients(reportIds, newStatus);

                // Act & Assert
                mockMvc.perform(put("/api/reports/updateStatus/{status}", newStatus)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(reportIds)))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        void updatePatientStatus_WithMissingStatus() throws Exception {
                // Arrange
                List<String> reportIds = Arrays.asList("report1", "report2");

                // Act & Assert
                mockMvc.perform(put("/api/reports/updateStatus/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(reportIds)))
                                .andExpect(status().isNotFound());

                verify(patientService, never()).updateStatusForPatients(any(), any());
        }

        @Test
        void updatePatientStatus_WithNullIds() throws Exception {
                // Act & Assert
                mockMvc.perform(put("/api/reports/updateStatus/{status}", Status.ACTIVE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("null"))
                                .andExpect(status().isInternalServerError());

                verify(patientService, never()).updateStatusForPatients(any(), any());
        }

        @Test
        void updatePatientStatus_WithInvalidJson() throws Exception {
                // Act & Assert
                mockMvc.perform(put("/api/reports/updateStatus/{status}", Status.ACTIVE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("invalid-json"))
                                .andExpect(status().isInternalServerError());

                verify(patientService, never()).updateStatusForPatients(any(), any());
        }
}
