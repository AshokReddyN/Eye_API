package com.nayonikaeyecare.api.controllers;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nayonikaeyecare.api.dto.patient.PatientRequest;
import com.nayonikaeyecare.api.dto.patient.PatientResponse;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.services.PatientService;

import jakarta.validation.Valid;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000","http://nayonika-user-management-dev-1511095685.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-qa-580028363.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-stg-1382154925.ap-south-1.elb.amazonaws.com",
"https://d1vkdavcz76wk9.cloudfront.net","https://d1ly0bgal3oowh.cloudfront.net","https://dxsbwamx9jelm.cloudfront.net","https://d13hs8y0241ipp.cloudfront.net"})
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping("/addPatient")
    public ResponseEntity<PatientResponse> createPatient(@RequestBody PatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.createPatient(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientsById(@PathVariable String id) {
        try {
            ObjectId objectId = convertToObjectId(id);
            return ResponseEntity.ok(patientService.getPatientById(objectId.toHexString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/getAllPatients")
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/ambassador/{id}")
    public ResponseEntity<List<PatientResponse>> getPatientsByAmbassadorId(@PathVariable String id) {
        return ResponseEntity.ok(patientService.getPatientsByAmbassadorId(id));
    }

    // @PostMapping("/createReferral/{id}")
    // public ResponseEntity<String> createReferalForPatient(@PathVariable String id,
    //                                                       @RequestBody ReferralRequest request) {
    //     try {
    //         ObjectId objectId = convertToObjectId(id);
    //         patientService.createReferalForPatient(objectId.toHexString(), request);
    //         return ResponseEntity.ok("Referral created successfully for the patient");
    //     } catch (IllegalArgumentException e) {
    //         return ResponseEntity.badRequest().body("Invalid ObjectId: " + id);
    //     }
    // }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable String id,
            @RequestBody PatientRequest patientRequest) {
        try {
            ObjectId objectId = convertToObjectId(id);
            PatientResponse updatedPatient = patientService.updatePatient(objectId.toHexString(), patientRequest);
            return ResponseEntity.ok(updatedPatient);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable String id) {
        try {
            ObjectId objectId = convertToObjectId(id);
            patientService.deletePatient(objectId.toHexString());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Referral>> getPatientReports(@RequestParam String id,
                                                            @RequestParam(required = false) String hospitalId) {
        try {
            ObjectId objectId = convertToObjectId(id);
            List<Referral> reports = patientService.getPatientReports(objectId.toHexString(), hospitalId);

            if (reports.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(reports);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<PatientResponse>> getPaginatedPatients(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ambassadorId,
            Pageable pageable) {

        return ResponseEntity.ok(patientService.filterPatients(state, city, name, ambassadorId, pageable));
    }

    /**
     * Utility method to safely convert String to ObjectId.
     */
    private ObjectId convertToObjectId(String id) {
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ObjectId format: " + id);
        }
    }
}
