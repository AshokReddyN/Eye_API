package com.nayonikaeyecare.api.dto.visionambassador;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class VisionAmbassadorRequest {
    // private String id;

    @NotBlank(message = "Vision ambassador name is required")
    @Size(max = 100, message = "Vision Ambassador name must be less than 100 characters")
    private String name;
    // private String email;

    @NotBlank(message = "Vision Ambassador name is required")
    @Size(max = 100, message = "Vision Ambassador name must be less than 100 characters")
    private String phoneNumber;

    @NotNull(message = "Status is required")
    @Valid
    private boolean status;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City name must be less than 100 characters")
    @NotNull(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State name must be less than 100 characters")
    @NotNull(message = "State is required")
    private String state;

    @NotNull(message = "Language is required")
    private String language;

}
