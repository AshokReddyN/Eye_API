package com.nayonikaeyecare.api.controllers;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.nayonikaeyecare.api.dto.referral.BulkReferralUpdateRequest;
import com.nayonikaeyecare.api.dto.referral.BulkReferralUpdateResponse;
import com.nayonikaeyecare.api.dto.referral.ReferralRequest;
import com.nayonikaeyecare.api.dto.referral.ReferralResponse;
import com.nayonikaeyecare.api.services.ReferralService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = {"http://localhost:3000","http://nayonika-user-management-dev-1511095685.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-qa-580028363.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-stg-1382154925.ap-south-1.elb.amazonaws.com",
"https://app-dev.nayonikaeyecare.com","https://app-qa.nayonikaeyecare.com","https://app-stg.nayonikaeyecare.com","https://app.nayonikaeyecare.com"})
@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor

public class ReferralController {

    private final ReferralService referralService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReferralResponse createReferral(@Valid @RequestBody ReferralRequest referralRequest) {
        return referralService.createReferral(referralRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReferralResponse> getAllReferrals() {
        return referralService.getAllReferrals();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReferralResponse> getReferralById(@PathVariable String id) {
        try {
            ObjectId objectId = convertToObjectId(id);
            ReferralResponse referral = referralService.getReferralById(objectId.toHexString());
            return ResponseEntity.ok(referral);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/ambassador/{id}")
    public ResponseEntity<List<ReferralResponse>> getReferralsByAmbassadorId(@PathVariable String id) {
        return ResponseEntity.ok(referralService.getReferralsByAmbassadorId(id));
    }

    @GetMapping("/hospital/{id}")
    public ResponseEntity<List<ReferralResponse>> getReferralsByHospitalId(@PathVariable String id) {
        return ResponseEntity.ok(referralService.getReferralsByHospitalId(id));
    }

    @GetMapping("/hospital/{id}/paginated")
    public ResponseEntity<Page<ReferralResponse>> getReferralsByHospitalIdPaginated(
            @PathVariable String id,
            Pageable pageable) {
        return ResponseEntity.ok(referralService.getReferralsByHospitalIdPaginated(id, pageable));
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<ReferralResponse>> getReferralsByPatientId(@PathVariable String id) {
        return ResponseEntity.ok(referralService.getReferralsByPatientId(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ReferralResponse>> getPaginatedReferrals(
            @RequestParam(required = false) ObjectId ambassadorId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String name, // Keep for backward compatibility
            @RequestParam(required = false) String searchString, // Add this for your frontend
            Pageable pageable) {
        return ResponseEntity.ok(referralService.filterReferrals(
                ambassadorId, state, city, status, name, searchString, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReferral(@PathVariable String id) {
        try {
            ObjectId objectId = convertToObjectId(id);
            referralService.deleteReferralById(objectId.toHexString());
            return ResponseEntity.ok("Referral deleted successfully with id: " + id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid ObjectId: " + id);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReferralResponse> updateReferral(
            @PathVariable String id,
            @Valid @RequestBody ReferralRequest referralRequest) {
        try {
            ObjectId objectId = convertToObjectId(id);
            ReferralResponse updatedReferral = referralService.updateReferral(objectId.toHexString(), referralRequest);
            return ResponseEntity.ok(updatedReferral);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<BulkReferralUpdateResponse> bulkUpdateReferrals(
            @Valid @RequestBody List<BulkReferralUpdateRequest> bulkRequest) {
        BulkReferralUpdateResponse response = referralService.bulkUpdateReferrals(bulkRequest);
        return ResponseEntity.ok(response);
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
