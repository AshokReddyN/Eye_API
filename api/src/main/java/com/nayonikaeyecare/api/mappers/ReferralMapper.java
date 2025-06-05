package com.nayonikaeyecare.api.mappers;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.nayonikaeyecare.api.dto.referral.ReferralRequest;
import com.nayonikaeyecare.api.dto.referral.ReferralResponse;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.entities.ServiceType;
import com.nayonikaeyecare.api.entities.Status;
// import com.nayonikaeyecare.api.entities.VisionAmbassador; // Removed
import com.nayonikaeyecare.api.entities.user.User; // Added
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j; // Added

@Component
@Slf4j // Added
public class ReferralMapper {

    public Referral toEntity(ReferralRequest request) {
        return Referral.builder()
                .status(request.status())
                .patientId(new ObjectId(request.patientId()))
                .patientName(request.patientName())
                .ageRange(request.ageRange())
                .gender(request.gender())
                .hospitalName(request.hospitalName())
                .city(request.city())
                .state(request.state())
                .guardianContact(request.guardianContact())
                .hospitalId(new ObjectId(request.hospitalId()))
                .ambassadorId(new ObjectId(request.ambassadorId()))
                .services(request.services())
                .treatment(request.treatment())
                .rightEye(request.rightEye())
                .leftEye(request.leftEye())
                // .remarks(request.remarks())
                .createdAt(request.createdAt())
                .updatedAt(request.updatedAt())
                .isSpectacleRequested(request.isSpectacleRequested())
                .spectacleRequestedOn(request.spectacleRequestedOn())
                .hospitalCode(request.hospitalCode())
                .build();
    }

    public ReferralResponse toResponse(Referral referral, User user) { // Signature changed to User
        log.info("ReferralMapper.toResponse called for Referral ID: {}", referral.getId());
        String ambassadorName = null;
        String ambassadorPhoneNumber = null;
        String ambassadorEmail = null;

        if (user != null) {
            log.info("Mapping with User: ID={}, FirstName={}, LastName={}, Phone={}, Email={}", 
                     user.getId(), user.getFirstName(), user.getLastName(), user.getPhoneNumber(), user.getEmail());
            
            // Combine firstName and lastName for ambassadorName, handling potential nulls
            String firstName = user.getFirstName();
            String lastName = user.getLastName();
            if (firstName != null && lastName != null) {
                ambassadorName = firstName + " " + lastName;
            } else if (firstName != null) {
                ambassadorName = firstName;
            } else if (lastName != null) {
                ambassadorName = lastName;
            }
            
            ambassadorPhoneNumber = user.getPhoneNumber();
            ambassadorEmail = user.getEmail();
        } else {
            log.info("Mapping with NULL User object.");
        }

        return new ReferralResponse(
                referral.getId().toString(),
                referral.getStatus(),
                referral.getPatientId().toString(),
                referral.getPatientName(),
                referral.getAgeRange(),
                referral.getGender(),
                referral.getHospitalName(),
                referral.getCity(),
                referral.getState(),
                referral.getGuardianContact(),
                referral.getHospitalId().toString(),
                referral.getAmbassadorId() != null ? referral.getAmbassadorId().toString() : null, // Handle null ambassadorId
                referral.getServices(),
                referral.getTreatment(),
                referral.getRightEye(),
                referral.getLeftEye(),
                // referral.getRemarks(),
                referral.getCreatedAt(),
                referral.getUpdatedAt(),
                referral.getIsSpectacleRequested(),
                referral.getSpectacleRequestedOn(),
                referral.getHospitalCode(),
                ambassadorName,
                ambassadorPhoneNumber,
                ambassadorEmail // New field
        );
    }

    public Referral updateEntity(Referral existingReferral, ReferralRequest request) {
        existingReferral.setStatus(request.status());
        existingReferral.setPatientId(new ObjectId(request.patientId()));
        existingReferral.setPatientName(request.patientName());
        existingReferral.setAgeRange(request.ageRange());
        existingReferral.setGender(request.gender());
        existingReferral.setHospitalName(request.hospitalName());
        existingReferral.setCity(request.city());
        existingReferral.setState(request.state());
        existingReferral.setGuardianContact(request.guardianContact());
        existingReferral.setHospitalId(new ObjectId(request.hospitalId()));
        existingReferral.setAmbassadorId(new ObjectId(request.ambassadorId()));
        existingReferral.setServices(request.services());
        existingReferral.setTreatment(request.treatment());
        existingReferral.setRightEye(request.rightEye());
        existingReferral.setLeftEye(request.leftEye());
        // existingReferral.setRemarks(request.remarks());
        existingReferral.setUpdatedAt(new Date());
        existingReferral.setIsSpectacleRequested(request.isSpectacleRequested());
        existingReferral.setSpectacleRequestedOn(request.spectacleRequestedOn());
        existingReferral.setHospitalCode(request.hospitalCode());
        return existingReferral;
    }
}
