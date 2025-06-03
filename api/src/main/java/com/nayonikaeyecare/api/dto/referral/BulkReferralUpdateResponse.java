package com.nayonikaeyecare.api.dto.referral;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkReferralUpdateResponse {

    private int totalRecords;
    private int updatedRecords;
    private int rejectedRecords;
    private List<RejectedReferralInfo> rejectedList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RejectedReferralInfo {
        private String referrals;
        private String guardianContact;
        private String gender;
        private String hospitalName;
    }
}
