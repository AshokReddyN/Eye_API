package com.nayonikaeyecare.api.controllers;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.nayonikaeyecare.api.dto.hospital.HospitalRequest;
import com.nayonikaeyecare.api.dto.hospital.HospitalResponse;
import com.nayonikaeyecare.api.services.HospitalService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = {"http://localhost:3000","http://nayonika-user-management-dev-1511095685.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-qa-580028363.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-stg-1382154925.ap-south-1.elb.amazonaws.com",
"https://d1vkdavcz76wk9.cloudfront.net","https://d1ly0bgal3oowh.cloudfront.net","https://dxsbwamx9jelm.cloudfront.net","https://d13hs8y0241ipp.cloudfront.net"})

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalResponse createHospital(@Valid @RequestBody HospitalRequest hospitalRequest) {
        return hospitalService.createHospital(hospitalRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<HospitalResponse> getAllHospitals() {
        return hospitalService.getAllHospitals();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalResponse> getHospitalById(@PathVariable String id) {
        try {
            ObjectId objectId = convertToObjectId(id);
            HospitalResponse hospital = hospitalService.getHospitalById(objectId.toHexString());
            return ResponseEntity.ok(hospital);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHospital(@PathVariable String id) {
        try {
            ObjectId objectId = convertToObjectId(id);
            hospitalService.deleteHospitalById(objectId.toHexString());
            return ResponseEntity.ok("Hospital deleted successfully with id: " + id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid ObjectId: " + id);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<HospitalResponse> updateHospital(
            @PathVariable String id,
            @Valid @RequestBody HospitalRequest hospitalRequest) {
        try {
            ObjectId objectId = convertToObjectId(id);
            HospitalResponse updatedHospital = hospitalService.updateHospital(objectId.toHexString(), hospitalRequest);
            return ResponseEntity.ok(updatedHospital);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<HospitalResponse>> getPaginatedHospitals(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) List<String> cities,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String searchString,
            @RequestParam(required = false) List<String> services,
            Pageable pageable) {
        return ResponseEntity.ok(hospitalService.filterHospitals(state, cities, status, searchString,services,pageable));
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<String> bulkUploadHospitals(@Valid @RequestBody List<HospitalRequest> hospitals) {
        try {
            if (hospitals.isEmpty()) {
                return ResponseEntity.badRequest().body("No Hospital data provided");
            }

            int insertedCount = hospitalService.saveAllHospitalsIfHospitalCodeNotExists(hospitals);
            return ResponseEntity.ok("Successfully uploaded and saved " + insertedCount + " hospitals (skipped duplicates)");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid data: " + e.getMessage());
        }
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
