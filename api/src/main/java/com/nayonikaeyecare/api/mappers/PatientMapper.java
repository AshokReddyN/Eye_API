package com.nayonikaeyecare.api.mappers;

import com.nayonikaeyecare.api.dto.patient.PatientResponse;
import com.nayonikaeyecare.api.entities.Patient;

public class PatientMapper {

    public static PatientResponse mapToPatientResponse(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId()!= null ? patient.getId().toHexString() : null)
                .name(patient.getName())
                .ambassadorId(patient.getAmbassadorId())
                .gender(patient.getGender())
                .ageRange(patient.getAgeRange())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .city(patient.getCity())
                .state(patient.getState())
                .hospitalName(patient.getHospitalName())
                .status(patient.getStatus())
                .referralIds(patient.getReferralIds())
                .guardian(patient.getGuardianContact())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}