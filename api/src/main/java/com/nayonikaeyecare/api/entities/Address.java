package com.nayonikaeyecare.api.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Address {

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 100, message = "Address line 1 must be less than 100 characters")
    private String address1;

    @Size(max = 100, message = "Address line 2 must be less than 100 characters")
    private String address2;

    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must be less than 50 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State must be less than 50 characters")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Pincode must be 6 digits")
    private String pincode;
}
