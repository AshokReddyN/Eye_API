package com.nayonikaeyecare.api.mappers;

import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;
import com.nayonikaeyecare.api.entities.VisionAmbassador; // Import the missing class

public class VisionAmbassadorMapper {

    public static VisionAmbassadorResponse mapToVisionAmbassadorResponse(VisionAmbassador visionAmbassador) {
        return VisionAmbassadorResponse.builder()
                .id(visionAmbassador.getId()!= null ? visionAmbassador.getId().toHexString() : null)
                .name(visionAmbassador.getName())
                .phoneNumber(visionAmbassador.getPhoneNumber())
                .status(visionAmbassador.isStatus())
                .city(visionAmbassador.getCity())
                .state(visionAmbassador.getState())
                .language(visionAmbassador.getLanguage())
                .createdAt(visionAmbassador.getCreatedAt())
                .updatedAt(visionAmbassador.getUpdatedAt())
                .build();
    }
}