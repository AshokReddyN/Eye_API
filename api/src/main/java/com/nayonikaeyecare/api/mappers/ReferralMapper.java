package com.nayonikaeyecare.api.mappers;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.nayonikaeyecare.api.dto.referral.ReferralRequest;
import com.nayonikaeyecare.api.dto.referral.ReferralResponse;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.entities.ServiceType;
import com.nayonikaeyecare.api.entities.Status;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Component
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

    public ReferralResponse toResponse(Referral referral) {
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
                referral.getAmbassadorId().toString(),
                referral.getServices(),
                referral.getTreatment(),
                referral.getRightEye(),
                referral.getLeftEye(),
                // referral.getRemarks(),
                referral.getCreatedAt(),
                referral.getUpdatedAt(),
                referral.getIsSpectacleRequested(),
                referral.getSpectacleRequestedOn(),
                referral.getHospitalCode()
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
