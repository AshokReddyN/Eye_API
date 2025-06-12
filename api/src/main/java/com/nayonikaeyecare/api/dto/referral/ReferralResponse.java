package com.nayonikaeyecare.api.dto.referral;

import java.util.Date;
import java.util.List;

import com.nayonikaeyecare.api.entities.EyeDetails;
import com.nayonikaeyecare.api.entities.ServiceType;
import com.nayonikaeyecare.api.entities.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.nayonikaeyecare.api.entities.Hospital;
import com.nayonikaeyecare.api.entities.Patient;

@Builder

public record ReferralResponse(
        String id,
        Status status,
        String patientId,
        String patientName,
        String age,
        String gender,
        String hospitalName,
        String city,
        String state,
        String guardianContact,
        String hospitalId,
        String ambassadorId,
        List<ServiceType> services,
        String treatment,
        EyeDetails rightEye,
        EyeDetails leftEye,
        //String remarks,
        Date createdAt,
        Date updatedAt,
        Boolean isSpectacleRequested,
        String spectacleRequestedOn,
        String hospitalCode,
        String ambassadorName,
        String ambassadorPhoneNumber,
        String ambassadorEmail,
        Hospital hospital,       // New field
        Patient patient // New field for patient details
) {}