package com.nayonikaeyecare.api.dto.patient;

import java.util.List;

import com.nayonikaeyecare.api.entities.Gender;
import com.nayonikaeyecare.api.entities.Guardian;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PatientRequest {
    @NotBlank(message = "Patient/referral name is required")
    @Size(max = 100, message = "Patient/Referral name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Ambassador id is required")
    @Size(max = 100, message = "Ambassador id must be less than 100 characters")
    @NotNull(message = "Ambassador id is required")
    private String ambassadorId;

    @NotBlank(message = "Gender is required")
    @Size(max = 100, message = "Gender must be less than 100 characters")
    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Age range is required")
    private String ageRange;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be a valid 10-digit number")
    private String phone;

    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City name must be less than 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State name must be less than 100 characters")
    private String state;

    private List<String> referralIds;
    private String hospitalName;
    private String status;

    @Valid
    @NotNull(message = "Guardian information is required")
    private Guardian guardian;
}
