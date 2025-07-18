package com.nayonikaeyecare.api.mappers;

import com.nayonikaeyecare.api.dto.hospital.HospitalResponse;
import com.nayonikaeyecare.api.entities.Hospital;

public class HospitalMapper {

    public static HospitalResponse mapToHospitalResponse(Hospital hospital) {
        return new HospitalResponse(
            hospital.getId()!= null ? hospital.getId().toHexString() : null,
            hospital.getHospitalCode(),
            hospital.getName(),
            hospital.getAddress(),
            hospital.getServices(),
            hospital.getStatus(),
            hospital.getCoordinator(),
            hospital.getCoordinator_phonenumber(),
            hospital.getCoordinator_email(),
            hospital.getGoogleLink(),
            hospital.getRegistration_date(),
            0L, // Default value for referralCount
            0L  // Default value for spectacleReferralCount
        );
    }
}
