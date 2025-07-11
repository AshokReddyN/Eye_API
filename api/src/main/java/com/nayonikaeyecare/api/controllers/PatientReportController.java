package com.nayonikaeyecare.api.controllers;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mongodb.bulk.BulkWriteResult;
import com.nayonikaeyecare.api.dto.PatientReportRequestDto;
import com.nayonikaeyecare.api.entities.Status;
import com.nayonikaeyecare.api.services.PatientService;
import com.nayonikaeyecare.api.services.ReportJsonImportService;

@CrossOrigin(origins = {"http://localhost:3000","http://nayonika-user-management-dev-1511095685.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-qa-580028363.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-stg-1382154925.ap-south-1.elb.amazonaws.com",
"https://app-dev.nayonikaeyecare.com","https://app-qa.nayonikaeyecare.com","https://app-stg.nayonikaeyecare.com","https://app.nayonikaeyecare.com"})
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class PatientReportController {

    private final PatientService patientService;

    private final ReportJsonImportService reportJsonImportService;

    @PostMapping("/upload")
    public ResponseEntity<String> importReportsJson(@RequestBody List<PatientReportRequestDto> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return ResponseEntity.badRequest().body("Reports cannot be empty");
            }
            CompletableFuture<BulkWriteResult> future = reportJsonImportService.importReportsFromJson(reports);
            BulkWriteResult result = future.get();

            return ResponseEntity.ok(String.format("JSON import completed. Modified: %d, Matched: %d",
                    result.getModifiedCount(), result.getMatchedCount()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to import JSON: " + e.getMessage());
        }
    }

    @PutMapping("/updateStatus/{status}")
    public ResponseEntity<String> updatePatientStatus(
            @PathVariable Status status,
            @RequestBody List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body("Report IDs cannot be empty");
        }

        try {
            patientService.updateStatusForPatients(ids, status);
            return ResponseEntity.ok("Reports added successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to update status: " + e.getMessage());
        }
    }
}
