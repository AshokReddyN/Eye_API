package com.nayonikaeyecare.api.dto.hospital;

import java.util.List;

import com.nayonikaeyecare.api.entities.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * HospitalRequest represents the incoming request payload for all of Hospital
 * APIs
 * 
 * @param id          - String - unique id of the hospital
 * @param name        - string - name of the hospital - mandatory
 * @param address     - Address - address of the hospital - mandatory
 * @param services    - String - services offered by the hospital - mandatory
 * @param status      - boolean - status of the hospital - mandatory
 * @param coordinator - String - name of the coordinator
 * @param coordinator_phonenumber - String - phonenumber of the coordinator
 * @param coordinator_email - String - email of the coordinator
 * @param googleLink  - String - link to google map
 */
@Builder
public record HospitalRequest(
    @Size(max = 36, message = "ID must be less than 36 characters") String id,

    @NotBlank(message = "Hospital name is required") @Size(max = 100, message = "Hospital name must be less than 100 characters") String name,

    @NotNull(message = "Address is required") @Valid Address address,

    @NotNull(message = "Service is required") @Size(max = 500, message = "Services description must be less than 500 characters") List<String> services,

    @NotNull(message = "Status is required") Boolean status,

    @Size(max = 100, message = "Coordinator name must be less than 100 characters") String coordinator,

    @Pattern(
    regexp = "^(\\d{10}|\\d{3,4}-\\d{6,8})$",
    message = "Phone number must be a valid 10-digit mobile number or a landline number with area code (e.g., 080-67459834, 0413-2272901)") String coordinator_phonenumber,

    @Pattern(regexp = "^[^\s@]+@[^\s@]+\\.[^\s@]+$", message = "Email must be a valid email address") String coordinator_email,

    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$", message = "Invalid URL format") String googleLink) {
}
