package com.nayonikaeyecare.api.mappers;

import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;
import com.nayonikaeyecare.api.entities.VisionAmbassador; // Import the missing class
import com.nayonikaeyecare.api.entities.user.User; // Import the missing class

public class VisionAmbassadorMapper {

    public static VisionAmbassadorResponse mapToVisionAmbassadorResponse(User user) {
        return VisionAmbassadorResponse.builder()
                .id(user.getId()!= null ? user.getId().toHexString() : null)
                .name((user.getFirstName()) + " " + (user.getLastName() != null ? user.getLastName() : ""))
                .phoneNumber(user.getPhoneNumber())
                // .status(user.getStatus())
                .city(user.getCity())
                .state(user.getState())
                .language(user.getLanguage())
                .createdAt(user.getCreatedAt())
                // .updatedAt(user.getUpdatedAt())
                .build();
    }
}