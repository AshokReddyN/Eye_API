package com.nayonikaeyecare.api.dto.referral;

import lombok.AllArgsConstructor;
import lombok.Builder; // Added
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Added
public class BulkReferralUpdateRequest {

    private String hospitalName;
    private String hospitalCode;
    private String state;
    private String city;
    private String pincode;
    private String referrals; // maps to patientName
    private String guardianContact;
    private Integer age;
    private String gender;
    private String requestedOn;
    private EyeDetailsDto rightEye;
    private EyeDetailsDto leftEye;
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder // Added
    public static class EyeDetailsDto {
        private Double sph;
        private Double cyl;
        private Integer axis;
    }
}
