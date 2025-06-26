package com.nayonikaeyecare.api.mappers;

import com.nayonikaeyecare.api.dto.user.UserSummaryDto;
import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;
import com.nayonikaeyecare.api.entities.VisionAmbassador;
import com.nayonikaeyecare.api.entities.user.User;

public class VisionAmbassadorMapper {

    /**
     * This method might be deprecated or is not used for the primary VisionAmbassador
     * filtering logic that fetches VisionAmbassador entities first.
     * It was originally designed when VisionAmbassador data was directly derived from User.
     */
    public static VisionAmbassadorResponse mapToVisionAmbassadorResponse(User user) {
        if (user == null) {
            return null;
        }
        return VisionAmbassadorResponse.builder()
                .id(user.getId() != null ? user.getId().toHexString() : null) // This was user ID, might need to be VA ID if VA is primary
                .name((user.getFirstName()) + " " + (user.getLastName() != null ? user.getLastName() : ""))
                .phoneNumber(user.getPhoneNumber())
                // .status(user.getStatus()) // User status is different from VA status
                .city(user.getCity())
                .state(user.getState())
                .language(user.getLanguage())
                .createdAt(user.getCreatedAt()) // This is User's createdAt
                // .updatedAt(user.getUpdatedAt()) // This is User's updatedAt
                .build();
    }

    public static UserSummaryDto toUserSummaryDto(User user) {
        if (user == null) {
            return null;
        }
        return UserSummaryDto.builder()
                .id(user.getId() != null ? user.getId().toHexString() : null)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .city(user.getCity())
                .state(user.getState())
                .language(user.getLanguage())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static VisionAmbassadorResponse toVisionAmbassadorResponse(
            VisionAmbassador visionAmbassador,
            UserSummaryDto userDetails,
            int patientCount) {
        if (visionAmbassador == null) {
            return null;
        }
        return VisionAmbassadorResponse.builder()
                .id(visionAmbassador.getId() != null ? visionAmbassador.getId().toHexString() : null)
                .name(visionAmbassador.getName())
                .phoneNumber(visionAmbassador.getPhoneNumber())
                .status(visionAmbassador.isStatus())
                .city(visionAmbassador.getCity())
                .state(visionAmbassador.getState())
                .language(visionAmbassador.getLanguage())
                .createdAt(visionAmbassador.getCreatedAt())
                .updatedAt(visionAmbassador.getUpdatedAt())
                .userDetails(userDetails)
                .patientCount(patientCount)
                .build();
    }
}