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
@Builder

public record ReferralRequest(
        Status status,
        String patientId,
        String patientName,
        String ageRange,
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
        // String remarks,
        Date createdAt,
        Date updatedAt,
        Boolean isSpectacleRequested,
        String spectacleRequestedOn,
        String hospitalCode
) {}